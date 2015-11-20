package com.maxmind.gatling.gen.test

import com.maxmind.gatling.gen.{Coverage, Grain}
import nyaya.gen.Gen
import utest._

object FeedTests extends TestSuite {

  def tests = TestSuite {

    // Gatling feeder is an iterator of maps, on each request pulls out a map
    type Feeder[T] = Iterator[Map[String, T]]

    "builtin feeder" - {
      var i = 0;
      val iut: Feeder[Int] = Iterator.continually { Map("foo" → { i += 1; i }) }
      val actual = (iut take 5).map(_.get("foo").get).mkString
      val expected = "12345"
      assert(actual == expected)
    }

    "generated feeder" - {
      val generator = for (
        path ← Coverage("/searchWeb", "/searchEmail");
        query ← Gen.alphaNumeric string 3
      ) yield Map("path" → path, "query" → query)
      val iut: Feeder[String] = Grain.selfTest({ generator })
      val actual = (iut take 2).map(m ⇒
        (m get "path").get + ":" + (m get "query").get) mkString ","
      val expected = "/searchEmail:HNL,/searchWeb:HFB"
      assert(actual == expected)
    }
  }
}
