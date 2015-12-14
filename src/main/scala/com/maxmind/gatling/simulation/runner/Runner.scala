package com.maxmind.gatling.simulation.runner

import ammonite.ops._
import io.gatling.app.GatlingStatusCodes
import io.gatling.app.GatlingStatusCodes._
import java.util.Date
import scala.collection.immutable.HashMap
import scala.sys.process._
import scalaz.Scalaz._
import scalaz._

/**
  * A gatling simulation runner - launch in new process, as Gatling docs command.
  */
object Runner {
  type CliArgs = Seq[String]
  type CliEnv = Seq[(String, String)]

  def apply(conf: RunnerConf) = conf()
}

class Runner(conf: RunnerConf) {
  import Runner.CliArgs

  def argsErr(args: CliArgs) = "Invalid args: " ++ args.mkString("(", ", ", ")")

  def apply(): (Boolean, String) = {
    { conf asSettings () } ▹ { case (args, env) ⇒
      Process(args, None, env: _*).! match {
        case GatlingStatusCodes.Success ⇒ (true, "OK")
        case AssertionsFailed           ⇒ (false, "Fail: simulation assertion")
        case InvalidArguments           ⇒ (false, argsErr(args))
      }
    }
  }
}

object RunnerConf {

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

case class RunnerConf(
    simClassName: String,
    baseUrl: String = RunnerConf.baseUrlDef,
    props: Map[String, String] = RunnerConf.propsDef,
    jarFile: Path = RunnerConf.jarFileDef,
    outDir: Path = RunnerConf.outDirDef,
    runnerSh: Path = RunnerConf.runnerShDef,
    simName: String = RunnerConf.simNameDef,
    simDesc: String = RunnerConf.simDescDef
){
  import Runner.{CliArgs, CliEnv}

  def asSettings(): (CliArgs, CliEnv) = {
    assume(
      { """^[0-9a-zA-Z._\-]+$""".r findFirstIn simName }.isDefined,
      s"Invalid sim-name '$simName'"
    )
    // gatling -h describes all CLI args listed here.
    val asArgs: CliArgs = Seq(
      runnerSh.toString,
      "--output-name", simName,
      "--results-folder", outDir.toString,
      "--run-description", simDesc,
      "--simulation", simClassName.toString
    )

    val asEnv: CliEnv = {

      { props |+| HashMap("baseUrl" → baseUrl) } map
        { case (k: String, v: String) ⇒ s"-D$k=$v" }

    } ▹ { (opts: Iterable[String]) ⇒
      Seq("JAVA_CLASSPATH" → jarFile.toString,
        "GATLING_HOME" → outDir.toString,
        "JAVA_OPTS" → (opts mkString "")
      )
    }

    (asArgs, asEnv) ◃ { case (args, env) ⇒
      println(
        "# Running gatling:\n# \t" ++ args.mkString(" ") ++ s"\n# \t" ++ env.mkString(" ")
      )
    }
  }

  def apply(): Runner = new Runner(this ◃ { _ prepareOutDir () })

  def prepareOutDir(): Unit =
    for (d ← Seq(
      RunnerConf.stubDirClassesRel,
      RunnerConf.stubDirLibRel)
    ) (outDir / d) ◃ { mkdir ! _ } ◃ { d ⇒ assert((stat ! d).isDir, { s"No dir $d" }) }
}

