package essf.jvmExample

import org.seekloud.essf.Simulator

/**
  * User: Taoz
  * Date: 8/5/2018
  * Time: 6:12 PM
  */
object Test1 {

  def main(args: Array[String]): Unit = {
    val self = this.getClass.toString
    println(s"i am $self")
    Simulator.helloWorld()
    println("DONE>")
  }

}
