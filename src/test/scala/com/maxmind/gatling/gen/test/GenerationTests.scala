package com.maxmind.gatling.gen.test

import scala.util.Random
import scalaprops._
import scalaz.Scalaz._

object GenerationTests extends Scalaprops {

  type Seed = Long

  val defaultSize = 100

  private def realSeed(): Seed = new Random().nextLong()

  private def samples[T](gen: Gen[T])(seed: Seed): List[T] =
    gen samples(defaultSize, defaultSize, seed)

  private val sampleInts: Seed ⇒ List[Int] = samples(Gen[Int])

  // A pair of seeds, sometimes equal sometimes not.
  // Trivial property example- chars are always one char long.
  private implicit def numCharGen: Gen[Char] =
    Gen.genNumChar map { _.asInstanceOf[Char] }

  val `∀c, c∈Char ⊃ ∣c∣=1` = Property.forAll {
    c: Char ⇒ c.toString.length == 1
  }

  /* For all generator=g, seed=s, and generation step=t,
     If vₜ=g(s)ₜ is generated value at step t, and vₜ₋₁=g(s)ₜ₋₁ at step t-1,
     Then vₜ and vₜ₋₁ are not correlated.

     Here we check only equality, i.e. ∀ g,s, if the series of generated values
     is vs=v₀..vₜ₋₁, and if the distinct values of those are ds=vs.distinct,
     then there exists an n big enough so that ∀t>n implies ds≠vs and ∣ds∣≠1.

     I.e. for a large enough gen count, the series of generated values will
     never be all self equal nor all self distinct. At large gen counts, there
     will always be some generated values that are equal, and some that are
     distinct.

     Because checking for ∃n is slow, we choose a large generation codomain that
     guarantees we can safely switch the ∃ into a ∀. There will be many hardware
     failures before a 100 identical integers are generated. Thus at Int*n=100
     instead of checking there exists an n, we simply check for all n.

     ∀g∈G, ∀s∈Seed ⇒ ∃n∈ℕ, vs=❴gen(s)ₜ∣t∈ℕ ∧ 0≤t<n-1❵, ∣vs∣≠∣vs.distinct∣≠1

  */
  val `generation steps are uncorrelated` =
    Property.forAll { s: Seed ⇒ sampleInts(s).distinct.length != 1 }

  /* As in the "generated values are uncorrelated" property, we can safely
     assert generated series will always be different between generators thanks
     to the large generation codomain.

     ∀g∈G, ∀s₀,s₁∈Seed, s₀≠s₁ ⇒ g(s₀)≠g(s₁) ∧ s₀=s₁ ⊃ g(s₀)=g(s₁)
  */
  val `same/different seeds ≡ same/different generators` =

    val seedPairGen = Gen[(Seed, Seed)] map {
      val sampleBooleans = samples(Gen.genBoolean) _
      ss: (Seed, Seed) ⇒ (ss._1, sampleBooleans(realSeed()).head ? ss._1 | ss._2)
    }

    Property.forAllG(seedPairGen) {
      ss: (Seed, Seed) ⇒ {
        val (s0, s1) = ss
        val (v0, v1) = (sampleInts(s0), sampleInts(s1))
        (s0 == s1) ? (v0 == v1) | v0 != v1
      }
    }

  /* Scalaz calls the constant gen pure gen or η, the greek letter theta.

     a∀η∈Gη, ∀s∈Seed ⇒ vs=❴η(s)ₜ∣t∈ℕ ∧ 0≤t<n-1❵, ∣vs.distinct∣=1
   */
  val `pure gen is constant` = Property.forAll {
    val isSelfEqual =  (_: List[Int]).distinct.length == 1
    (s: Seed, n: Int) ⇒ isSelfEqual((Gen.value(n) |> samples)(s))
  }

  val `rng is immutable` = Property.forAll {
    s: Seed ⇒ {
      val rng = MersenneTwister64.standard(s)
      val generate: Rand ⇒ List[Int] = r ⇒ samples(Gen.genIntAll)(r.nextInt._2)
      val v1 = generate(rng)
      (1 to defaultSize) foreach { _ ⇒ rng.nextInt } // try to mutate even more
      val v2 = generate(rng)
      v1 == v2
    }
  }
}

