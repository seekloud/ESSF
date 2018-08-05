package org.seekloud.essf.test

import org.seekloud.essf.Simulator

/**
  * User: Taoz
  * Date: 8/5/2018
  * Time: 6:15 PM
  */
object Test {

  def main(args: Array[String]): Unit = {
    val self = this.getClass.toString
    println(s"i am $self")
    Simulator.helloWorld()
    println("DONE>")
  }

}
