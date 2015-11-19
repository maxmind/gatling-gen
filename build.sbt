
name := "gatling-gen root project"

lazy val root = project.in(file(".")).
  aggregate(gatlingGenJS, gatlingGenJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val gatlingGen = crossProject.in(file(".")).
  settings(

    name := "gatling-gen",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.7",

    autoScalaLibrary := true,

    libraryDependencies += "com.lihaoyi" %% "utest" % "0.3.1" withSources() withJavadoc(),
    libraryDependencies += "com.github.japgolly.nyaya" %%% "nyaya-prop" % "0.6.0" withSources() withJavadoc(),
    libraryDependencies += "com.github.japgolly.nyaya" %%% "nyaya-gen" % "0.6.0" withSources() withJavadoc(),
    libraryDependencies += "com.github.japgolly.nyaya" %%% "nyaya-test" % "0.6.0" % "test" withSources() withJavadoc(),
    libraryDependencies += "com.lihaoyi" % "ammonite-repl" % "0.4.8" % "test" cross CrossVersion.full,
    libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "0.4.8",

    testFrameworks += new TestFramework("utest.runner.Framework"),

    initialCommands in (Test, console) := """ammonite.repl.Repl.run("")"""
  ).

  jvmSettings(
    libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7" withSources() withJavadoc()
  ).

  jsSettings(

    persistLauncher in Compile := true,
    persistLauncher in Test := false
  )

lazy val gatlingGenJVM = gatlingGen.jvm
lazy val gatlingGenJS = gatlingGen.js


