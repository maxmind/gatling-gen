name := "vandegraaf root project"

lazy val root = project.in(file(".")).
  aggregate(vandegraafJS, vandegraafJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val vandegraaf = crossProject.in(file(".")).
  settings(

    name := "vandegraaf",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.7",

    autoScalaLibrary := true,

    libraryDependencies += "com.lihaoyi" %% "utest" % "0.3.1" withSources() withJavadoc(),
    libraryDependencies += "com.github.japgolly.nyaya" %%% "nyaya-gen" % "0.6.0" withSources() withJavadoc(),
    libraryDependencies += "com.lihaoyi" % "ammonite-repl" % "0.4.8" % "test" cross CrossVersion.full,
    libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "0.4.8",
    libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7" withSources() withJavadoc(),

    testFrameworks += new TestFramework("utest.runner.Framework"),

    initialCommands in (Test, console) := """ammonite.repl.Repl.run("")"""
  ).
  jvmSettings(
  ).
  jsSettings(
    persistLauncher in Compile := true,
    persistLauncher in Test := false
  )

lazy val vandegraafJVM = vandegraaf.jvm
lazy val vandegraafJS = vandegraaf.js
