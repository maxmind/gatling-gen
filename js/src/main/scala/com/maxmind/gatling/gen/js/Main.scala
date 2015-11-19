package com.maxmind.gatling.gen.hs

import scala.scalajs.js
import com.maxmind.gatling.gen.Grain

object Main extends js.JSApp {
  def main(): Unit = {
    val grain = Grain(123)
    println(grain.split().split().initialSeed)
  }
}
