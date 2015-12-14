package com.maxmind.gatling.gen.mock

import collection.mutable
import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.specification.ForEach
import scalaz._
import Scalaz._

import com.maxmind.gatling.gen.mock.Agent.AgentState

/**
  * A specs2 trait for specification classes that require a mock server
  */
trait ServerContext extends ForEach[Server] {

  lazy val openAgents = mutable.Set[Agent]()

  def foreach[R: AsResult](testThunk: Server => R): Result =
    AsResult { Server(testThunk) } ◃ { _ ⇒ openAgents foreach Agent.stop }

  def mkAgentState(server: Server): AgentState = AgentState init server
}

