package com.maxmind.gatling.gen.mock

import scalaz._
import Scalaz._
import org.specs2.execute.Result

import com.maxmind.gatling.gen.mock.Agent.AgentState
import com.maxmind.gatling.gen.test.BaseSpec
import com.maxmind.gatling.gen.test.SampleSize

class ServerContextSpec extends BaseSpec with ServerContext {

  implicit lazy val size: SampleSize = 5

  val pingEndpoint     : AgentState ⇒ AgentState = doGet("/ping", "pong")(_)
  val freshPingEndpoint: Server ⇒ AgentState     = mkAgentState(_: Server) ▹ pingEndpoint
  val quit             : AgentState ⇒ Result     = a ⇒ a.asResult

  "Endpoints under MockServer specs2 context".title

  "⋅ /ping → pong - basic example" >> {
    (server: Server) ⇒ (mkAgentState(server) doGet "/ping") {
      (r: HttpResult) ⇒ (r.ok must beTrue) and (r.body must_== "pong")
    } ▹ { _.asResult }
  }

  def doGet(uri: String, expect: String)(agent: AgentState): AgentState =
    (agent doGet uri) { _.okAndBody must_== expect }

  "⋅ /ping → pong, using test helpers" >> { (server: Server) ⇒
    mkAgentState(server) ▹ pingEndpoint ▹ quit
  }

  "⋅ N ping → pongs, N agents" >> { (server: Server) ⇒
    fillN { mkAgentState(server) ▹ pingEndpoint ▹ quit } foldMap identity[Result]
  }

  "⋅ N ping → pongs, 1 agent" >> {
    (server: Server) ⇒ fillN { pingEndpoint(_: AgentState) } ▹ {
      mkResultList ⇒ mkAgentState(server) ▹ {
        oldSt ⇒ (mkResultList foldl oldSt) { newSt ⇒ _ (newSt) } ▹ quit
      }
    }
  }

  "⋅ /404 → fail" >> {
    (server: Server) ⇒ mkAgentState(server) ▹
      { st ⇒ (st doGet "/foo") { _.ok must beFalse } } ▹ quit
  }

  "⋅ /parrot → request returned as string for debugging" >> {
    (server: Server) ⇒ (mkAgentState(server) doGet "/parrot") {
      r ⇒ r.okAndBody must contain("Host: localhost")
    } ▹ quit

  }

  "⋅ /parrot returns query parameters" >> {
    (server: Server) ⇒ (mkAgentState(server) doGet "/parrot?a=1&b=2") {
      r ⇒ r.okAndBody must contain("a=1&b=2")
    } ▹ quit
  }
}
