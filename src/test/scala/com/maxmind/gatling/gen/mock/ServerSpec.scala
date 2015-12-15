package com.maxmind.gatling.gen.mock

import scalaz._
import Scalaz._

import com.maxmind.gatling.gen.mock.Agent.AgentState
import com.maxmind.gatling.gen.test._

class ServerSpec extends BaseSpec {

  implicit lazy val size: SampleSize = 5

  "MockServer: examples and self-sanity".title

  "⋅ Basic example" >> {

    // Create a mock HTTP server and start listening
    Server { (server: Server) ⇒

      // Make a new agent, configured to request from the mock server
      val init: AgentState = AgentState init server

      // Send an HTTP GET through the agent, project to future assertion
      val afterPing: AgentState = (init doGet "/ping") {
        r ⇒ (r.ok must beTrue) and (r.body must_== "pong")
      }

      // Fold recorded matches into a single result
      afterPing.asResult
    }
  }

  "• Server control " >> {

    def findOpenPort() = Server findOpenPort ()

    def startStop(mkPort: ⇒ Int = findOpenPort()): Boolean = Server { _ ⇒ true }

    "⋅ OS reserves open ports requested for a bit so we have distinct port" >>
      { fillN { findOpenPort() }.distinct must haveSizeN }

    "⋅ Start/stop" >> { startStop() must beTrue }

    "⋅ Start/stop repeat" >> { fillN { startStop() } must beAllTrue }

    "⋅ Start/stop repeat on same port- shows ports are being freed" >>
      { findOpenPort() ▹ { p ⇒ this fillN { startStop(p) } must beAllTrue } }
  }
}
