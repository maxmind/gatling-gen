

name := "gatlinggen"
organization := "MaxMind"
version := "0.1-SNAPSHOT"
scalaVersion := "2.11.7"

autoScalaLibrary := true

libraryDependencies += "com.github.scalaprops" %% "scalaprops" % "0.1.16" withSources() withJavadoc()
libraryDependencies += "com.lihaoyi" % "ammonite-repl" % "0.4.9" cross CrossVersion.full withSources() withJavadoc()
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.1.5" withSources() withJavadoc()

logBuffered in Test := false

initialCommands in (Test, console) := """ammonite.repl.Repl.run("")"""

testFrameworks += new TestFramework("scalaprops.ScalapropsFramework")

parallelExecution in Test := false

scalapropsSettings

scalapropsVersion := "0.1.16"
