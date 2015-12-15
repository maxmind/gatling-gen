package com.maxmind.gatling.gen.mock

import akka.actor._
import akka.io.IO
import akka.pattern._
import akka.util.Timeout
import io.gatling.core.util.IO.withCloseable
import java.net.ServerSocket
import scala.concurrent.Await
import scalaz._
import Scalaz._
import spray.can.Http
import spray.http._
import spray.routing._

import com.maxmind.gatling.gen.Conf
import com.maxmind.gatling.gen.GatlingGenConf.DevConf._
import com.maxmind.gatling.gen.mock.Server.Handler

/**
  * A mock http server using akka for a server, and ning async http client for mock agents
  */
class Server(
    port: Int = Server.findOpenPort(),
    root: Conf = implicitly[Conf]
) {
  lazy val conf: MockServerConf = root.dev.mockServer

  /* For the benefit of akka and scala and scala timeouts */
  implicit val timeout: Timeout = conf.timeout

  lazy val sys          = ActorSystem("mock-server-actor-system")
  lazy val awaitTimeout = timeout.duration
  lazy val host         = conf.host
  lazy val baseUrl      = "http:" + ((Uri() withHost host) withPort port).toString
  lazy val mkUrl        = (urlSuffix: String) ⇒ s"$baseUrl$urlSuffix"

  private def start() = (sys actorOf (Props[Handler], "mock-server-handler")) ▹ {
    handler ⇒
      implicit val impSys = sys

      { IO(Http) ? (Http Bind (handler, host, port)) } ▹
        { bind ⇒ Await ready (bind, awaitTimeout) }
  }

  private def stop() = sys ◃ { _ shutdown () } ◃ { _ awaitTermination awaitTimeout }

  override def toString = s"MockServer on $baseUrl"
}

object Server {

  /* Run code inside mock server */
  def apply[T](f: Server ⇒ T): T = new Server() ◃ { _ start () } ▹
    { server ⇒ try f(server) finally { server stop () } }

  def findOpenPort(): Int = withCloseable(new ServerSocket(0)) { _ getLocalPort () }

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
