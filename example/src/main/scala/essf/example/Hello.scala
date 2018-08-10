package essf.example

import org.seekloud.essf.io.FrameInputStream

/**
  * User: Taoz
  * Date: 8/10/2018
  * Time: 12:42 PM
  */
object Hello {

  def main(args: Array[String]): Unit = {
    println("Anybody here?")

    val file = ""
    val input = new FrameInputStream(file)
    println(org.seekloud.essf.common.Constants.IO_VERSION)
    println(org.seekloud.essf.common.Constants.LIB_VERSION)


  }

}
