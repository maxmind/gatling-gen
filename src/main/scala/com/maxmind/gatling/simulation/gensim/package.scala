package com.maxmind.gatling.simulation

import ammonite.ops._
import pureconfig.StringConvert
import scala.util.Try

/**
  * Configuration for GenSim parametrized simulation.
  */
package object gensim {

  case class Config(gatlinggen: GatlinggenConfig)

  case class GatlinggenConfig(http: HttpConfig, scenario: ScenarioConfig)

  case class HttpConfig(base: String, name: String, path: String)

  case class ScenarioConfig(name: String, desc: String, users: ScenarioUsersConfig)

  case class ScenarioUsersConfig(max: Int)

  implicit val deriveStringFromToPath = new StringConvert[Path] {
    override def from(str: String): Try[Path] = Try(Path(str))
    override def to(path: Path): String = path.toString
  }
}

