

name := "gatlinggen"
organization := "MaxMind"
version := "0.1-SNAPSHOT"
scalaVersion := "2.11.7"

autoScalaLibrary := true

libraryDependencies += "com.github.japgolly.nyaya" %% "nyaya-test" % "0.6.0" withSources() withJavadoc()
libraryDependencies += "com.github.japgolly.nyaya" %% "nyaya-prop" % "0.6.0" withSources() withJavadoc()
libraryDependencies += "com.github.japgolly.nyaya" %% "nyaya-gen" % "0.6.0" withSources() withJavadoc()
libraryDependencies += "com.lihaoyi" %% "utest" % "0.3.1" withSources() withJavadoc()
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7" withSources() withJavadoc()

testFrameworks += new TestFramework("utest.runner.Framework")
