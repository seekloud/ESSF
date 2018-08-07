package org.seekloud.essf.test

import java.io.{File, FileInputStream}
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

import org.scalatest.Outcome

/**
  * User: Taoz
  * Date: 8/7/2018
  * Time: 7:33 AM
  */
class TmpFunSpec extends UnitSpec {


  def withFileChannel(testCode: FileChannel => Any): Unit = {
    val file = "test_data/tmp1.txt" //abcdefg1234567
    val fc = new FileInputStream(new File(file)).getChannel
    try {
      testCode(fc)
    } finally {
      fc.close()
    }
  }

  def readChars(fc: FileChannel, buffer: ByteBuffer, num: Int): String = {
    buffer.clear()
    buffer.limit(2)
    fc.read(buffer)
    buffer.flip()
    val array = new Array[Byte](buffer.remaining())
    buffer.get(array)
    new String(array, "utf-8")
  }

  "A ByteBuffer" should "just read limit bytes" in withFileChannel{ fc =>
    val buffer = ByteBuffer.allocate(1024)
    buffer.clear()
    buffer.limit(2)
    fc.read(buffer)
    buffer.flip()
    val array = new Array[Byte](buffer.remaining())
    buffer.get(array)
    assert(array.length == 2 && buffer.position() == 2)
  }


  it should "continue read" in withFileChannel{ fc =>
    val buffer = ByteBuffer.allocate(1024)
    readChars(fc, buffer, 2)
    buffer.limit(4)
    buffer.mark()
    fc.read(buffer)
    buffer.reset()
    val array = new Array[Byte](buffer.remaining())
    buffer.get(array)
    val str = new String(array, "utf-8")
    assert(str == "cd")
  }

  it should "continue read2" in withFileChannel{ fc =>
    val buffer = ByteBuffer.allocate(1024)
    val str1 = readChars(fc, buffer, 2)
    val str2 = readChars(fc, buffer, 2)
    assert(str2 == "cd")
  }



}
