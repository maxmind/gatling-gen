import scala.scalajs.js

object Main extends js.JSApp {
  def main(): Unit = {
    val lib = new MyLibrary
    print(lib.sq(3) + "\n")
  }
}
