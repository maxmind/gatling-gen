package com.maxmind.gatling.gen.mock

import com.ning.http.client.ListenableFuture
import com.ning.http.client.Response
import org.specs2.matcher.Expectable
import org.specs2.matcher.MustMatchers
import scalaz._
import Scalaz._
import spray.http.StatusCode

/**
  * A result of an HTTP request by a mock server agent
  */
case class HttpResult(requestor: ListenableFuture[Response]) extends MustMatchers {

  lazy val resp       = requestor get ()
  lazy val bodyString = resp.getResponseBody
  lazy val stat       = StatusCode int2StatusCode resp.getStatusCode
  lazy val okMsg      = s"actual response '${ stat.toString }'"
  lazy val bodyMsg    = s"actual response body '$bodyString'"
  lazy val ok         = stat.isSuccess aka okMsg
  lazy val fail       = ok map { not _ }
  lazy val body       = bodyString aka bodyMsg

  /* Specs2 matcher checks request OK and lets you run a matcher on body contents */
  lazy val okAndBody: (Expectable[String]) =
    ok flatMap { _ ? body | ((null: String) aka s"Request failed: $okMsg, $bodyMsg") }
}
