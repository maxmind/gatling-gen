package com.maxmind.gatling.simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
  * A simple gatling simulation.
  */
class BasicSimulationExample extends Simulation {

  val base = System getProperty ("gatlinggen.http.base", "http://localhost:80")

  val scn = scenario("a scenario") exec
    { http("a request") get "/ping" check (status is 200) }

  setUp( scn inject atOnceUsers(1) protocols (http baseURL base) )
}
