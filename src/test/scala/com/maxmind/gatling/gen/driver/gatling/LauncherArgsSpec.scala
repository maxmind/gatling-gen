package com.maxmind.gatling.gen.driver.gatling

import ammonite.ops._
import collection.immutable.HashMap
import scalaz._
import Scalaz._

import com.maxmind.gatling.gen.driver.gatling.LauncherArgs._
import com.maxmind.gatling.gen.test.BaseSpec

class LauncherArgsSpec extends BaseSpec {

  /* A mock process builder that records args+env that were used for its launching */
  def mockStarter(cliArgs: CliArgs, env: Map[String, String]) =
    CliFinalParam(cliArgs, env.toList.toMap)

  "LauncherArgs builds a gatling runner from its exe conf".title

  "• LauncherArgs is facade for experiment launcher" >> {

    (LauncherArgs().baseUrl set "http://x/t") apply { mockStarter } match {
      case CliFinalParam(actualArgs, actualEnv) ⇒

        "⋅ Command line cliArgs" >> {
          (actualArgs must containMatch("gatling.sh")) and
            (actualArgs must contain("--output-name")) and
            (actualArgs must contain("--results-folder"))
        }

        "⋅ Command line cliEnv" >> {
          val actualKeys :: actualValues :: Nil = actualEnv ▹
            { a ⇒ List(a.keySet, a.values) ∘ { _.toList.sorted } }
          val internalDir = cwd / 'experiments / 'internal

          (actualKeys aka "env keys" must_== List(
            "GATLING_CONF",
            "GATLING_HOME",
            "JAVA_CLASSPATH",
            "JAVA_OPTS"
          )) and (
            actualValues aka "env values" must_== List(
              "-DexpPropertiesFile=" + (
                internalDir / 'conf / "experiment.properties"
                ).toString,
              internalDir.toString,
              (internalDir / 'conf).toString,
              (cwd / 'target / "scala-2.11" / "gatlinggen.jar").toString
            )
            )
        }
    }
  }

  "• CliSettings is lowest level of the wrapper" >> {

    /* Make CliSettings, merge them, start process, assert started correctly */
    {

      CliSettings(
        args = Seq("cmdFoo", "-keyBar", "-valueBar", "--flagBar"),
        env = Map("baz" → "123", "quux" → "987"),
        props = HashMap("abc" → "def", "ZZZZ" → "AAAA")
      )

    } apply { mockStarter } match {
      case CliFinalParam(actualArgs, actualEnv) ⇒

        "⋅ Command line cliArgs" >> {
          actualArgs must_==
            Seq("cmdFoo", "-keyBar", "-valueBar", "--flagBar")
        }

        "⋅ Command line cliEnv" >> {
          val actualKeys :: actualValues :: Nil = actualEnv ▹
            { a ⇒ List(a.keySet, a.values) ∘ { _.toList.sorted } }

          (actualKeys aka "env keys" must_== List(
            "JAVA_OPTS", "baz", "quux"
          )) and
            (actualValues aka "env values" must_== List(
              "-Dabc=def -DZZZZ=AAAA", "123", "987"))
        }
    }
  }
}

