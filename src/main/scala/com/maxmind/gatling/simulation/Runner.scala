package com.maxmind.gatling.simulation

import ammonite.ops._
import io.gatling.app.GatlingStatusCodes._
import java.util.Date
import scala.collection.immutable.HashMap
import scala.sys.process._
import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.simulation.RunnerConfig.{Quiet, Verbose, Verbosity}

/**
  * A gatling simulation runner - launch in new process, as Gatling docs command.
  */
object Runner {
  type CliArgs = Seq[String]
  type CliEnv = Seq[(String, String)]

  def apply(conf: RunnerConfig) = conf mkRunner ()
}

class Runner(conf: RunnerConfig) {
  import Runner.CliArgs

  def argsErr(args: CliArgs) = "Invalid args: " ++ args.mkString("(", ", ", ")")

  def apply(): (Boolean, String) = {
    conf() ▹ { case (args, env) ⇒
      import io.gatling.app.GatlingStatusCodes
      Process(args, None, env: _*).! match {
        case GatlingStatusCodes.Success ⇒ (true, "OK")
        case AssertionsFailed           ⇒ (false, "Fail: simulation assertion")
        case InvalidArguments           ⇒ (false, argsErr(args))
      }
    }
  }
}

object RunnerConfig {

  sealed trait Verbosity
  case object Quiet extends Verbosity
  case object Verbose extends Verbosity

  lazy val stubDirClassesRel: RelPath = 'target / "test-classes"
  lazy val stubDirLibRel    : RelPath = 'lib

  lazy val pwd              = cwd
  lazy val propsDef         = new HashMap[String, String]()
  lazy val jarFileDef       = pwd / 'target / "scala-2.11" / "gatlinggen.jar"
  lazy val outDirSimResults = pwd / "sim-results"
  lazy val outDirTmp        = Path(Path.makeTmp)
  lazy val outDirDef        = (exists ! outDirSimResults) ? outDirSimResults | outDirTmp
  lazy val runnerShDef      = pwd / 'dev / "gatling.sh"
  lazy val pathDate         = new Date().toString replaceAll ("[ :]", "-")
  lazy val simNameDef       = s"gatlilng-sim-anon-$pathDate"
  lazy val simDescDef       = s"$simNameDef-description"
  lazy val verbosityDef     = Verbose
}

case class RunnerConfig(
    simClassName: String,
    props: Map[String, String] = RunnerConfig.propsDef,
    jarFile: Path = RunnerConfig.jarFileDef,
    outDir: Path = RunnerConfig.outDirDef,
    runnerSh: Path = RunnerConfig.runnerShDef,
    simName: String = RunnerConfig.simNameDef,
    simDesc: String = RunnerConfig.simDescDef,
    verbosity: Verbosity = RunnerConfig.verbosityDef) {
  import Runner.{CliArgs, CliEnv}

  lazy val isQuiet = verbosity match {
    case Quiet   ⇒ true
    case Verbose ⇒ false
  }

  // gatling -h describes all CLI args listed here.
  lazy val asArgs: CliArgs = Seq(
    runnerSh.toString,
    "--output-name", simName,
    "--results-folder", outDir.toString,
    "--run-description", simDesc,
    "--simulation", simClassName.toString
  )

  lazy val asEnv: CliEnv = Seq(
    "JAVA_CLASSPATH" → jarFile.toString,
    "GATLING_HOME" → outDir.toString,
    "JAVA_OPTS" → (props map { case (k: String, v: String) ⇒ s"-D$k=$v" }).mkString(" ")
  )

  def apply(): (CliArgs, CliEnv) = {
    assume(
      { """^[0-9a-zA-Z._\-]+$""".r findFirstIn simName }.isDefined,
      s"Invalid sim-name '$simName'"
    )
    (asArgs, asEnv) ◃ { case (args, env) ⇒
      if (!isQuiet) println(
        "# Running gatling:\n# \t" ++ args.mkString(" ") ++ s"\n# \t" ++ env.mkString(" ")
      )
    }
  }

  def mkRunner(): Runner = new Runner(this ◃ { _ prepareOutDir () })

  def prepareOutDir(): Unit =
    for (d ← Seq(
      RunnerConfig.stubDirClassesRel,
      RunnerConfig.stubDirLibRel)
    ) (outDir / d) ◃ { mkdir ! _ } ◃ { d ⇒ assert((stat ! d).isDir, { s"No dir $d" }) }
}

