package com.maxmind.gatling.gen.driver.gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.config.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder
import scalaz._
import Scalaz._

/**
  * The gatling way to describe an HTTP request
  *
  * Gatling HTTP settings are split into protocol & request settings. This unifies them
  */
object HttpBuilder {

  /* Get an HttpBuilder from some basic details */
  val init: (Name, BaseUrl, Segment) ⇒ HttpBuilder =
    (name, baseUrl, basePath) ⇒ HttpBuilder(
      http baseURL baseUrl,
      http(name) get basePath
    )
}

case class HttpBuilder(
    protocolBuilder: HttpProtocolBuilder,
    requestBuilder: HttpRequestBuilder
)

