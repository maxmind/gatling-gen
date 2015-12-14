package com.maxmind.gatling.gen.dev.tools

import com.maxmind.gatling.gen.driver.gatling.Launcher
import com.maxmind.gatling.gen.driver.gatling.LauncherArgs
import com.maxmind.gatling.gen.mock.Server

/**
  * Launches a simple gatling simulation on the mock server
  */
object MockServerSimulationLauncherApp extends App {

  println("# Launching mock server.")

  val server = Server { (s: Server) ⇒
    println(s"# Started $s, launching gatling simulation.")

    val (isOk, msg) = Launcher(LauncherArgs().baseUrl set s.baseUrl)()

    println(s"# Simulation $msg.")
  }

  println("Mock server stopped.")
}
