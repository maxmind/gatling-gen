package com.maxmind.gatling.gen.driver.gatling

import ammonite.ops._
import collection.immutable.HashMap
import java.nio.file.Files
import monocle.macros.Lenses
import scala.language.higherKinds
import scala.language.implicitConversions
import scalaz._
import Scalaz._

import com.maxmind.gatling.gen.Conf._
import com.maxmind.gatling.gen.Configurable
import com.maxmind.gatling.gen.GatlingGenConf.DriverConf.GatlingConf._
import com.maxmind.gatling.gen._
import com.maxmind.gatling.gen.driver.gatling.LauncherArgs._

/*
    The configuration required by the gatling.sh simulation launcher + some helpers to
    customize the config and start the launcher.

    Apply to get a gatling launcher process.

    LauncherArgs is concerned with driving the gatling simulation launcher and creating
    the environment it requires for a correct experiment launch:

      ▸ parameters: classpath, gatling home, base url, custom java properties, env vars,
        cli args. Everything required to build a launcher process

      ▸ gatling launcher process: build the process according to the cli args, env, and
        properties defined here

      ▸ configuration: all properties have reasonable defaults taken from config file,
        and there are lenses to customize properties without touching configuration.
        Custom config is saved to disk before launch, so launcher process can see any
        config changes that took place in the parent process.

      ▸ file system: create experiment dir structure, clean the tools dir
        (experiments/internal), create gatling mandated dir structure, copy launcher
        script from this jar into the tools bin dir
*/
@Lenses case class LauncherArgs(root: Conf = implicitly[Conf]) extends Configurable {
  lazy val conf: LauncherArgsConf = root.gatlinggen.driver.gatling.launcherArgs

  assume(
    { """^[0-9a-zA-Z._\-]+$""".r findFirstIn conf.simName }.isDefined,
    s"Invalid sim-name '${ conf.simName }', must be usable as a dir name"
  )

  /* T is the type of the process- the object created on CliSettings apply. */
  def apply[T](starter: CliProcessStarter[T]): T = {

    def createFiles() = {
      conf.internalDir ◃ { rm ! _ } ◃ { d ⇒ assert(!exists.!(d), { s"Can' rm '$d'" }) }

      for (d ← Seq(
        conf.expConfDir,
        conf.runnerExeDir,
        conf.stubClassesDir,
        conf.stubLibDir
      )) {
        mkdir ! d
        assert((stat ! d).isDir, { s"Can't create dir '$d'" })
      }

      conf.runnerExeFile ◃ {
        file ⇒ {
          (getClass getResourceAsStream ("/" ++ conf.runnerExeName)) ◃
            { stream ⇒ { Files copy (stream, file.toPath) } } ▹
            { stream ⇒ stream close () }
        }
      } ▹ { _ setExecutable true }

      root.setInExperiment save conf.expConfFile
    }

    starter ▹ {

      createFiles()
      println(
        (ls ! Path("/home/eilara/me/IdeaProjects/gatling-gen/experiments"))
          map { _.toString } mkString "\n"
      )

      CliSettings(

        args = Seq(
          conf.runnerExe.toString,
          "--output-name", conf.simName,
          "--results-folder", conf.expBaseDir.toString,
          "--simulation", conf.simClassName
        ),

        env = HashMap(
          "JAVA_CLASSPATH" → conf.jarFile.toString,
          // This way no gatling OS install is required, everything is in the one jar
          "GATLING_HOME" → conf.internalDir.toString,
          "GATLING_CONF" → conf.expConfDir.toString
        ),

        props = conf.props |+|
          HashMap(Conf.expPropertiesFileKey → conf.expConfFile.toString)
      ).apply
    }
  }

  def mkLens[T](f: LensBetween[LauncherArgsConf.type, GatlingGenConf, T]) =
    mkLensBetween(this, LauncherArgsConf, LauncherArgs.root ^|-> Conf.gatlinggen)(f)

  lazy val baseUrl      = mkLens { _._baseUrl }
  lazy val simClassName = mkLens { _._simClassName }
}

object LauncherArgs {

//  def apply(baseUrl: String): LauncherArgs =
//    LauncherArgs().baseUrl set baseUrl

  type CliArgs = Seq[String]
  type CliEnv = Map[String, String]
  type CliProps = Map[String, String]

  /* A function like Process.apply: all args required to start some process. */
  type CliProcessStarter[T] = (Seq[String], Map[String, String]) ⇒ T

  /* Type required for creating a CliSettings, lowest level before command line */
  case class CliParam(args: CliArgs, env: CliEnv, props: CliProps)

  /* Final type required for launching, props have been merged into env */
  case class CliFinalParam(args: CliArgs, env: CliEnv)

  /* Final low-level gatling launcher args- exact amount required to build a process. */
  case class CliSettings(
      args: CliArgs = Monoid[List[String]].zero,
      env: CliEnv = Monoid[CliProps].zero,
      props: CliProps = Monoid[CliProps].zero
  ) {

    /** T = type returned by CliSettings apply(CliProcessorStarter).
      * Probably a scala process (scala.sys.process.Process), or perhaps a test mock.
      */
    def apply[T](f: CliProcessStarter[T]): T =
      (
        env + ("JAVA_OPTS" → (props.toSeq map { case (k, v) ⇒ s"-D$k=$v" } mkString " "))
        ).toList ▹ { e ⇒ f(args, e.toMap[String, String])
      }
  }
}
