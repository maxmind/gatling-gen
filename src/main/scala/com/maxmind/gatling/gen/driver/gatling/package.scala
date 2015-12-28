package com.maxmind.gatling.gen.driver

import io.gatling.http.config.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

package object gatling {

  type HttpBuilder = (HttpProtocolBuilder, HttpRequestBuilder)
  type HeadersSpec = Map[String, List[String]]

}
