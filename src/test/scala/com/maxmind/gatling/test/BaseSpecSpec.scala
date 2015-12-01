package com.maxmind.gatling.test

import scalaz.Scalaz._
import scalaz._

class BaseSpecSpec extends BaseSpec {

  "BaseSpec self-sanity and examples".title

  "• SampleSize implicit" >> {

    def default = SampleSize.defaultSize

    "⋅ Is defined" >> { default.toInt must be_>(1) }

    "⋅ Converts to Int" >> { 0 + default must_=== default.toInt }

    "⋅ Implicit default" >> { currentSize must_== default }

    "⋅ Parameter override" >> {
      currentSize(SampleSize(4321)) must_== SampleSize(4321)
    }

    "⋅ Local implicit override" >> {
      implicit val size: SampleSize = 1234
      currentSize must_== size
    }
  }

  "• BaseSpec test helpers" >> {

    implicit val size: SampleSize = 3

    "⋅ fillN" >> { fillN { 7 } must_== List(7, 7, 7) }

    "⋅ beN" >> { (3 must beN) and (5 must beN.not) }

    "⋅ haveSizeN" >> {
      implicit val size: SampleSize = 2
      (List(1, 2) must haveSizeN) and (List(1, 2, 3) must haveSizeN.not)
    }
  }

}

