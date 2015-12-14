package com.maxmind.gatling.simulation.runner

import ammonite.ops._
import collection.immutable.HashMap
import java.util.Date
import scalaz.Scalaz._
import scalaz._

/**
  * The configuration required by the gatling.sh simulation launcher, a part of the global
  * gatlinggen config.
  */

object ExeConf {
  type CliArgs = Seq[String]
  type CliEnv = Seq[(String, String)]

  lazy val stubDirClassesRel: RelPath = 'target / "test-classes"
  lazy val stubDirLibRel    : RelPath = 'lib

  lazy val pwd              = cwd
  lazy val baseUrlDef       = "http://localhost:80"
  lazy val propsDef         = new HashMap[String, String]()
  lazy val jarFileDef       = pwd / 'target / "scala-2.11" / "gatlinggen.jar"
  lazy val outDirSimResults = pwd / "sim-results"
  lazy val outDirTmp        = Path(Path.makeTmp)
  lazy val outDirDef        = (exists ! outDirSimResults) ? outDirSimResults | outDirTmp
  lazy val runnerShDef      = pwd / 'dev / "gatling.sh"
  lazy val pathDate         = new Date().toString replaceAll ("[ :]", "-")
  lazy val simNameDef       = s"gatlilng-sim-anon-$pathDate"
  lazy val simDescDef       = s"$simNameDef-description"
}

class ExeConf(
    simClassName: String,
    baseUrl: String = ExeConf.baseUrlDef,
    props: Map[String, String] = ExeConf.propsDef,
    jarFile: Path = ExeConf.jarFileDef,
    outDir: Path = ExeConf.outDirDef,
    runnerSh: Path = ExeConf.runnerShDef,
    simName: String = ExeConf.simNameDef,
    simDesc: String = ExeConf.simDescDef
) {
  import ExeConf.{CliArgs, CliEnv}

  assume(
    { """^[0-9a-zA-Z._\-]+$""".r findFirstIn simName }.isDefined,
    s"Invalid sim-name '$simName'"
  )

  // gatling -h describes all CLI args listed here.
  lazy val asArgs: CliArgs = Seq(
    runnerSh.toString,
    "--output-name", simName,
    "--results-folder", outDir.toString,
    "--run-description", simDesc,
    "--simulation", simClassName.toString
  )

  lazy val asEnv: CliEnv = {

    { props |+| HashMap("baseUrl" → baseUrl) } map
      { case (k: String, v: String) ⇒ s"-D$k=$v" }

  } ▹ { (opts: Iterable[String]) ⇒
    Seq(
      "JAVA_CLASSPATH" → jarFile.toString,
      "GATLING_HOME" → outDir.toString,
      "JAVA_OPTS" → (opts mkString " ")
    )
  }

  def asSettings: (CliArgs, CliEnv) = (asArgs, asEnv)

  def prepareOutDir(): Unit =
    for (d ← Seq(
      ExeConf.stubDirClassesRel,
      ExeConf.stubDirLibRel)
    ) (outDir / d) ◃ { mkdir ! _ } ◃ { d ⇒ assert((stat ! d).isDir, { s"No dir $d" }) }

  override def toString: String =
    (asArgs, asEnv map { case (k, v) ⇒ s"$k=$v" }) ▹
      { case (args, env) ⇒ s"${ args mkString " " }(${ env mkString " " })" }
}
