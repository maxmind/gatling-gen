package com.maxmind.gatling.experiment

import com.ning.http.client.uri.{Uri ⇒ NingUri}
import scala.language.implicitConversions
import scalaz._
import Scalaz._
import spray.http.Uri.Path.Segment
import spray.http.Uri._
import spray.http.{StringRendering, Uri ⇒ SprayUri}

import com.maxmind.gatling.experiment.Url.{Scheme, SchemePort, Port}

/**
  * Bridge the super-convenient spray URI ↔ the ning URI used by gatling
  */

object Url {

  sealed trait Port {
    def value(schemeName: String = "http"): Int
  }
  case class CustomPort(val value: Int) extends Port {
    def value(schemeName: String): Int = value
  }
  case class SchemePort() extends Port {
    def value(schemeName: String): Int = SprayUri.defaultPorts(schemeName)
  }

  object Scheme {implicit def convertToString(s: Scheme): String = s.asUrlPrefix }
  sealed trait Scheme {
    val asUrlPrefix: String
    lazy val port: Port = SchemePort()
  }
  object NonSecure extends Scheme {val asUrlPrefix = "http" }
  object Secure extends Scheme {val asUrlPrefix = "https" }


  implicit def toSprayUri(url: Url): SprayUri = url.uri
  implicit def toNingUri(url: Url): NingUri = NingUri.create(url())

  lazy val schemeLens = Lens.lensu[Url, Scheme](
    (baseUrl, scheme) => baseUrl copy (scheme = scheme), _.scheme
  )

  lazy val hostLens = Lens.lensu[Url, NonEmptyHost](
    (baseUrl, host) => baseUrl copy (host = host), _.host
  )

  lazy val portLens = Lens.lensu[Url, Port](
    (baseUrl, port) => baseUrl copy (port = port), _.port
  )

  lazy val effectivePortLens = Lens.lensu[Url, Int](
    (baseUrl, port) => baseUrl copy (port = CustomPort(port)), _.authority.port
  )
}

case class Url(
    val scheme: Scheme = Predef.schemeDef,
    val host: NonEmptyHost = Predef.hostDef,
    val port: Port = SchemePort(),
    val path: Path = Predef.pathDef
) {

  def authority: Authority = Authority(
    host = host,
    port = port value scheme.asUrlPrefix
  )

  def uri: SprayUri = SprayUri(
    scheme = scheme,
    authority = authority,
    path = path
  )

  def apply(): UrlString = (uri render new StringRendering()).get

  def /(s: Segment): Url = copy(path = path ++ s)

}
