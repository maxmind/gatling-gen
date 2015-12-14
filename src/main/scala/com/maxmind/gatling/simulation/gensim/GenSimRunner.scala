package com.maxmind.gatling.simulation.gensim

import ammonite.ops._
import pureconfig._
import scala.collection.immutable.HashMap
import scala.util.{Failure, Success}
import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.simulation.{RunnerConfig, gensim}

/**
  * A simulation runner for the GenSim parameterized simulation.
  */

object GenSimRunner {
  def apply(conf: GenSimRunnerConf) = conf mkRunner ()
}

class GenSimRunner(conf: GenSimRunnerConf) {
  lazy val runner = conf.runnerConf mkRunner ()

  def apply(): (Boolean, String) = runner()
}

object GenSimRunnerConf {

  lazy val simConf = System.getProperty("gatlinggen.conf", "") ◃
    { f ⇒ assume(!f.isEmpty, "Cannot find system property gatlinggen.conf") } ▹ get

  def get(confFileName: String) = (loadConfig[gensim.Config](confFileName) match {
    case Failure(f)    => throw f
    case Success(conf) => conf
  }).gatlinggen
}

case class GenSimRunnerConf(
    confFileName: String,
    baseUrl: String = "http://localhost:80",
    outDir: Path = RunnerConfig.outDirSimResults,
    simClassName: String = classOf[GenSim].getCanonicalName,
    simName: String = "gensim-anon",
    simDesc: String = s"GenSim bootstrap via GenSimRunner"
) {

  lazy val runnerConf = RunnerConfig(
    simClassName = simClassName,
    outDir = outDir,
    props = HashMap("gatlinggen.http.base" → baseUrl),
    simName = simName,
    simDesc = simDesc
  )

  def mkRunner(): GenSimRunner = new GenSimRunner(this)
}
