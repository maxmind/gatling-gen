package vandegraaf

import scala.util.Random

/**
 * A seed for RNGs.
 */
class Seed(seed: Long) {

  lazy val rng = buildRng(seed)

  // Return a new RNG seeded by my seed.
  def splitRng() = new Seed(rng.nextInt())

  private def buildRng(seedArg: Long) = new Random(seedArg)
}
