package com.maxmind.gatling

import Predef._
import scala.language.implicitConversions
import scalaz._
import Scalaz._
import spray.http.Uri.Path.Segment
import spray.http.Uri._

import experiment.Url.{Scheme, Port}

/**
  * Experiment types and implicits.
  */
package object experiment {

  type PathSegment = Path
  type UrlString = String

  implicit def stringToNonEmptyHost(s: String): NonEmptyHost =
    Host(s).asInstanceOf[NonEmptyHost]

  implicit def hostToString(h: Host): String = h.address

  implicit def stringToSegment(s: String): Segment = Segment(s, Path.Empty)


  abstract class AccessPoint(val label: Label, val base: BaseUrl) {
    def url: UrlString = base.url

    type Self <: AccessPoint
    val self = this.asInstanceOf[Self]

    lazy val labelLens = Lens.lensu[Self, Label](
      (webEntity, label) ⇒ this.copyWebEntity(label = label), _.label
    )

    lazy val baseLens = Lens.lensu[Self, BaseUrl](
      (webEntity, base) ⇒ this.copyWebEntity(base = base), _.base
    )

    lazy val nameLens          = labelLens >=> Label.nameLens
    lazy val schemeLens        = baseLens >=> BaseUrl.schemeLens
    lazy val hostLens          = baseLens >=> BaseUrl.hostLens
    lazy val portLens          = baseLens >=> BaseUrl.portLens
    lazy val effectivePortLens = baseLens >=> BaseUrl.effectivePortLens
    lazy val segmentLens       = baseLens >=> BaseUrl.segmentLens

    def withLabel(newLabel: Label) = labelLens.set(self, newLabel)
    def name = nameLens.get(self)
    def withSegment(newSegment: Segment) = segmentLens.set(self, newSegment)
    def scheme: Scheme = schemeLens.get(self)
    def withScheme(newScheme: Scheme) = schemeLens.set(self, newScheme)
    def port: Port = portLens.get(self)
    def withPort(newPort: Port): Self = portLens.set(self, newPort)
    def effectivePort: Int = effectivePortLens.get(self)
    def host: Host = hostLens.get(self)
    def withHost(newHost: NonEmptyHost) = hostLens.set(self, newHost)

    def copyWebEntity(label: Label = label, base: BaseUrl = base): Self
  }

  /**
    * A webservice.
    */
  abstract class WebService(label: Label, base: BaseUrl) extends AccessPoint(label, base)

  /**
    * An endpoint in a web service.
    */
  abstract class Endpoint(label: Label, base: BaseUrl) extends AccessPoint(label, base)
}
