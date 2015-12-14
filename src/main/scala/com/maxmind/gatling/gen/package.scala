package com.maxmind.gatling

import akka.util.Timeout
import ammonite.ops._
import java.io.File
import java.util.Date
import monocle.macros.Lenses
import pureconfig.StringConvert
import scala.concurrent.duration._
import scala.language.higherKinds
import scala.language.implicitConversions
import scala.util.Try
import scalaz._
import Scalaz._

/**
  * Gatling-gen config
  */
package object gen {

 @Lenses case class GatlingGenConf(
      dev: GatlingGenConf.DevConf,
      driver: GatlingGenConf.DriverConf,
      isInExperiment: Boolean
  )

  object GatlingGenConf {
    @Lenses case class DevConf(mockServer: DevConf.MockServerConf)

    object DevConf {

      @Lenses case class MockServerConf(host: String, timeout: Timeout)
    }

    @Lenses case class DriverConf(gatling: DriverConf.GatlingConf)

    object DriverConf {

      /* Lens path, parent to me, so that we always have: root conf -> child conf */
      lazy val path = GatlingGenConf.driver

      @Lenses case class GatlingConf(launcherArgs: GatlingConf.LauncherArgsConf)

      object GatlingConf {

        lazy val path = DriverConf.path ^|-> DriverConf.gatling

        @Lenses case class LauncherArgsConf(
            baseUrl: String,
            binDirRelPath: RelPath,
            internalDirRelPath: RelPath,
            jarFileRelPath: RelPath,
            props: Map[String, String],
            runnerExeName: String,
            expBaseDirRelPath: RelPath,
            expConfDirRelPath: RelPath,
            expConfName: String,
            simClassName: String,
            simNamePrefix: String,
            stubClassesDirRelPath: RelPath,
            stubLibDirRelPath: RelPath
        ) {
          lazy val pathDate             = new Date().toString replaceAll ("[ :]", "-")
          lazy val simName              = s"$simNamePrefix-$pathDate"
          lazy val jarFile       : Path = cwd / jarFileRelPath
          lazy val expBaseDir    : Path = cwd / expBaseDirRelPath
          lazy val internalDir   : Path = expBaseDir / internalDirRelPath
          lazy val expConfDir    : Path = internalDir / expConfDirRelPath
          lazy val expConfFile   : Path = expConfDir / (expConfName + ".properties")
          lazy val binDir        : Path = internalDir / binDirRelPath
          lazy val runnerExe     : Path = binDir / runnerExeName
          lazy val runnerExeDir  : Path = (runnerExe.segments dropRight 1) ▹ { Path(_) }
          lazy val runnerExeFile : File = new File(runnerExe.toString)
          lazy val stubClassesDir: Path = internalDir / stubClassesDirRelPath
          lazy val stubLibDir    : Path = internalDir / stubLibDirRelPath
        }

        object LauncherArgsConf extends Configurable {

          lazy val path = GatlingConf.path ^|-> GatlingConf.launcherArgs

          type Conf = GatlingGenConf.DriverConf.GatlingConf.LauncherArgsConf
          def mkLens[T](f: LensBetween[LauncherArgsConf.type, Conf, T]) =
            path ^|-> f(LauncherArgsConf)

          lazy val _baseUrl      = mkLens { _.baseUrl }
          lazy val _simClassName = mkLens { _.simClassName }
        }
      }
    }
  }

  implicit val deriveStringFromToPath = new StringConvert[Path] {
    override def from(s: String): Try[Path] = Try(Path(s))
    override def to(path: Path): String = path.toString
  }

  implicit val deriveStringFromToTimeout = new StringConvert[Timeout] {
    override def from(s: String): Try[Timeout] = {
      val re = """\d+\.((second(s?))|(milli(s?))|(millisecond(s?)))"""
      require(s matches re, s"Value '$s' must match '$re'")
      val (value :: unit :: Nil) = (s split '.').toList
      Try(value.toInt) map {
        v ⇒ (unit matches "second(s?)") ? v.seconds | v.milliseconds
      } map { Timeout(_) }
    }
    override def to(timeout: Timeout): String =
      timeout.duration ▹ { (t: FiniteDuration) ⇒
        val unit = t.unit
        val value = t.toUnit(unit).toInt.toString
        value ++ "." ++ unit.toString.toLowerCase
      }
  }
}
