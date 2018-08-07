package org.seekloud.essf.io

import java.io.{File, FileInputStream}
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

import org.seekloud.essf.box.{Box, BoxType, EBIF_Box, EOEP_Box}

import scala.util.Try

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 3:55 PM
  */
private[essf] class ESSFReader(file: String) {

  private val fc = new FileInputStream(new File(file)).getChannel
  private val defaultBuffer = ByteBuffer.allocateDirect(32 * 1024)

  import ESSFReader.readHead

  def get(): Box = {
    val (boxSize, boxType, payloadSize) = readHead(fc)
    val buf =
      if (defaultBuffer.capacity() < payloadSize) {
        ByteBuffer.allocate(payloadSize)
      } else defaultBuffer
    buf.clear()
    buf.limit(payloadSize)
    fc.read(buf)
    if (buf.hasRemaining) {
      throw new RuntimeException("buffer not full.")
    }
    buf.flip()
    decodeBox(boxType, buf)
  }



  private[this] def decodeBox(boxType: String, buf: ByteBuffer): Box = {
    val boxTry: Try[Box] = boxType match {
      case BoxType.ebif => EBIF_Box.readFromBuffer(buf)
      case BoxType.eoep => EOEP_Box.readFromBuffer(buf)
      case x =>
        throw new RuntimeException(s"unknown boxType error: $x")
    }
    boxTry.get
  }

  def close(): Unit = {
    fc.close()
  }

}

object ESSFReader {

  private[this] val hBuff = ByteBuffer.allocateDirect(32)


  private def readHead(fc: FileChannel): (Int, String, Int) = {
    hBuff.clear()
    hBuff.limit(6)
    fc.read(hBuff)
    hBuff.flip()
    var boxSize: Int = hBuff.getShort()
    val tmpArr = new Array[Byte](4)
    hBuff.get(tmpArr)
    val boxType = new String(tmpArr, "utf-8")
    var payloadSize = boxSize - 6
    if (boxSize == 1) {
      hBuff.clear()
      hBuff.limit(4)
      fc.read(hBuff)
      hBuff.flip()
      boxSize = hBuff.getInt()
      payloadSize = boxSize - 10
    }

    (boxSize, boxType, payloadSize)
  }
}
