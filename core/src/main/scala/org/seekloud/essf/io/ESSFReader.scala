package org.seekloud.essf.io

import java.io.{File, FileInputStream}
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

import org.seekloud.essf.box.{Box, BoxType, EBIF_Box, EOEP_Box}

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
    val box = boxType match {
      case BoxType.ebif => new EBIF_Box()
      case BoxType.eoep => new EOEP_Box()
      case x =>
        throw new RuntimeException(s"unknown boxType error: $x")
    }
    box.readPayload(buf)
    box
  }

  def close(): Unit = {
    fc.close()
  }

}

object ESSFReader {

  private[this] val headBuffer = ByteBuffer.allocateDirect(32)


  private def readHead(fc: FileChannel): (Int, String, Int) = {
    headBuffer.clear()
    headBuffer.limit(6)
    fc.read(headBuffer)
    headBuffer.flip()
    var boxSize: Int = headBuffer.getShort()
    val typeStrArray = new Array[Byte](4)
    headBuffer.get(typeStrArray)
    val boxType = new String(typeStrArray, "utf-8")
    var payloadSize = boxSize - 6
    if (boxSize == 1) {
      headBuffer.clear()
      headBuffer.limit(4)
      fc.read(headBuffer)
      headBuffer.flip()
      boxSize = headBuffer.getInt()
      payloadSize = boxSize - 10
    }

    (boxSize, boxType, payloadSize)
  }
}
