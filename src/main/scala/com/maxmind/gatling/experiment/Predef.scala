package com.maxmind.gatling.experiment

import scalaz.Scalaz._
import scalaz._
import spray.http.Uri.Path.Segment
import spray.http.Uri._

import com.maxmind.gatling.experiment.Url.{NonSecure, Port, Scheme}

/**
  * Predefined experiment values & implicits.
  */
object Predef {

  lazy val schemeDef : Scheme       = NonSecure
  lazy val hostDef   : NonEmptyHost = NamedHost("localhost")
  lazy val portDef   : Port         = schemeDef.port
  lazy val pathDef   : Path         = Path./
  lazy val segmentDef: Segment      = "/"
  lazy val baseUrlDef: Url          = Url()
}
