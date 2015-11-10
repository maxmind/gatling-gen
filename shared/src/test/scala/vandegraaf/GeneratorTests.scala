package vandegraaf

import org.scalacheck.Gen
import utest._

object GeneratorTests extends TestSuite {
  def tests = TestSuite {
    "RNGs are seeded correctly"-{
      val gen1 = Gen.choose(1, 3);
      val gen2 = Gen.choose(1, 3);
      assert(1 == 1);
//      assert(gen1.g
    }
  }
}
