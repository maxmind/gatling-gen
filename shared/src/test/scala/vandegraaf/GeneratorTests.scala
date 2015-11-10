package vandegraaf

import utest._

object GeneratorTests extends TestSuite {
  def tests = TestSuite {
    'first {
      val actual = 1 + 2
      val expected = 2 + 1
      assert(actual == expected)
    }
  }
}
