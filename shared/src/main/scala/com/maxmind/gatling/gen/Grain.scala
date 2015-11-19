package com.maxmind.gatling.gen

import nyaya.gen.{Gen, GenCtx, GenSize}

import scala.collection.AbstractIterable
import scala.util.Random
import scala.util.hashing.MurmurHash3
import scalaz.Scalaz._

// Helps with:
// 1) keeping special seeds
// 2) grain { Gen[T] } materializes a generator using the seed in the grain
// 3) Grain(grain) give you a new grain split from the parent
// 4) toString shows initial seed, as does initialSeed()

case class Grain(seed: Long) {

  private val intsToLong     = (a: Int, b: Int) ⇒ (a << 32) | (b & 0xFFFFFFFFL)
  private val longToInts     = (l: Long) ⇒ {val a = l >> 32; a.toInt → l.toInt }
  private val intToBytes     = (a: Int) ⇒ BigInt(a).toByteArray
  private val hashInt        = (v: Int) ⇒ MurmurHash3 bytesHash intToBytes(v)
  private val hashIntPair    = (a: Int, b: Int) ⇒ (hashInt(a), hashInt(b))
  private val hashLongToInts = (l: Long) ⇒ hashIntPair tupled longToInts(l)
  private val hashLong       = (l: Long) ⇒ intsToLong tupled hashLongToInts(l)
  private val buildRng       = (s: Long) ⇒ new Random(seed)

  private lazy val rngs: (Random, Random) =
    seed |> buildRng |> (r ⇒ (r, r |> nextSeed |> buildRng))

  private lazy val (rng, splitterRng) = rngs._1 → rngs._2

  val initialSeed: Long = seed

  def split(): Grain = splitterRng |> nextSeed |> Grain.apply

  def shuffle[T](l: AbstractIterable[T]): AbstractIterable[T] =
    rng shuffle l.toList

  def apply[T](
    generator: Gen[T],
    size: Int = GenSize.Default.value
  ): Iterator[T] = {
    implicit val grain:Grain = this
    generator samples buildGenCtx(size)
  }

  private def buildGenCtx(size: Int): GenCtx =
    new GenCtx(rng.self, GenSize(size))

  // un-hashed generated seed = correlation to parent and between siblings
  private def nextSeed(rng: Random): Long = hashLong(rng nextLong())

  override def toString: String = super.toString + "[" + "seed:" + seed + "]"
}

object Grain {

  final val SELF_TEST_SEED = 0
  final val STANDARD_SEED  = 1

  def selfTest: Grain = Grain(SELF_TEST_SEED)

  def standardTest: Grain = Grain(STANDARD_SEED)

  def apply(grain: Grain): Grain = grain split()
}

// TODO: https://github.com/clojure/test.check/
// blob/master/src/main/clojure/clojure/test/check/random.cljs

