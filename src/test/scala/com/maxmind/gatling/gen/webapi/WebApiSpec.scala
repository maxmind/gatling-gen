package com.maxmind.gatling.gen.webapi

import scalaz._
import Scalaz._

import com.maxmind.gatling.gen.test.BaseSpec

class WebApiSpec extends BaseSpec {

  "Test the 'WebWeather' web service example".title

  type IpAddress = String

  case class Get(ip: IpAddress) extends GetAction

  "⋅ Another test" >> {
    1 must_== 1
  }
}

/*
⋅•⚫⏺●⬤


driver.action()

WeatherAction.generate(IpAddress("127.0.0.1"))

ipWeather


*/
