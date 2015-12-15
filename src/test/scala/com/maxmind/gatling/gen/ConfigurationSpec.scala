package com.maxmind.gatling.gen

import ammonite.ops._
import scalaz._
import Scalaz._

import com.maxmind.gatling.gen.GatlingGenConf.DriverConf.GatlingConf._
import com.maxmind.gatling.gen.test.BaseSpec

class ConfigurationSpec extends BaseSpec {

  "Configuration loads config files".title

  "• reference.conf" >> {

    val iut: GatlingGenConf = (Conf load ()).gatlinggen
    val mockServer = iut.dev.mockServer
    val launcherArgs = iut.driver.gatling.launcherArgs

    "⋅ String" >> { mockServer.host must_== "localhost" }

    "⋅ Timeout" >> { mockServer.timeout.toString must_== "Timeout(5 seconds)" }

    "⋅ RelPath" >>
      { launcherArgs.jarFileRelPath.toString must_== "target/scala-2.11/gatlinggen.jar" }

    "⋅ Path" >>
      { launcherArgs.expBaseDir.toString() must_== (cwd / "experiments").toString }

    "⋅ Lens get" >> { (LauncherArgsConf._simClassName get iut) must contain("gatling") }
  }
}
