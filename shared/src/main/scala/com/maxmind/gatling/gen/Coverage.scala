package com.maxmind.gatling.gen

import nyaya.gen.{Gen, GenCtx}

import scalaz.Scalaz._

// How do you achieve fast data coverage? Sometimes you want to explore new
// unknowns, but for regression testing you want:
// 1- first goal of test is to cover as much as possible
// 2- willing to trade - low coverage of each area and less randomized testing
// 3- start from sampling, as many as possible, sectors of the input space
// 4- start with simple values, except when conflict with rule above
// 5- no duplicates whatsoever until coverage reached
// 6- after coverage achieved, switch to genChoose
// 7- TODO signal coverage progress
// 8- TODO coverage not on all children visited. Coverage on children covered!

object Coverage {

  def apply[T](nonEmpty: T, restChoices: T*): Gen[T] =
   apply((nonEmpty :: restChoices.toList) map Gen.pure)

  def apply[T](nonEmpty: Gen[T], restChoices: Gen[T]*): Gen[T] =
    apply(nonEmpty :: restChoices.toList)

  private def apply[T](choices: Seq[Gen[T]]): Gen[T] = {
    val onCover = Gen chooseGen(choices.head, choices.tail:_*)
    var iter: Iterator[Gen[T]] = null
    val initializeIter: GenCtx ⇒ Gen[T] = (genCtx: GenCtx) ⇒ {
      iter = (Gen shuffle choices run genCtx).iterator
      iter.next()
    }

    Gen {
      genCtx ⇒ {
        (iter == null) ? initializeIter(genCtx) |
          (iter.hasNext ? iter.next() | onCover)
      }.run(genCtx)
    }
  }
}


