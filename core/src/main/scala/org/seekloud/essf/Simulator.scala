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

    val map = new java.util.TreeMap[Int, Long]()

    map.put(10, 11l)
    map.put(20, 12l)
    map.put(30, 13l)
    map.put(40, 14l)
    map.put(50, 15l)
    map.put(60, 16l)
    map.put(70, 17l)

    println(s"map.get(1): ${map.get(1)}")
    println(s"map.lowerKey(15): ${map.lowerKey(15)}")
    println(s"map.lowerKey(20): ${map.lowerKey(20)}")

    println(s"map.floorKey(5): ${map.floorKey(5)}")
    println(s"map.floorKey(15): ${map.floorKey(15)}")
    println(s"map.floorKey(20): ${map.floorKey(20)}")
    println(s"map.floorKey(75): ${map.floorKey(75)}")

    println(s"map.ceilingKey(5): ${map.ceilingKey(5)}")
    println(s"map.ceilingKey(15): ${map.ceilingKey(15)}")
    println(s"map.ceilingKey(20): ${map.ceilingKey(20)}")
    println(s"map.ceilingKey(75): ${map.ceilingKey(75)}")




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
