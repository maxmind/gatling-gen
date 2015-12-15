package com.maxmind.gatling.gen.dev.tools

import scala.io.StdIn

import com.maxmind.gatling.gen.mock.Server

/** Mock server launcher for manual self-test outside of test env
  */
object MockServerLauncherApp extends App {

  Server { (s: Server) ⇒
    println(s"# Started $s, hit <Enter> key to stop.")
    StdIn readLine ()
  }

  println("# Server stopped.")
}
