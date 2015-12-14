package com.maxmind.gatling.simulation.gensim

import caseapp._
import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.dev.MockServer

/**
  * Launches the GenSim simulation on a given configuration file.
  *
  * For testing, you can set the -m flag. If set, mock server will be started
  * before the simulation, and stopped when the simulation completes.
  */

@ProgName("gensim-launcher")
case class GenSimLauncherAppOptions(

    @ExtraName("c") @HelpMessage("Simulation config file name, e.g. 'myapp'")
    confFileName: String,

    @ExtraName("u") @HelpMessage("Base url, if not using mock server")
    baseUrl: String,

    @ExtraName("n") @HelpMessage("Canonical simulation class name")
    simClassName: String = classOf[GenSim].getCanonicalName,

    @ExtraName("m") @HelpMessage("Start mock server for self-test simulations")
    shouldStartMockServer: Boolean = false

) extends App {

  lazy val runner = GenSimRunner(GenSimRunnerConf(
    confFileName = confFileName,
    baseUrl = baseUrl,
    simClassName = simClassName
  ))

  val (isOk: Boolean, msg: String) =
    shouldStartMockServer ? { MockServer() runFor { runner() } } | runner()

  println(s"# Simulation $msg.")
  sys.exit(isOk ? 0 | 1)
}

object GenSimLauncherApp extends AppOf[GenSimLauncherAppOptions]

