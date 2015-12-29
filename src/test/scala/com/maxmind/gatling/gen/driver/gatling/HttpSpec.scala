package com.maxmind.gatling.gen.driver.gatling

import collection.JavaConverters._
import collection.immutable.HashMap
import collection.mutable
import com.ning.http.client.Realm
import io.gatling.core.Predef._
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.validation
import io.gatling.http.Predef._
import io.gatling.http.config.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder
import io.gatling.http.util.HttpHelper
import java.util
import org.specs2.matcher.MatchResult
import scalaz._
import Scalaz._
import io.gatling.core.session.Expression
import io.gatling.core.session.Session
import spray.http.HttpMethod
import spray.http.HttpMethods

import com.maxmind.gatling.gen.test.BaseSpec
import com.maxmind.gatling.gen.test.GatlingHttpBaseSpec

class HttpSpec extends GatlingHttpBaseSpec {

  "Building HTTP requests using the gatling driver".title

  """⋅ Build then test an http request using the gatling http builder dsl
  ☛ Note how we describe an http request using the gatling builder methods
    Result of gatling req gen flow is a fully fleshed-out http request.""" >> {

    val builder: HttpBuilder = RequestMold(
      "SomeRequest": Name,
      "http://server:9999": BaseUrl,
      "/foo": Segment
    ) {
      (p, r) ⇒ (

      p.disableAutoReferer
        connection "close",
      r.disableFollowRedirect.ignoreDefaultChecks
        header (HttpHeaderNames.UserAgent, "uaZ")
        headers HashMap("Abc" → "123")
        basicAuth ("userX", "passwordY")

      )
    }

    ExpectRequest(

      expectMethod = HttpMethods.GET,
      expectRealm = HttpHelper buildBasicAuthRealm ("userX", "passwordY"),
      expectHeaders = HashMap(
        HttpHeaderNames.UserAgent → List("uaZ"),
        HttpHeaderNames.Connection → List("close"),
        "Abc" → List("123")
      )

    ) forBuilder builder
  }
}



