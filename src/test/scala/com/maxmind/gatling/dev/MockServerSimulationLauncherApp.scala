package com.maxmind.gatling.dev

import scala.collection.immutable.HashMap
import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.simulation.{Runner, RunnerConfig}
import com.maxmind.gatling.simulation.BasicSimulationExample

/**
  * Launches a simple gatling simulation on the mock server.
  */
object MockServerSimulationLauncherApp extends App {

  val simClassName = classOf[BasicSimulationExample].getCanonicalName

  println("# Launching mock server.")
  val server = MockServer()
  println(s"# Started $server.")

  println(s"# Launching gatling simulation $simClassName.")
  val runner = Runner(RunnerConfig(
    props = HashMap("gatlingen.http.base" → server.uriString),
    simClassName = simClassName
  ))

  val (isOk, msg) = runner()
  println(s"# Simulation $msg.")

  server stop ()
  println("Mock server stopped.")
}
