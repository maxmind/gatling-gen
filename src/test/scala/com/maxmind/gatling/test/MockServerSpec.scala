package com.maxmind.gatling.test

import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.test.MockServer.HttpResult

class MockServerSpec extends BaseSpec {

  implicit lazy val size: SampleSize = 5

  "MockServer: examples and self-sanity".title

  "⋅ Basic example" >> {

    // Create a mock HTTP server and start listening.
    val server = MockServer()

    // Make a new agent, configured to request from the mock server.
    val agent: server.Agent = server mkAgent ()

    // Send an HTTP GET through the agent, we now have a future HTTP response.
    val result: HttpResult = agent doGet "/ping"

    // Assert a success response with body "pong" and save it.
    val matches =
      (result.ok must beTrue) and (result.body must_== "pong")

    // Stop mockver.
    server stop ()

    // A successful server stop is not our assertion, so recall "matches".
    matches
  }

  "• Server control" >> {

    lazy val fixedPort = freshPort()

    def freshPort = () ⇒ MockServer findOpenPort ()

    def startStop(mkPort: ⇒ Int = freshPort()) = MockServer() stop ()

    "⋅ OS reserves open ports requested for a bit so we have distinct port" >>
      { this.fillN { MockServer findOpenPort () }.distinct must haveSizeN }

    "⋅ Start/stop" >> startStop()

    "⋅ Start/stop repeat" >>
      { (this fillN { startStop() }) must contain(allOf(beTrue)) }

    "⋅ Start/stop repeat on same port- shows ports are being freed" >>
      { (this fillN { startStop(fixedPort) }) must contain(allOf(beTrue)) }
  }

  EndpointsAndAgentsSpec().tests

  case class EndpointsAndAgentsSpec() extends MockServerContext {

    def tests = {
      "• Endpoints under MockServer specs2 context" >> {

        "⋅ /ping → pong" >> { doGet("/ping").okAndBody must_== "pong" }

        "⋅ /404 → fail" >> { doGet("/foo").ok must beFalse }

        "⋅ /parrot → returns the request as string for debugging" >>
          { doGet("/parrot").okAndBody must contain("Host: localhost") }

        "⋅ /parrot returns query parameters" >>
          { doGet("/parrot?a=1&b=2").okAndBody must contain("a=1&b=2") }
      }
    }
  }
}
