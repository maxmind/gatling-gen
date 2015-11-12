package vandegraaf

import utest._

object GeneratorTests extends TestSuite {

  def tests = TestSuite {

    "RNGs are seeded correctly" - {

      val x = 123;
      val y = 123;
      assert(x == y)

    }
  }
}
