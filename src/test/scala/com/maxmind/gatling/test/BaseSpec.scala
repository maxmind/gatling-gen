package com.maxmind.gatling.test

import scala.language.implicitConversions
import scalaz.Scalaz._
import scalaz._
import org.scalacheck.Gen
import org.specs2.ScalaCheck
import org.specs2.matcher._
import org.specs2.mutable.Specification

object SampleSize {

  // The sample size we will use for generating.
  implicit val defaultSize: SampleSize = 100

  implicit def sampleSizeToInt(s: SampleSize): Int = s.toInt
  implicit def intToSampleSize(i: Int): SampleSize = SampleSize(i)
}

case class SampleSize(size: Int) {
  val toInt = size
}

trait BaseSpec extends Specification
  with FutureMatchers
  with Matchers
  with MustMatchers
  with ResultMatchers
  with ScalaCheck {

  lazy val genParams: Gen.Parameters = Gen.Parameters.default

  def gen[T](g: Gen[T]): Option[T] = g(genParams)

  def currentSize(implicit s: SampleSize): SampleSize = s

  def fillN[T](t: ⇒ T)(implicit s: SampleSize): List[T] =
    (List fill s) { t }

  def sampleN[T](loopOver: ⇒ T)(implicit s: SampleSize): Seq[T] =
    for (_ ← 1 to s) yield loopOver

  // Final flatten will remove any None values in the generated set, but we
  // don't care because we don't use the Option feature of Gen.
  def sampleSet[T](g: Gen[T])(implicit s: SampleSize): Set[T] =
    sampleN(g(genParams))(s).toSet.flatten

  def beN(implicit s: SampleSize): Matcher[Int] =
    be_===(s: Int) ^^ { (c: Int) ⇒ c aka "sample size" }

  def haveSizeN[T](implicit s: SampleSize): Matcher[Traversable[T]] =
    haveSize(s)
}
