import vandegraaf.Grain

object Main {
  def main(args: Array[String]): Unit = {
    val grain =Grain(123)
    println(grain.split().split().initialSeed)
  }
}
