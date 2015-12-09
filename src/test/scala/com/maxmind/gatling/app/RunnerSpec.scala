package com.maxmind.gatling.app

import ammonite.ops._

import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.dev.BasicSimulationExample
import com.maxmind.gatling.test.{BaseSpec, MockServerContext, SampleSize}
import com.maxmind.gatling.app.RunnerConfig.Verbose

class RunnerSpec extends BaseSpec with MockServerContext {

  implicit lazy val size: SampleSize = 5

  "Gatling runner self-test".title

  lazy val simClassName = classOf[BasicSimulationExample].getCanonicalName

  lazy val runner: Runner = Runner(RunnerConfig(
    simClassName = simClassName,

    baseUrl = mockBaseUrl,
    outDir = Path(Path.makeTmp),
    verbosity = Verbose
  ))

  def run() = {
    val (isOk, msg) = runner run mockBaseUrl.some
    (isOk must beTrue) and (msg must_== "OK")
  }

  "⋅ Basic example" >> run()
}
