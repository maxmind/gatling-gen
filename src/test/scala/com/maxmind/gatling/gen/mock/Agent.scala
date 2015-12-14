package com.maxmind.gatling.gen.mock

import com.ning.http.client._
import concurrent.ExecutionContext
import org.specs2.execute.Result
import org.specs2.matcher._
import scalaz._
import Scalaz._

/**
  * An agent for testing the mock server
  */
case class Agent(impl: AsyncHttpClient, mkUrl: Agent.UrlMaker) {
  implicit val ec = ExecutionContext.Implicits.global

  /* Async GET request on a uri, blocks only when asserting on the HttpResult */
  val doGet: String ⇒ (Agent, HttpResult) =
    uri ⇒ { impl prepareGet mkUrl(uri) execute () } ▹ HttpResult.apply ▹ { copy() → _ }

  def stop(): Unit = impl close ()
}

object Agent {
  type Specs2Match = MatchResult[Any]
  type ResultList = IList[() ⇒ MatchResult[Any]]
  type UrlMaker = String ⇒ String

  val stop: Agent ⇒ Unit = _ stop ()

  /* Create a new agent, on a mkUrl baseUrl maker, and with a new async http client */
  def apply(mkUrl: UrlMaker): Agent = Agent(new AsyncHttpClient(), mkUrl)

  case class AgentState(agent: Agent, results: ResultList) {

    def doGet(uri: String)(matcher: HttpResult ⇒ Specs2Match): AgentState =
      (agent doGet uri) ▹ { case (a: Agent, h: HttpResult) ⇒
        (results :+ { () ⇒ matcher(h) }) ▹ { AgentState(a, _) }
      }

    def withResults(rs: ResultList): AgentState = copy(agent copy (), rs)

    def asResult: Result = results foldMap { r ⇒ r().toResult }
  }

  object AgentState {

    /* An initial AgentState ready for 1st request */
    val init: Server ⇒ AgentState = { (s: Server) ⇒ Agent apply s.mkUrl } ∘
      { _ → ∅[ResultList] } ∘ { apply _ }.tupled
  }
}

