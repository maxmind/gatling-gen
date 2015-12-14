package com.maxmind.gatling.dev

import scala.collection.immutable.HashMap
import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.dev.MockServer.MockServer
import com.maxmind.gatling.simulation.BasicSimulationExample
import com.maxmind.gatling.simulation.runner.RunnerConf

/**
  * Launches a simple gatling simulation on the mock server.
  */
object MockServerSimulationLauncherApp extends App {

  val simClassName = classOf[BasicSimulationExample].getCanonicalName

  println("# Launching mock server.")

  val server = MockServer() runFor { (s: MockServer) ⇒
    println(s"# Started $s, launching gatling simulation $simClassName.")

    val (isOk, msg) = RunnerConf(
      props = HashMap("gatlingen.http.base" → s.uriString),
      simClassName = simClassName
    )()()

    println(s"# Simulation $msg.")
  }

  println("Mock server stopped.")
}
