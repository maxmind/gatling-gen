package com.maxmind.gatling

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.matcher._
import org.specs2.specification.core.Fragments
import org.specs2.{ScalaCheck, Specification}

trait BaseSpec extends Specification with ScalaCheck with Matchers {

  val defaultSampleSize = SampleSize(100)

  def is = isolated ^ innerIs

  protected implicit def gen2Arb[T](g: Gen[T]): Arbitrary[T] = Arbitrary(g)

  protected def samples[T](
    g: Gen[T], size:SampleSize = defaultSampleSize): Seq[T] =
    size samples g(Gen.Parameters.default).get

  protected def distinctSamples[T](
    g: Gen[T], size:SampleSize = defaultSampleSize): Seq[T] =
    samples(g, size).distinct

  protected def distinctSamplesBig[T](g: Gen[T]): Seq[T] =
    distinctSamples(g, defaultSampleSize.incTenFold)

  protected def innerIs: Fragments
}

case class SampleSize(size: Int) {
  def samples[T](loopOver: ⇒ T): Seq[T] = for (_ ← 1 to size) yield loopOver

  def incTenFold: SampleSize = SampleSize(size * 10)
}


