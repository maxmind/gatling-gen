package com.maxmind.gatling.app

import java.util.Date

import ammonite.ops._

import scala.sys.process._

import io.gatling.app.GatlingStatusCodes

import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.app.RunnerConfig.{Quiet, Verbose, Verbosity}

/**
  * A gatling simulation runner.
  *
  * It is not trivial because gatling requires a results dir, and because
  * each simulation requires a fresh JVM so cannot be run programmatically.
  * Each simulation must be started in a new Java OS process.
  */
object Runner {
  def apply(conf: RunnerConfig): Runner = {
    conf prepareOutDir ()
    new Runner(conf)
  }
}

class Runner(conf: RunnerConfig) {

  def run(baseUrl: Option[String] = None): (Boolean, String) = {

    val fullConf = baseUrl.isDefined ? (conf withBaseUrl baseUrl.get) | conf
    val (args, env) = (conf.asArgs, conf.asEnv)

    conf log "Running gatling CLI:"
    conf log s"\t${ args mkString " " }"
    conf log s"\t${ env mkString " " }"

    val process = Process(args, None, env: _*)

    process.! match {

      case GatlingStatusCodes.Success =>
        (true, "OK")

      case GatlingStatusCodes.AssertionsFailed =>
        (false, "Fail: simulation assertion")

      case GatlingStatusCodes.InvalidArguments => (
        false,
        "Fail: invalid arguments [" ++ args.mkString("(", ", ", ")") ++ "]"
        )
    }
  }
}

object RunnerConfig {

  sealed trait Verbosity
  case object Quiet extends Verbosity
  case object Verbose extends Verbosity

  lazy val stubDirClassesRel: RelPath = 'target / "test-classes"
  lazy val stubDirLibRel: RelPath = 'lib

  lazy val pwd          = cwd
  lazy val baseUrl      = "http://localhost:80"
  lazy val jarFileDef   = pwd / 'target / "scala-2.11" / "gatlinggen.jar"
  lazy val outDirOptA   = pwd / "sim-results"
  lazy val outDirOptB   = Path(Path.makeTmp)
  lazy val outDirDef    = (exists ! outDirOptA) ? outDirOptA | outDirOptB
  lazy val runnerShDef  = pwd / 'dev / "gatling.sh"
  lazy val simNameDef   = "anonymous simulation " ++ new Date().toString
  lazy val simDescDef   = simNameDef ++ " description"
  lazy val verbosityDef = Verbose
}

case class RunnerConfig(
  simClassName: String,

  baseUrl: String = RunnerConfig.baseUrl,
  jarFile: Path = RunnerConfig.jarFileDef,
  outDir: Path = RunnerConfig.outDirDef,
  runnerSh: Path = RunnerConfig.runnerShDef,
  simName: String = RunnerConfig.simNameDef,
  simDesc: String = RunnerConfig.simDescDef,
  verbosity: Verbosity = RunnerConfig.verbosityDef
) {

  lazy val isQuiet = verbosity match {
    case Quiet   ⇒ true
    case Verbose ⇒ false
  }

  def withBaseUrl(u: String): RunnerConfig = this copy (baseUrl = u)

  def prepareOutDir(): Unit = {
    mkdir ! outDir
    assert((stat ! outDir).isDir, { s"There is no dir at $outDir" })

    // Used as Gatling home dir, so need to create some fake dirs or else the
    // gateling file searching code will throw.
    for (stubDirRel ← Seq(
      RunnerConfig.stubDirClassesRel,
      RunnerConfig.stubDirLibRel
    )) { { outDir / stubDirRel } <| { mkdir ! (_: Path) } } <| {
       (d: Path) ⇒ assert( (stat ! d).isDir, { s"There is no dir at $d" })
     }
    }

  lazy val asArgs: Seq[String] = Seq(
    runnerSh.toString,
    "--output-name", simName,
    "--results-folder", outDir.toString,
    "--run-description", simDesc,
    "--simulation", simClassName.toString
  ) map { _.replace(' ', '_') } map { _.replace(':', '-') }

  lazy val asEnv: Seq[(String, String)] = Seq(
    "JAVA_CLASSPATH" → jarFile.toString,
    "JAVA_OPTS" → ("-DbaseUrl=" ++ baseUrl),
    "GATLING_HOME" → outDir.toString
  )

  def log(msg: String): Unit = if (!isQuiet) println("# " ++ msg)
}


