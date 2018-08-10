package org.seekloud.essf

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream}
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.zip.{ZipEntry, ZipFile, ZipInputStream, ZipOutputStream}

import scala.collection.immutable.SortedMap

/**
  * User: Taoz
  * Date: 8/5/2018
  * Time: 6:09 PM
  */
class TmpTests {

}


object TmpTests {
  def helloWorld(): Unit = {
    println("hello, world.")
  }

  def main5(args: Array[String]): Unit = {
    val target = "test_data/ziptest1.zip"
    val zipInput = new ZipFile(target)
    val inputStream = zipInput.getInputStream(zipInput.getEntry("f1.txt"))

    val bytes = new Array[Byte](256)
    val len = inputStream.read(bytes)
    val data = new Array[Byte](len)
    System.arraycopy(bytes, 0, data, 0, len)

    val str = new String(data)
    println(s"data:$str")
  }

  def main4(args: Array[String]): Unit = {

    val target = "test_data/ziptest1.zip"
    val zipOutput = new ZipOutputStream(new FileOutputStream(target))
    zipOutput.putNextEntry(new ZipEntry("f1.txt"))
    zipOutput.write("hello".getBytes("utf-8"))
    zipOutput.close()
    println("DONE.")

  }

  def main3(args: Array[String]): Unit = {

    val arr1 = Array(1,2,3,4,5)
    val arr2 = Array(1,2,3,4,5)

    println(Utils.arrayEquals(arr1, arr2))


  }


  def main2(args: Array[String]): Unit = {

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
