package com.maxmind.gatling.dev

import java.util.concurrent.ThreadLocalRandom

import com.maxmind.gatling.rng.FakeRandom
import org.scalacheck.Gen.Parameters
import org.scalacheck.{Arbitrary, Gen}
import org.scalameter.{Bench, Gen ⇒ MeterGen}

import scala.util.Random
import scalaz.Scalaz._
import scalaz._

/** Single threaded generation micro benchmarks.
  *
  * bench(b: Int => Option[Int]) will run the code and print benchmarks, e.g:
  *
  * bench { counter: Int => myMeasuredFunction(counter) }
  *
  */
object BenchmarkApp extends Bench.LocalTime {

  val stepInKiloSteps = 100
  val step            = stepInKiloSteps * 1000
  val sizes           = (1 * step, 5 * step, step) // from, upto, hop

  def bench(b: Int ⇒ Option[Int]): Unit = {

    val sizedRange = MeterGen.range("size")(sizes._1, sizes._2, sizes._3)
    val sizedRanges = for (size ← sizedRange) yield 0 until size

    using(sizedRanges) in { (r: Range) ⇒ r map { i ⇒ b(i) map 1.+ } }
  }

  val intGen                = Arbitrary.arbitrary[Int]
  val javaUtilRandom        = new Random()
  val javaThreadLocalRandom = ThreadLocalRandom.current()

  performance of "Generation" in {

    performance of "Fastest possible baselines" in {

      // 100K/sec
      measure method "Nop" in bench { _.some }

      // 50K/sec
      measure method "Java util random" in
        bench { _ ⇒ javaUtilRandom.nextInt().some }

      //
      measure method "Java thread-local random" in
        bench { _ ⇒ javaThreadLocalRandom.nextInt().some }
    }

    performance of "ScalaCheck" in {

      val intGen = Arbitrary.arbitrary[Int]
      val genInt = intGen(_: Gen.Parameters)
      val sample = (rng: Random) ⇒ (Parameters.default withRng rng) |> genInt

      performance of "Rng choice on fastest Int gen" in {

        def benchRng(rng: Random) = bench { _ ⇒ sample(rng) }

        // Note rng is not where the time goes.

        // 7K/sec
        measure method "Stub rng with super-fast constant" in
          benchRng(FakeRandom { () ⇒ 123L })

        // 6K/sec
        measure method "Using java util random" in
          benchRng(javaUtilRandom)

        // 5K/sec
        measure method "With Java thread-local random" in
          benchRng(javaThreadLocalRandom)
      }
      performance of "Gen complexity" in {

        // 1.5K/sec
        measure method "Three gens and combinators, no collections" in {
          val gen = for (
            i ← intGen;
            j ← intGen map { 2.* };
            k ← Gen.const(j - i) filter { _ % 2 == 0 }
          ) yield k + j - i

          bench { _ ⇒ gen.sample }
        }
      }
    }
  }
}

/*

::Benchmark Generation.Fastest possible baselines.Nop::
Parameters(size -> 100000): 2.15829
Parameters(size -> 200000): 4.350438
Parameters(size -> 300000): 6.502275
Parameters(size -> 400000): 8.691458
Parameters(size -> 500000): 10.873246

::Benchmark Generation.Fastest possible baselines.Java util random::
Parameters(size -> 100000): 2.482613
Parameters(size -> 200000): 5.03178
Parameters(size -> 300000): 7.496197
Parameters(size -> 400000): 10.018049
Parameters(size -> 500000): 12.550072

::Benchmark Generation.Fastest possible baselines.Java thread-local random::
Parameters(size -> 100000): 2.186524
Parameters(size -> 200000): 4.376645
Parameters(size -> 300000): 6.613001
Parameters(size -> 400000): 8.822945
Parameters(size -> 500000): 11.033432

::Benchmark Generation.ScalaCheck.Rng choice on fastest Int gen.Stub rng with
 super-fast constant::
Parameters(size -> 100000): 19.845232
Parameters(size -> 200000): 39.731332
Parameters(size -> 300000): 59.652303
Parameters(size -> 400000): 79.700604
Parameters(size -> 500000): 99.399719

::Benchmark Generation.ScalaCheck.Rng choice on fastest Int gen.Using java
util random::
Parameters(size -> 100000): 25.939782
Parameters(size -> 200000): 51.974261
Parameters(size -> 300000): 78.001929
Parameters(size -> 400000): 104.010259
Parameters(size -> 500000): 130.054869

::Benchmark Generation.ScalaCheck.Rng choice on fastest Int gen.With Java
thread-local random::
Parameters(size -> 100000): 23.976724
Parameters(size -> 200000): 48.049091
Parameters(size -> 300000): 72.213066
Parameters(size -> 400000): 96.195578
Parameters(size -> 500000): 120.286977

::Benchmark Generation.ScalaCheck.Gen complexity.Three gens and combinators,
no collections::
Parameters(size -> 100000): 66.552535
Parameters(size -> 200000): 135.350497
Parameters(size -> 300000): 202.392687
Parameters(size -> 400000): 272.545514
Parameters(size -> 500000): 345.348511


 */
