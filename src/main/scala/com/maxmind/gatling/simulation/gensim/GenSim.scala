package com.maxmind.gatling.simulation.gensim

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scalaz.Scalaz._
import scalaz._

/*

A simulation that uses a ScalaCheck generator as a feeder, and is completely
parameterizable for all kinds of load and functional test scenarios.

This simulation looks for one system property: gatlinggen.conf. The file
defines the behavior of the GenSim simulation:

• gatlinggen.http.base     - String="http://localhost:80"
                             all URLs will be prefixed to this
• gatlinggen.http.name     - String="GenSim Scenario Request"
                             name of single http stream in single scenario
• gatlinggen.http.path     - String="/"
                             path of http requests
• gatlinggen.scenario.name - String="GenSim-Scenario"
                             name of single scenario that will be simulated
                             must be a valid POSIX path segment: [0-9a-ZA-Z._\-]
*/

class GenSim extends Simulation {

  lazy val conf = GenSimRunnerConf.simConf

  val httpBase     = conf.http.base
  val httpName     = conf.http.name
  val httpPath     = conf.http.path
  val scenarioName = conf.scenario.name
  val usersMax     = conf.scenario.users.max

  val scn = scenario(scenarioName) exec { http(httpName) get httpPath }

  setUp(scn inject atOnceUsers(usersMax) protocols (http baseURL httpBase))
}
