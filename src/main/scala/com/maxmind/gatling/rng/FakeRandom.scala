package com.maxmind.gatling.rng

import java.util.Random

/**
  * ScalaCheck gen only uses getLong() and getDouble(). For testing and replay,
  * replace the rng with this java.util.Random compatible rng, and control it
  * using the constructor param function mkLong.
  */
case class FakeRandom(mkLong: () ⇒ Long = { () ⇒ 0L }) extends Random {

  override def nextLong = mkLong()

  override def nextDouble = mkLong().toDouble
}
