package vandegraaf

import org.scalacheck.Gen
import org.scalacheck.Gen.Parameters
import utest._

import scala.util.Random

object GeneratorTests extends TestSuite {

  def tests = TestSuite {

    "RNGs are seeded correctly" - {

      val makeRng = () ⇒ new Random(0)
      val commonRng = makeRng()

      // Make a list of generators, with an RNG that is either common or unique
      val makeGenerator = (useCommon: Boolean) ⇒
        (1 to 100) map
        (_ ⇒ Gen.choose(0, 1000) apply
             Parameters.default.withRng(
               if (useCommon) commonRng else makeRng()))

      val (fromCommon, fromUnique) = (makeGenerator(true), makeGenerator(false))
      val commonHead = fromCommon.head

      assert(commonHead.isDefined)
      assert(commonHead == fromUnique.head)
      assert(
        ((1 to fromCommon.size) map (_ ⇒ commonHead)) ==
        fromUnique)
      assert(fromCommon != fromUnique)
    }
  }
}
