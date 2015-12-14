package com.maxmind.gatling.dev

import ammonite.ops._
import java.io.{BufferedOutputStream, File, FileOutputStream}
import org.scalacheck.Gen
import scala.language.reflectiveCalls
import scala.sys.process._
import scala.util.Random
import scala.util.hashing.MurmurHash3
import scala.util.matching.Regex
import scalaz.Scalaz._
import scalaz._
import squants.storage.StorageConversions._

/**
  * Run PracRand randomness quality test suite in checkout dir.
  *
  * PracRand is from pracrand.sourceforge.net, written by Chris Doty-Humphrey.
  *
  * First we run some sanity tests for:
  *
  * 1) Asserting sample size is big enough
  * 2) Calibrating the watermark, needed because Java standard RNGs so not
  * pass 100% of the tests.
  *
  * We run three types of sanity tests:
  *
  * 1) Best PracRand RNG is random when tested by PracRand
  * 2) Various kinds of fake randoms are not random
  * 3) Java builtin rngs
  *
  * Then we test the gatling-gen project randomness:
  *
  * 1) Generating bytes using Arbitrary[Byte] does not kill rng randomness
  * 2) Combinators on Arbitrary[Byte] should maintain randomness
  */

object RandomnessApp extends App {

  // More than or equal to this number of passing tests is random enough.
  val minPass = 72

  val testSize      = 1500D.megabytes
  // Gen size must be larger than test size, or PracRand will core dump.
  val genSize       = testSize + testSize / 10
  val toolPath      = cwd / 'ext / 'PracRand
  val selfTester    = toolPath / "self-test.sh"
  val fileTester    = toolPath / "rng-test-from-file.sh"
  val tmpFile       = toolPath / "random-bytes.tmp"
  val okRegex       = "no anomalies in".r
  val warnRegex     = """and (\d+) test result\(s\) without anomalies""".r
  val batch         = 1000
  // PracRand test unit is 2^ de facto (mebibyte) not decimal SI (megabyte).
  val testSizer     = testSize.toMebibytes.toInt.toString + "MB"
  val genSizer      = genSize.toBytes.toInt.toString
  val shouldSucceed = runTest(expect = true) _
  val shouldFail    = runTest(expect = false) _

  runTests()

  def runTests() = {

    runCommand(expect = true)("self-test", selfTester.toString + " " + genSizer)

    shouldFail("constant generator") { _ ⇒ Array.fill(batch)(123: Byte) }

    shouldFail(s"$batch linear generators")({ () ⇒
      val gen: Array[Byte] = ((1 to batch) map { i ⇒ (i + 3).toByte }).toArray
      (i: Int) ⇒ gen map { (j: Int) ⇒ (j + i).toByte }
    } apply ())

    shouldSucceed("java.util.Random")({ () ⇒
      val (rng, bytes) = (new Random(), new Array[Byte](batch))
      (_: Int) ⇒ { rng nextBytes bytes; bytes }
    } apply ())

    shouldFail("ScalaCheck 1.12.5 generates bytes like this")({ () ⇒
      val rng = new Random()
      val nextLong = () ⇒ rng nextLong ()
      val (h, l) = (Byte.MaxValue, Byte.MinValue)
      val d = h - l + 1 // 256

      val longToByte: Long ⇒ Byte = n ⇒ (l + Math.abs(n % d)).toByte
      (_: Int) ⇒ (Array fill batch) { longToByte(nextLong()) }
    } apply ())

    {
      seededFromParentTest("java.util.Random: 1000 seeded from 1") {
        (rng: Random) ⇒ rng nextLong ()
      }

      seededFromParentTest("java.util.Random: 1000 seeded from 1 with hashing") {
        (rng: Random) ⇒
          val makeInt: () ⇒ Array[Byte] = () ⇒ { new Array[Byte](4) } ◃ rng.nextBytes
          val hashInt: () ⇒ Int = () ⇒ MurmurHash3.bytesHash(makeInt())
          val (a, b) = (hashInt(), hashInt())
          (a << 32) | (b & 0xFFFFFFFFL)
      }

      def seededFromParentTest(name: String)(seeder: Random ⇒ Long): Unit = {
        val root = new Random()
        val rngs = (1 to batch) map { _ ⇒ new Random(seeder(root)) }
        val bytes = new Array[Byte](2)
        val circular = (Stream continually { rngs.toStream }).flatten

        shouldFail(name)({ () ⇒ { (_: Int) ⇒ bytes ◃ (circular take 1).head.nextBytes }
        } apply ())
      }
    }

    {
      def arbByte = Gen.Choose.chooseByte choose (Byte.MinValue, Byte.MaxValue)
      val params = Gen.Parameters.default

      shouldSucceed("Arbitrary[byte]")({ () ⇒
        (_: Int) ⇒ (Array fill batch) { arbByte(params).get }
      } apply ())

      shouldSucceed("Arbitrary[byte] with combinators")({ () ⇒
        val gen = for {x ← arbByte; y ← arbByte; z ← Gen.oneOf(x, y)} yield z
        (_: Int) ⇒ (Array fill batch) { gen.sample.get }
      } apply ())
    }
  }

  def runTest(expect: Boolean)(name: String)(code: Int ⇒ Array[Byte]) = {

    val w: BufferedOutputStream = tmpFile.toString ▹ { new File(_) } ▹
      { new FileOutputStream(_) } ▹ { new BufferedOutputStream(_) }

    color(name, Console.MAGENTA + s"generating $testSizer + 10%")

    // We don't mind if the file is a bit too big, so we ignore the remainder.
    var i = 0
    var left = genSize.toBytes.toInt
    while (left > 0) {
      val bytes = code(i)
      w.write(bytes)
      left -= bytes.length
      i += 1
    }
    w close ()

    runCommand(expect)(name, fileTester toString ())
  }

  def runCommand(expect: Boolean)(name: String, cmd: String): Unit = {

    // TODO
    def logger() = new {
      val sb                 = new StringBuffer
      val add: String ⇒ Unit = { s ⇒
        s ◃ { s ⇒ sb append (s + '\n') } ◃ { s ⇒ println("#       " + s) }
      }
    }

    color(name, Console.MAGENTA + s"testing $testSizer")

    val (outLog, errLog) = (logger(), logger())
    val exitValue = Process(cmd + " " + testSizer) run ProcessLogger(
      outLog.add, errLog.add) exitValue ()

    // Die unless: ❨ (exitCode = 0) ∧ (errorLog = ∅) ❩
    assert(
      exitValue == 0 || errLog.sb.toString.isEmpty,
      s"Error in $cmd: exitValue=$exitValue stderr=${ errLog.sb.toString }"
    )


    // Grep stdout log for "total OK" and "partial OK" messages, deduce if rng is OK with:
    // ❨ (total OK msg) ∨ (partial OK msg ∧ OK# > minPass) ❩ ⊃ (rng OK)

    val okMatch :: warnMatch :: Nil =
      List(okRegex, warnRegex) ∘ { _ findFirstMatchIn outLog.sb.toString }

    {
      {
        okMatch.nonEmpty ? expect | {
          warnMatch ∘ { (p: Regex.Match) ⇒ p group 1 } match {
            case None        ⇒ !expect
            case Some(count) ⇒ expect == (count.toInt >= minPass)
          }
        }
      } ? s"${ Console.GREEN }OK" | s"${ Console.RED }FAIL"
    } ▹ { color(name, _) }

  }

  def color(s1: String, s2: String) =
    println(s"${ Console.RESET }# $s1: $s2${ Console.RESET }")
}

