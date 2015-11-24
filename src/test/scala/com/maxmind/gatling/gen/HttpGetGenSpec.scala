package com.maxmind.gatling.gen

import com.maxmind.gatling.BaseSpec
import org.scalacheck.{Arbitrary, Gen, Prop}
import org.specs2.specification.core.Fragments

import scalaz.Scalaz._
import scalaz._

class HttpGetGenSpec extends BaseSpec {

  override protected def innerIs: Fragments = s2"""

    Sanity tests for 3rd Party Libs - basic tests and examples

        Builtin generation

          Specs2 with ScalaCheck
          I
            Can test properties                                  $specs2Gen
            Custom generators work                               $specs2Custom

          ScalaCheck

            Gen works outside of Specs2 ScalaCheck harness       $noSpecs2
            Gen works outside its normal properties environment  $noProps

          Rng

            Rng is not constant                                  $rngNotConstant
            Rng covers domain                                    $rngBoundaries

  """

  def specs2Gen = s2"${prop { (s: String) => s.reverse.reverse must_== s }}"

  def specs2Custom = {
    implicit def arbString: Arbitrary[String] =
      Gen.alphaLowerChar map {_.toString}
    s2"""${prop { (c: String) ⇒ c must =~("[a-z]") }}"""
  }

  def noSpecs2 =
    s2"${Prop.forAll (Gen pick(9, 0 to 9)) {_ count (_ > 9) must_== 0}}"

  def noProps = {
    val g = Gen choose (0, 9)
    s2"${distinctSamples(g) count { (i: Int) ⇒ i <= 9 && i >= 0 } must_== 10}"
  }

  def rngNotConstant = {
    implicit def arbGenLong: Arbitrary[Gen[Long]] =
      Gen.choose(Long.MinValue, Long.MaxValue) |> {Arbitrary apply _}
    s2"${prop { (g: Gen[Long]) ⇒ distinctSamples(g).length must_!= 2 }}"
  }

  def rngBoundaries = {
    implicit def arbGenLong: Arbitrary[Gen[Int]] =
      Gen.choose(1, 3) |> {Arbitrary apply _}
    def analyze(g: Gen[Int]) = distinctSamplesBig(g).sorted mkString ","
    s2"${prop { (g: Gen[Int]) ⇒ analyze(g) must_== "1,2,3" }}}"
  }
}

