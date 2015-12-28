package com.maxmind.gatling.gen.driver.gatling

import scalaz._
import Scalaz._

/**
  * The gatling way to describe an HTTP request
  *
  * A gatling HTTP request mold is an HttpBuilder endomorphism
  */

case class RequestMold(build: HttpBuilder ⇒ HttpBuilder) {

  implicit val requestMoldEndo = new Endo[HttpBuilder](this.apply)

  def apply(from: HttpBuilder): HttpBuilder = build(from)
}
