package com.maxmind.gatling.experiment

import scalaz.Scalaz._
import scalaz._
import spray.http.Uri.{Host, IPv4Host, NamedHost}

import com.maxmind.gatling.experiment.Url.{CustomPort, Secure}
import com.maxmind.gatling.test.BaseSpec

class WebEntitiesSpec extends BaseSpec {

  "A WebEntity is a named path on some base URL".title

  case class FooWebService(
      override val label: Label = Label("foo-name", "foo-label", "foo-description"),
      override val base: BaseUrl =
      BaseUrl() withSegment "fooWebService" withScheme Secure withHost "127.0.0.1"
  ) extends WebService(label, base) {
    type Self = FooWebService

    lazy val barEndpoint = BarEndpoint()

    override def copyWebEntity(
        label: Label = label, base: BaseUrl = base): FooWebService =
      copy(label = label, base = base)

    case class BarEndpoint(
        override val label: Label = Label("bar-name", "bar-label", "bar-description"),
        override val base: BaseUrl = FooWebService.this.base / "barEndpoint"
    ) extends WebService(label, base) {
      type Self = BarEndpoint

      override def copyWebEntity(
          label: Label = label, base: BaseUrl = base): BarEndpoint =
        copy(label = label, base = base)
    }
  }

  lazy val fooWebService           = FooWebService()
  lazy val fooWebServiceCustomPort = fooWebService withPort CustomPort(12345)

  lazy val barEndpoint           = fooWebService.barEndpoint
  lazy val barEndpointCustomPort = fooWebServiceCustomPort.barEndpoint
  lazy val barEndpointCustomHost = barEndpoint withHost "gatling.io"

  "⋅ Basic" >> { (fooWebService.name: String) must_== "foo-name" }

  "• WebService isa UrlBased" >> {

    "⋅ Host" >> { fooWebService.host must_== Host("127.0.0.1") }

    "⋅ Url" >> { fooWebService.url must_== "https://127.0.0.1/fooWebService" }

    "⋅ Port from scheme" >> { fooWebService.effectivePort must_== 443 }

    "⋅ Port lens" >> { fooWebServiceCustomPort.effectivePort must_== 12345 }
  }

  "• Web service has endpoints" >> {

    val barEndpoint = fooWebService.barEndpoint

    "⋅ Host taken from web service" >> { barEndpoint.host must_== IPv4Host("127.0.0.1") }

    "⋅ But can be customized" >>
      { barEndpointCustomHost.host must_== NamedHost("gatling.io") }

    "⋅ Port taken from web service" >>
      { barEndpointCustomPort.effectivePort must_== 12345 }
  }
}

