package com.maxmind.gatling.gen.driver.gatling

import com.maxmind.gatling.gen.Conf
import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
  * A simple gatling simulation that sends one request and stops
  */
class OneRequestGatlingSim extends Simulation {

  val base = Conf().driver.gatling.launcherArgs.baseUrl

  val scn = scenario("a scenario") exec
    { http("a request") get "/ping" check (status is 200) }

  setUp(scn inject atOnceUsers(1) protocols (http baseURL base))
}
