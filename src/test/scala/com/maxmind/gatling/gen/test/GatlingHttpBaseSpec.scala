package com.maxmind.gatling.gen.test

import collection.mutable
import com.ning.http.client.Realm
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session._
import io.gatling.core.validation
import io.gatling.http.Predef._
import java.util
import org.specs2.matcher.MatchResult
import spray.http.HttpMethod
import collection.JavaConverters._
import scalaz._
import Scalaz._

import com.maxmind.gatling.gen.driver.gatling.HttpBuilder
import com.maxmind.gatling.gen.driver.gatling._

/**
  * Base class for testing gatling level http building
  */
abstract class GatlingHttpBaseSpec extends BaseSpec {

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
    def forBuilder(builder: HttpBuilder): MatchResult[Any] = {
      builder match {
        case HttpBuilder(p, r) ⇒ {
          (r build (p.build, throttled = false)) ▹
            { req ⇒ eval { s ⇒ req build ("bar", s) } }
        }.get.ahcRequest
      }
    } ▹ testRequest
  }

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
