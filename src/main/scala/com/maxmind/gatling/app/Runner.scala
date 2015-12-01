package com.maxmind.gatling.app

import java.util.Date

import ammonite.ops._
import scala.sys.process._

import io.gatling.app.GatlingStatusCodes

import scalaz.Scalaz._
import scalaz._

/**
  * A gatling simulation runner.
  */
object Runner {
  def apply(
    baseDir: Path = cwd / "sim-results",
    classPath: String = null,
    description: String = new Date().toString,
    name: String = null,
    simClassName: String
  ): Runner = {

    val defaultBinDir = cwd / 'target / "scala-2.11"
    val defaultPackageJar = defaultBinDir / "gatlinggen.jar"

    prepareBaseDir(baseDir)

    val fixedName =
      (name == null) ? (simClassName ++ ":" ++ description) | name

    val fixedClassPath =
      (classPath == null) ? defaultPackageJar.toString | classPath

    new Runner(
      baseDir,
      fixedClassPath,
      description,
      fixedName,
      simClassName
    )
  }

  def prepareBaseDir(baseDir: Path): Unit = {

    if (exists ! baseDir) {
      val baseStat = stat ! baseDir
      assume(!baseStat.isFile, { s"There is a file at $baseDir" })
    } else {
      mkdir ! baseDir
    }
    assert((stat ! baseDir).isDir, { s"There is no dir at $baseDir" })
  }
}

class Runner(
  baseDir: Path,
  classPath: String,
  description: String,
  name: String,
  simClassName: String
) {

  lazy val args = Array[String](
    "--output-name", name,
    "--results-folder", baseDir.toString,
    "--run-description", description,
    "--simulation", simClassName
  )

  def run(baseUrl: String): (Boolean, String) = {

    val command: Seq[String] =
      Seq("gatling") ++ (args.toSeq map { (s: String) ⇒ s.replace(' ', '_')})

    val process = Process(
      command,
      None,
      "JAVA_CLASSPATH" → classPath,
      "JAVA_OPTS" → ("-DbaseUrl=" ++ baseUrl)
    )

    process.! match {

      case GatlingStatusCodes.Success =>
        (true, "OK")

      case GatlingStatusCodes.AssertionsFailed =>
        (false, "Fail: simulation assertion")

      case GatlingStatusCodes.InvalidArguments => (
        false,
        "Fail: invalid arguments [" ++ args.mkString("(", ", ", ")") ++ "]"
        )
    }
  }
}
