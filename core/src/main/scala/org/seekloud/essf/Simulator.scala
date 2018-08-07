package org.seekloud.essf

import java.nio.ByteBuffer
import java.nio.charset.Charset

import scala.collection.immutable.SortedMap

/**
  * User: Taoz
  * Date: 8/5/2018
  * Time: 6:09 PM
  */
class Simulator {

}



object Simulator {
  def helloWorld(): Unit = {
    println("hello, world.")
  }




  def main(args: Array[String]): Unit = {




  }


  def main1(args: Array[String]): Unit = {

    val buffer = ByteBuffer.allocate(1024)
    buffer.put("hello,world".getBytes("utf-8"))
    buffer.rewind()
/*
    buffer.flip()
*/
    val array = new Array[Byte](buffer.limit())
    buffer.get(array)
    println(s"byte: ${array.mkString(",")}")
/*
    while (buffer.hasRemaining) {
      println(s"byte: ${buffer.get()}")
    }*/
  }





}
