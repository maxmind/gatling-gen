package com.maxmind.gatling.gen.driver.gatling

import io.gatling.app.GatlingStatusCodes
import io.gatling.app.GatlingStatusCodes._
import java.io.File
import monocle.macros.Lenses
import scala.language.higherKinds
import scala.language.implicitConversions
import scala.sys.process._
import scalaz._
import Scalaz._

import com.maxmind.gatling.gen.driver.gatling.LauncherArgs._

/**
  * A gatling simulation runner - launch in new process, as Gatling docs command us
  */
@Lenses case class Launcher(launcherArgs: LauncherArgs) {

  def apply(): (Boolean, String) = {

    val starter: CliProcessStarter[ProcessBuilder] = (args, env) ⇒ Process(
      args,
      none[File],
      env.toList: _*
    )

    starter ▹ { launcherArgs(_).! } match {
      case GatlingStatusCodes.Success ⇒ (true, "OK")
      case AssertionsFailed           ⇒ (false, "Fail: simulation assertion")
      case InvalidArguments           ⇒ (false, "Invalid args")
    }
  }
}
