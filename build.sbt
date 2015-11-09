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
    scalaVersion := "2.11.7"
  ).
  jvmSettings(
  ).
  jsSettings(
  )

lazy val vandegraafJVM = vandegraaf.jvm
lazy val vandegraafJS = vandegraaf.js
