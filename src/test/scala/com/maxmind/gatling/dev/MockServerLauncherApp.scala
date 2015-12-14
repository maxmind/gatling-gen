package com.maxmind.gatling.dev

import scala.io.StdIn
import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.dev.MockServer.MockServer

/** Mock server launcher for manual self-test outside of test env.
  */
object MockServerLauncherApp extends App {

  MockServer() runFor { (s: MockServer) ⇒
    println(s"# Started $s, hit <Enter> key to stop.")
    StdIn readLine ()
  }

  println("# Server stopped.")
}
