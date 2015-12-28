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

class ThirdPartySpec extends GatlingHttpTestHelper {

  """⋅ Build then test an http request using the gatling http builder dsl
  ☛ Note how we describe an http request using the gatling builder methods
    Result of gatling req gen flow is a fully fleshed-out http request.""" >> {

    ExpectRequest(
      expectMethod = HttpMethods.GET,
      expectRealm = HttpHelper buildBasicAuthRealm ("userX", "passwordY"),
      expectHeaders = HashMap(
        HttpHeaderNames.UserAgent → List("uaZ"),
        HttpHeaderNames.Connection → List("close"),
        "Abc" → List("123")
      )
    ) forBuilder RequestMold {
      case (p: HttpProtocolBuilder, r: HttpRequestBuilder) ⇒ (
        p.disableAutoReferer
          connection "close",
        r.disableFollowRedirect.ignoreDefaultChecks
          header (HttpHeaderNames.UserAgent, "uaZ")
          headers HashMap("Abc" → "123")
          basicAuth ("userX", "passwordY")
        )
    }
  }
}

abstract class GatlingHttpTestHelper extends BaseSpec {

  /* Required for setting configuration implicits. */
  GatlingConfiguration setUp ()

  /* Spec for an expected request, apply with an actual request to get a match result. */
  case class ExpectRequest(
      expectMethod: HttpMethod,
      expectRealm: Expression[Realm],
      expectHeaders: HeadersSpec
  ) {
    type ReqTest = Request ⇒ MatchResult[Any]

    val testRequest: ReqTest = { r ⇒
      val testMethod = r.getMethod aka "method" must_== expectMethod.value
      val testHeaders =
        convertHeadersFromJava(r.getHeaders).toMap aka "headers" must_== expectHeaders
      val testRealm = (r.getRealm.some >>= { Option(_) }, eval(expectRealm)) ▹ {
        case (actual: Option[Realm], expected: Option[Realm]) ⇒
          ((actual must beNone) and (expected must beNone)) or
            (actual.get.toString must_== expected.get.toString)
      }
      List(testMethod, testHeaders, testRealm) reduceLeft { _ and _ }
    }

    /* Materialize a request mold into request ready for the wire, then test it */
    def forBuilder(mold: RequestMold): MatchResult[Any] = {
      mold(
        http.baseURL("http://server:9999"), // protocol entry point
        http("someRequestName") get "/foo" // request entry point
      ) match {
        case (p, r) ⇒ (
          (r build (p.build, throttled = false)) ▹
            { mkReq ⇒ eval { s ⇒ mkReq build ("bar", s) } }
          ).get.ahcRequest
      }
    } ▹ { actual ⇒ testRequest(actual) }

    val aSession = { "baz" ++ (_: String) } ▹ { f ⇒ Session(f("session"), f("user")) }

    /* Eval a gatling expression into a string using the session */
    def eval[T](e: Expression[T]): Option[T] = e(aSession) ▹ {
      case validation.Success(t)     => t.some
      case validation.Failure(error) => None
    }

    def convertHeadersFromJava(j: AnyRef): mutable.Map[String, List[String]] = {
      type JavaList = util.List[String]
      def listMap(java: AnyRef) = java.asInstanceOf[util.Map[String, JavaList]].asScala
      def list(java: AnyRef) = java.asInstanceOf[JavaList].asScala
      listMap(j) map { case (k, v) ⇒ k → list(v).toList }
    }
  }
}

