package com.maxmind.gatling.gen.webapi

import scalaz._
import Scalaz._

import com.maxmind.gatling.gen.test.BaseSpec

class WebApiSpec extends BaseSpec {

  "Test the 'WebWeather' web service example".title

  type WeatherAction = WebWeather.IpWeather.Get
  type IpAddress = String


  case object WebWeather extends WebService {

    case object IpWeather extends Endpoint {

      case class Get(ip: IpAddress) extends Action
    }
  }


  "⋅ Another test" >> {
    1 must_== 1
  }
}

/*
⋅•⚫⏺●⬤

WeatherAction.generate(IpAddress("127.0.0.1"))
*/
