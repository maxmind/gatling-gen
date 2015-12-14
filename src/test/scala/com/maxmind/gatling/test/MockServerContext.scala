package com.maxmind.gatling.test

import org.specs2.execute.{AsResult, Result}
import org.specs2.specification.AroundEach
import scalaz.Scalaz._
import scalaz._

import com.maxmind.gatling.dev.MockServer

/**
  * A specs2 trait for specification classes that require a mock server.
  */
trait MockServerContext extends AroundEach {

  lazy val server      = MockServer()
  lazy val agent       = server mkAgent ()
  lazy val mockBaseUrl = server.uriString

  def doGet(path: String) = agent doGet path

  def around[R: AsResult](r: => R): Result = server runFor { AsResult(r) }
}

