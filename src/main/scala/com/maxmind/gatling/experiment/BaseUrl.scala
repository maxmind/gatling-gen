package com.maxmind.gatling.experiment

import scala.language.implicitConversions
import scalaz.Lens
import spray.http.Uri.NonEmptyHost
import spray.http.Uri.Path.Segment

import com.maxmind.gatling.experiment.Url.{Port, Scheme}

/**
  * Has a Irl, and adds to it a path segment. Useful when creating endpoints from
  * a web service, or requests from an endpoint- generally when something needs a URL,
  * but is awkward to define directly as a URL, and is best defined as a segment
  * appended to a parent UrL.
  */
object BaseUrl {
  implicit def urlBasedToUrlString(u: BaseUrl): UrlString = u.url

  lazy val parentLens = Lens.lensu[BaseUrl, Url](
    (urlBased, parent) ⇒ urlBased.copy(parent = parent), _.parent
  )

  lazy val segmentLens = Lens.lensu[BaseUrl, Segment](
    (urlBased, segment) ⇒ urlBased.copy(segment = segment), _.segment
  )

  lazy val schemeLens        = parentLens >=> Url.schemeLens
  lazy val hostLens          = parentLens >=> Url.hostLens
  lazy val portLens          = parentLens >=> Url.portLens
  lazy val effectivePortLens = parentLens >=> Url.effectivePortLens
}

case class BaseUrl(
    val parent: Url = Predef.baseUrlDef,
    val segment: Segment = Predef.segmentDef
) {
  import BaseUrl._

  def url: UrlString = effectiveUrl()
  def effectiveUrl: Url = parent / segment

  def withSegment(newSegment: Segment) = segmentLens.set(this, newSegment)
  def scheme: Scheme = schemeLens.get(this)
  def withScheme(newScheme: Scheme) = schemeLens.set(this, newScheme)
  def port: Port = portLens.get(this)
  def withPort(newPort: Port) = portLens.set(this, newPort)
  def effectivePort: Int = effectivePortLens.get(this)
  def host: NonEmptyHost = hostLens.get(this)
  def withHost(newHost: NonEmptyHost) = hostLens.set(this, newHost)

  def /(s: Segment): BaseUrl = copy(parent = effectiveUrl, segment = s)
}

