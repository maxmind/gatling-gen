package com.maxmind.gatling.experiment

import scala.language.implicitConversions
import scalaz._
import Scalaz._

import com.maxmind.gatling.experiment.Label.PosixName

/**
  * A name with a label and description to show in reports.
  */
object Label {

  object PosixName {
    implicit def nameToString(n: PosixName): String = n.value
    implicit def stringToName(s: String): PosixName = PosixName(s)
  }

  case class PosixName(val value: String) {
    val legalName = """^[0-9a-zA-Z._\-]+$""".r
    def apply(): String = value
    assume(
      { legalName findFirstIn value }.isDefined,
      s"Invalid name '$value'- must match $legalName"
    )
  }

  lazy val nameLens = Lens.lensu[Label, PosixName](
    (label: Label, newName: PosixName) ⇒ label.copy(name = newName), _.name
  )
}

case class Label(
    val name: PosixName = PosixName("no-name"),
    val label: String = "no label",
    val description: String = "no description"
)

