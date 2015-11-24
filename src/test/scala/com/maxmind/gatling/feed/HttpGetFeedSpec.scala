package com.maxmind.gatling.feed

import com.maxmind.gatling.BaseSpec
import org.specs2.specification.core.Fragments

class HttpGetFeedSpec extends BaseSpec {

  override protected def innerIs: Fragments = s2"""

    The generated HTTP GET gatling feed
  """

  1 must_== 1
}
