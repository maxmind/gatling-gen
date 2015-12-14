package com.maxmind.gatling.simulation

import ammonite.ops._
import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.test.{BaseSpec, MockServerContext}
import runner.{Runner, RunnerConf}

class RunnerSpec extends BaseSpec with MockServerContext {

  import scala.collection.immutable.HashMap

  "Gatling runner self-test".title

  lazy val simClassName = classOf[BasicSimulationExample].getCanonicalName

  lazy val runner: Runner = RunnerConf(
    outDir = Path(Path.makeTmp),
    props = HashMap("gatlinggen.http.base" → mockBaseUrl),
    simClassName = simClassName
  )()

  // Project the BasicSimulationExample onto a specs2 assertion.
  def run() = runner() match {
    case (isOk: Boolean, msg: String) ⇒ (isOk must beTrue) and (msg must_== "OK")
  }

  "⋅ Basic example" >> run()

  """⋅ Can repeat simulations because they run on VM-per-simulation
    Thanks to lots of vars and singletons, Gatling promises to run once in each VM, no
    more. This shows we re running the simulation on a different VM.

  """ >> run()

  """⋅ And again
    Two simulations in same MockServerContext will not work, because complete reset of
    the Akka actor system is required.

    """ >> run()
}
