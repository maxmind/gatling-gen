package vandegraaf

import org.scalacheck.Gen
import org.scalacheck.Gen.Parameters
import utest._

import scala.util.Random

object GeneratorTests extends TestSuite {

  val sampleN = 100

  def makeRng: Random = new Random(0)

  // a Choose generator ready to generate from an rng
  def makeChooseGen(rng: Random) =
    Gen.choose(1, 1000).apply(Parameters.default withRng rng)

  def assertChildrenEqual(parent: IndexedSeq[_]): Unit =
    assert(parent.distinct.size == 1)

  def tests = TestSuite {

    "RNGs are seeded correctly" - {

      val commonRng = makeRng
      val generate = (useCommon: Boolean) ⇒
        (1 to sampleN) map (_ ⇒
          makeChooseGen(if (useCommon) commonRng else makeRng))

      // Make a list of 1 value each from N RNGs, and from an RNG that is
      // either common or cloned.
      // Those with a common generator, will be random, because ScalaCheck uses
      // the Java mutable RNG.
      // Those with a cloned generator, will all generate the same values on
      // each apply, because their generators share the same seed and are called
      // the same number of times.
      val (common, cloned) = (generate(true), generate(false))
      val commonHead = common.head

      // head is defined and same in both- both heads are 1st calls to RNG
      assert(commonHead.isDefined)
      assert(commonHead == cloned.head)

      // entire cloned seq is same as head
      assertChildrenEqual(cloned)

      // but common seq is not- RNG mutates between generations and the RNG is
      // common
      assert(common != cloned)
    }

    "cloned RNGs remain in sync" - {

      // make a list of functions that generate with same generator
      val generators = (1 to sampleN)
        .map { _ ⇒ () ⇒ makeChooseGen(makeRng) }

      // apply the functions to get a list
      val generate = () ⇒ generators map (g ⇒ g())

      (1 to 10) foreach { _ ⇒ assertChildrenEqual(generate()) }
    }
  }
}
