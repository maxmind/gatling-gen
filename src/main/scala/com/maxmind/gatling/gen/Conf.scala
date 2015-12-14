package com.maxmind.gatling.gen

import ammonite.ops.Path
import com.typesafe.config.ConfigFactory
import io.gatling.core.util.IO.withCloseable
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import monocle.macros.Lenses
import pureconfig._
import pureconfig.conf.typesafeConfigToConfig
import scala.language.higherKinds
import scala.language.implicitConversions
import scalaz._
import Scalaz._
import util.Failure
import util.Success

/**
  * Gatling-Gen config loader, saver, checker, trait, implicit, and top config object
  */
object Conf {

  @Lenses case class ConfigImpl(gatlinggen: GatlingGenConf)

  implicit val configuration: Conf = load()
  def apply(): Conf = configuration

  lazy val expPropertiesFileKey = "expPropertiesFile"

  /* If given a confFile property, load this config file with highest priority */
  def load(): Conf = (ConfigFactory load ()) ▹ { r ⇒
    val extraConf = System getProperty (expPropertiesFileKey, "")
    extraConf.isEmpty ? r | ((ConfigFactory parseFile new File(extraConf)) withFallback r)
  } ▹ {
    // Now load this raw config into the ConfigImpl app config tree
    rawConfig ⇒ {
      loadConfig[ConfigImpl](typesafeConfigToConfig(rawConfig)) match {
        case Failure(f)    => throw f
        case Success(conf) => conf
      }
    } ▹ { Conf(_) }
  }

  lazy val gatlinggen = Conf.impl ^|-> ConfigImpl.gatlinggen
}

@Lenses case class Conf(impl: Conf.ConfigImpl) {
  lazy val gatlinggen = impl.gatlinggen
  lazy val driver     = gatlinggen.driver
  lazy val dev        = gatlinggen.dev

  def save(p: Path)(implicit conv: ConfigConvert[Conf.ConfigImpl]): Unit = {

    withCloseable(
      new File(p.toString) ▹ { Files newOutputStream _.toPath } ▹ { new PrintStream(_) }
    ) {
      (stream: PrintStream) ⇒ {
        (conv to (impl, "")) foreach { case (k, v) ⇒ stream.println(s"$k=$v") }
      }
    }
  }

  lazy val _isInExperiment          = Conf.gatlinggen ^|-> GatlingGenConf.isInExperiment
  lazy val isInExperiment : Boolean = _isInExperiment get this
  lazy val setInExperiment: Conf    = (_isInExperiment set true) (this)
}

trait Configurable {

  /**
    * S = Source point of the lens
    * T = This, the Target point of the lens
    * R = Root point of the lens
    * V = final Value exiting the lens
    */
  type LensBetween[S, R, V] = S ⇒ monocle.PLens[R, R, V, V]

  def mkLensBetween[R, S, T, V](self: T, source: S, root: monocle.Lens[T, R])
    (f: LensBetween[S, R, V]) = LensAccess(self, root ^|-> f(source))

  case class LensAccess[T, V](on: T, lens: monocle.PLens[T, T, V, V]) {
    def get: V = lens get on
    def set(v: V): T = (lens set v) (on)
    def modify(f: V ⇒ V): T = (lens modify f) (on)
  }
}
