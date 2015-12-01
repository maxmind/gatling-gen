package com.maxmind.gatling.dev

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
  * A simple gatling simulation.
  */
class BasicSimulationExample extends Simulation {

  val base = System getProperty ("baseUrl", "http://localhost:80")

  val scn = scenario("a scenario") exec
    { http("a request") get "/ping" check (status is 200) }

  setUp(
    scn inject atOnceUsers(1)
      protocols (http baseURL base)
  )
}
