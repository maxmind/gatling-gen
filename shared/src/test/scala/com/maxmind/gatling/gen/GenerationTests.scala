package com.maxmind.gatling.gen

import nyaya.gen.Gen
import utest._

import scalaz.Scalaz._

object GenerationTests extends TestSuite {

  type Matrix[T] = Seq[Seq[T]]

  val (egN, egMin) = 10 → 'a'
  val egMax        = egMin -+- egN.toChar.pred
  val egRange      = egMin to egMax
  val egPartRange  = egMin.succ to egMax

  def selfTestGrain: Grain = Grain.selfTest

  def makeGen: Gen[Char] = Gen chooseChar(egMin, egPartRange)

  def forN[T](thunk: => T): Seq[T] = (Seq fill egN)(thunk)

  def genN[T](grain: Grain, gen: Gen[T], count: Int = egN): Seq[T] =
    (grain { gen } take count).toSeq

  // foreach egN gens, get a grain, gen egN values, materialize into a matrix
  // and transpose so that each row is the result of one gen step from all gens
  def gen[T](gen: ⇒ Gen[T])(grain: ⇒ Grain): Matrix[T] =
    forN { genN(grain, gen) }.transpose

  def gen(grain: ⇒ Grain): Matrix[Char] = gen { makeGen } { grain }

  def assertSelfSimilarity(allEqual: Boolean)(parentSeq: Matrix[_]): Unit = {
    for (childSeq ← parentSeq) {
      val distinct = childSeq.distinct.size
      allEqual ? assert(distinct == 1) | assert(distinct != 1)
    }
  }

  def assertSelfEqual = assertSelfSimilarity(allEqual = true) _

  def assertSelfDistinct = assertSelfSimilarity(allEqual = false) _

  def tests = TestSuite {

    'basic - {
      //    We will be generating tuples of Boolean and Char:
      type GeneratedType = (Boolean, Char)

      //    Our goal is to produce a sequence of 5 values from this type.

      // 1- Build a generator. E.g.: a tuple (Boolean, Char) as below.
      val generator: Gen[GeneratedType] = for (
        b ← Gen.boolean;
        c ← Gen.chooseChar('a', 'b' to 'z')
      ) yield (b, c)

      // 2- Create a special grain (self-tests, standard tests), or a grain
      // on a specific Long seed with Grain(intSeed):
      val grain: Grain = Grain.standardTest

      // 3- Run a block of generation code. This is the code where generation
      //    takes place- from generator to materialized generated value.
      val generated = (grain { generator } take 5).mkString

      assert(generated == "(false,j)(false,m)(false,y)(false,u)(true,w)")
    }

    'generation - {

      'Grain - {

        "∀g₁g₂∈G, g₁seed=g₂seed ⊃ g₁=g₂ ≡ same seeds ⊃ same generated" - {
          val generated = gen { Grain(12345) }
          "Nth gen identical" - { assertSelfEqual(generated) }
          "Nth,Nth-1 gen distinct" - { assertSelfDistinct(generated.transpose) }
        }

        "∀g₁g₂∈G, g₁seed≠g₂seed ⊃ g₁≠g₂ ≡ !same seeds ⊃ !same generated" - {
          val rootGrain = selfTestGrain
          val generated = gen { Grain(rootGrain) }
          "Nth gen distinct" - { assertSelfDistinct(generated) }
          "Nth,Nth-1 gen distinct" - { assertSelfDistinct(generated.transpose) }
        }

        "∀g₁g₂∈G, g₁g₂∈Gᴾᵁᴿᴱ ⊃ g₁=g₂ ≡ pure gen ⊃ always constant" - {
          val rootGrain = selfTestGrain
          val generator = Gen pure 'd'
          val generated = gen { generator } { Grain(rootGrain) }
          "Nth gen identical" - { assertSelfEqual(generated) }
          "Nth,Nth-1 gen identical" - { assertSelfEqual(generated.transpose) }
        }
      }

      'Coverage {

        def grain = selfTestGrain
        val generator = Coverage(egMin, egPartRange: _*)
        val generated = genN(grain, generator, count = egN * egN)
        val expected = egRange

        "Coverage covers" - {
          val actual = (generated take egN).sorted
          assert(actual == expected)
        }
        "Coverage shuffles covered" - {
          val actual = generated
          assert(generated != expected)
        }
        "Coverage switches to Gen.choose" - {
          val actual = (generated slice(egN, 2 * egN)).sorted
          assert(actual != expected)
        }
      }
    }
  }
}
