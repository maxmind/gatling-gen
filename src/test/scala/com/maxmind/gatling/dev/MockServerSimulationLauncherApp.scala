package com.maxmind.gatling.dev

import com.maxmind.gatling.app.Runner
import com.maxmind.gatling.test.MockServer

import scalaz.Scalaz._
import scalaz._

/**
  * Launches a simple gatling simulation on the mock server.
  */
object MockServerSimulationLauncherApp extends App {

  val simClassName = classOf[BasicSimulationExample].getCanonicalName

  println("# Launching mock server.")
  val server = MockServer()
  println(s"# Started $server.")

  println(s"# Launching gatling simulation $simClassName.")
  val (_, msg) = Runner(simClassName = simClassName) run server.uriString
  println(s"# Simulation $msg.")

  server stop ()
  println("Mock server stopped.")

}
