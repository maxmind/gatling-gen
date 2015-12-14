package com.maxmind.gatling.gen.driver.gatling

import io.gatling.http.config.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder
import scalaz._
import Scalaz._

/**
  * The gatling way to describe an HTTP request transform
  *
  * A gatling HTTP request mold is an HttpBuilder endomorphism
  */

object RequestMold {

  /* Get an HttpBuilder from an endo function and basic starting details */
  def build(
      from: HttpBuilder
  )(
      using: (HttpProtocolBuilder, HttpRequestBuilder) ⇒
        (HttpProtocolBuilder, HttpRequestBuilder)
  ): HttpBuilder = RequestMold {
    new Endo[HttpBuilder](
      builder ⇒ using(
        builder.protocolBuilder,
        builder.requestBuilder
      ) ▹ { HttpBuilder.apply _ }.tupled
    )
  }(from)

  /* Run an initial builder from some basic details and a mold */
  def apply(
      name: Name,
      baseUrl: BaseUrl,
      basePath: Segment
  ) = (HttpBuilder.init ∘ build) (name, baseUrl, basePath)
}

case class RequestMold(build: Endo[HttpBuilder]) {

  implicit val requestMoldEndo = new Endo[HttpBuilder](this.apply)

  /* Run an HttpBuilder through this mold, collecting my changes into the HTTP request */
  def apply(from: HttpBuilder): HttpBuilder = build(from)
}
