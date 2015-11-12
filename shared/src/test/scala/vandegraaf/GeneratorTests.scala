package vandegraaf

import org.scalacheck.Gen
import org.scalacheck.Gen.Parameters
import utest._

import scala.util.Random

object GeneratorTests extends TestSuite {

  val sampleN = 100

  def makeRng: Random = new Random(0)

  // a Choose generator ready to generate from an rng
  def makeChooseGen(rng: Random): Option[Int] =
    Gen.choose(1, 1000).apply(Parameters.default withRng rng)

  def makeChooseGen2(rng: Random): Option[Int] =
    Gen.choose(1, 1000).apply(Parameters.default)

  def assertChildrenEqual(parent: IndexedSeq[_]): Unit =
    assert(parent.distinct.size == 1)

  def tests = TestSuite {

    "RNGs are seeded correctly" - {

      val commonRng = makeRng

      // Make a list of 1 value each from N RNGs, and from an RNG that is
      // either common or cloned.
      // Those with a common generator, will be random, because ScalaCheck uses
      // the Java mutable RNG.
      // Those with a cloned generator, will all generate the same values on
      // each apply, because their generators share the same seed and are called
      // the same number of times.
      val generate = (useCommon: Boolean) ⇒ for (_ ← 1 to sampleN)
        yield makeChooseGen(if (useCommon) commonRng else makeRng)

      val (common, cloned) = (generate(true), generate(false))

      // head is defined and same in both- both heads are 1st calls to RNG
      assert(common.head.isDefined, common.head == cloned.head)

      // entire cloned seq is same as head
      assertChildrenEqual(cloned)

      // but common seq is not all equal- RNG mutates between generations and
      // the RNG is common so different RNGs will generate different numbers
      assert(common != cloned)
    }

    "cloned RNGs remain in sync" - {

      // make a list of functions that generate with same generator
      val generators = for (_ ← 1 to sampleN) yield () ⇒ makeChooseGen(makeRng)

      // apply the functions to get a list
      for (_ ← 1 to 10) assertChildrenEqual(generators map { _ () })
    }

    "RNG seed is split correctly" - {
      val seed = new Seed(0)
      val childA = seed.splitRng()
      val childB = seed.splitRng()

      val rand = (s:Seed) ⇒ s.rng.nextInt()
      val (original, a, b) = (rand(seed), rand(childA), rand(childB))

      assert(original != a)
      assert(original != b)
      assert(a != b)

    }
  }
}
