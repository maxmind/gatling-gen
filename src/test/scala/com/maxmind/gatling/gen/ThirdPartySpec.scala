package com.maxmind.gatling.gen

import com.maxmind.gatling.rng.FakeRandom
import io.gatling.core.feeder.Feeder
import org.scalacheck.{Arbitrary, Gen, Prop}

import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.test.{SampleSize, BaseSpec}

class ThirdPartySpec extends BaseSpec {

  "Specs2, ScalaCheck, and Gatling: self-sanity + integration examples".title

  "• ScalaCheck generation, inside and outside of Specs2" >> {

    "⋅ Specs2 testing ScalaCheck properties" >>
      prop { s: String => s.reverse.reverse must_== s }

    "⋅ Default Arbitrary is imported" >>
      prop { (a: Char, b: Char) ⇒ (a.toString + b.toString).length must_== 2 }

    "⋅ Custom Gen injected through implicit Arbitrary" >> {
      implicit val arbChar = Arbitrary(Gen.alphaLowerChar)
      prop { (_: Char).toString must =~("[a-z]") }
    }

    "⋅ Use ScalaCheck properties with Specs2 matchers" >>
      Prop.forAll(Gen pick(3, 0 to 9)) { _.sum must be_<=(9 + 8 + 7) }

    "⋅ Use generators outside of ScalaCheck/Specs2 properties" >> {
      sampleSet(Gen choose (0, 9)) must contain(allOf(beBetween(0, 9)))
    }

    "⋅ Use generators outside of ScalaCheck/Specs2 properties" >> {
        sampleSet(Gen choose(0, 9)) must contain(allOf(beBetween(0, 9)))
    }

  }

  """• Random number generation
  Note: Randomness quality tests are at Randomness.scala""" >> {

    "⋅ Is not constant" >>
      prop { sampleSet(_: Gen[Long]).size must be_>(1) }.
        setGen(Arbitrary.arbitrary[Long])

    """⋅ Select 1-of-3 can be covered in 1000 generations
    When picking 1,000 times from the bag (1,2,3), chance of not picking
    at least one of each = (2^1,000 + 1) / 3^999 =~ 2*10e-176""" >> {
      implicit lazy val size: SampleSize = SampleSize(1000)

      prop { g: Gen[Int] ⇒ sampleSet(g) must_== Set(1, 2, 3) }.
        setGen(Gen.choose(1, 3))
    }

    "⋅ Rng can be fixed, turning a Boolean gen into a constant true gen" >> {
      prop { b: Boolean ⇒ b should beTrue } set (rng = FakeRandom { () ⇒ 6L })
    }
  }

  "• Gatling Feeder examples" >> {

    """⋅ Gatling documentation example feeder
    Gatling virtual users use Feeders as a network traffic source. A Feeder is
    just an Iterator[Map[String,T]], as explained at:
    http://gatling.io/docs/2.0.0-RC2/session/feeder.html""" >> {

      val iut: Feeder[Int] = Iterator from 1 map { i ⇒ Map("foo" → i) }

      (iut take 5 map { _ ("foo") }).toSet must_== Set(1, 2, 3, 4, 5)
    }

    "⋅ Gatling generating through ScalaCheck" >> {
      val gen = Gen.alphaChar filter { _ != '!' }
      val iter = Iterator continually gen.sample
      val iut: Feeder[Char] = iter map { c ⇒ Map("bar" → (c | '!')) }
      val feed = iut take 100 map { _ ("bar") }

      feed.toSeq must not contain '!'
    }
  }
}

