package com.maxmind.gatling

import org.scalacheck.Gen
import org.specs2.ScalaCheck
import org.specs2.matcher._
import org.specs2.mutable.Specification

import scalaz.Scalaz._
import scalaz._

/* This sample size will be used when sampling from a generator.
*/
object SampleSize {
  implicit val defaultSampleSizeImpicit = SampleSize(100)
}

case class SampleSize(size: Int) {
  def sample[T](loopOver: ⇒ T): Seq[T] = for (_ ← 1 to size) yield loopOver

  def incTenFold: SampleSize = SampleSize(size * 10)
}

trait BaseSpec extends Specification with ScalaCheck with Matchers {

  // Final flatten will remove any None values in the generated set, but we
  // don't care because we don't use the Option feature of Gen.

  protected def sampleSet[T](g: Gen[T])(implicit s: SampleSize): Set[T] =
    (s sample g(Gen.Parameters.default)).toSet.flatten
}


