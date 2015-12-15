package com.maxmind.gatling.gen.driver.gatling

import com.maxmind.gatling.gen.mock.Server
import com.maxmind.gatling.gen.mock.ServerContext
import com.maxmind.gatling.gen.test.BaseSpec

class LauncherSpec extends BaseSpec with ServerContext {

  // A bit heavy on the system starting all these processes at once
  sequential

  "Gatling sim launcher self-test".title

  def run() = { (server: Server) ⇒
    val args = LauncherArgs().baseUrl set server.baseUrl
    Launcher(args)() match {
      case (isOk: Boolean, msg: String) ⇒
        (isOk aka s"isOk,msg=$msg" must beTrue) and (msg must_== "OK")
    }
  }

  "⋅ Basic example" >> run()

  """⋅ Can repeat simulations because they run on VM-per-simulation
    Thanks to some singletons, Gatling promises to run once in each vm, no
    more. This shows we are running the simulation on a different vm.

  """ >> run()

  """⋅ And again
    Two simulations in same MockServerContext will not work, because complete reset of
    the Akka actor system is required, but runnning in a different vm, we can run three.

    """ >> run()
}
