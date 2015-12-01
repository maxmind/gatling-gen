package com.maxmind.gatling.app

import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.dev.BasicSimulationExample
import com.maxmind.gatling.test.{BaseSpec, MockServerContext, SampleSize}

class RunnerSpec extends BaseSpec with MockServerContext {

  implicit lazy val size: SampleSize = 5

  "Gatling runner self-test".title

  lazy val runner = Runner(
    simClassName = classOf[BasicSimulationExample].getCanonicalName
  )

  def run() = { runner run mockBaseUrl } |>
    { case (isOk, msg) ⇒ (isOk must beTrue) and (msg must_== "OK") }

  "⋅ Basic example" >> run()

}
