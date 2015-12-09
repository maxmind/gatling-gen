package com.maxmind.gatling.dev

import akka.actor._
import akka.io.IO
import akka.pattern._
import akka.util.Timeout
import com.ning.http.client.{AsyncHttpClient, ListenableFuture, Response}
import io.gatling.core.util.IO.withCloseable
import java.net.ServerSocket
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scalaz.Scalaz._
import scalaz._
import spray.can.Http
import spray.http._
import spray.routing._

/**
  * A mock server for testing gatling scenarios.
  */
object MockServer {
  import org.specs2.matcher.MustMatchers

  val host = "localhost"

  implicit val timeout : Timeout        = 5.seconds
  implicit val duration: FiniteDuration = timeout.duration

  def apply(port: Int = findOpenPort()) = new MockServer(port) ◃ { _ start () }

  def findOpenPort(): Int = withCloseable(new ServerSocket(0)) { _ getLocalPort () }

  class MockServer(port: Int) {

    lazy implicit val sys = ActorSystem("mock-server-actor-system")

    lazy val openAgents = mutable.Set[Agent]()
    lazy val handler    = sys actorOf (Props[Handler], "mock-server-handler")
    lazy val bind       = IO(Http) ? (Http Bind (handler, host, port))
    lazy val uri        = (Uri() withHost host) withPort port
    lazy val uriString  = "http:" + uri.toString
    lazy val mkUri      = (uriSuffix: String) ⇒ s"$uriString$uriSuffix"

    def start() = Await ready (bind, duration)

    def mkAgent(): Agent = Agent() ◃ openAgents.add

    def stop(): Boolean = {
      openAgents foreach { _ stop () }
      sys ◃ { _ shutdown () } ◃ { _ awaitTermination duration }
      true // Thus allowing us as last expression in test.
    }

    override def toString = s"MockServer on $uriString"

    case class Agent() {
      implicit val ec = ExecutionContext.Implicits.global

      lazy val impl = new AsyncHttpClient()

      def stop(): Unit = impl close ()

      def doGet(path: String): HttpResult = {
        impl prepareGet mkUri(path) execute ()
      } ▹ HttpResult
    }
  }

  case class HttpResult(requestor: ListenableFuture[Response])
    extends MustMatchers {
    import org.specs2.matcher.Expectable

    lazy val resp       = requestor get ()
    lazy val bodyString = resp.getResponseBody
    lazy val stat       = StatusCode int2StatusCode resp.getStatusCode
    lazy val okMsg      = s"actual response '${ stat.toString }'"
    lazy val bodyMsg    = s"actual response body '$bodyString'"
    lazy val ok         = stat.isSuccess aka okMsg
    lazy val fail       = ok map { not _ }
    lazy val body       = bodyString aka bodyMsg

    lazy val okAndBody: (Expectable[String]) =
      ok flatMap { _ ? body | ((null: String) aka s"Request failed: $okMsg, $bodyMsg") }

    override def toString = resp.toString
  }

  class Handler extends HttpServiceActor {

    def onGet(p: String)(s: HttpRequest ⇒ String) =
      (path(p) & get & extract { _.request }) { r ⇒ complete { s(r) } }

    val ping = (_: Any) ⇒ "pong"

    val parrot = (r: HttpRequest) ⇒ List(
      r.method.toString ++ " " ++ r.uri.toString,
      r.headers ∘ { _.toString } mkString "\n",
      r.entity ▹ { e ⇒ e.isEmpty ? "" | e.asString }
    ) mkString "\n"

    def receive = runRoute { onGet("ping") { ping } ~ onGet("parrot") { parrot } }
  }
}

