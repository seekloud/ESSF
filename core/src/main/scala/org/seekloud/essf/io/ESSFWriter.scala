package org.seekloud.essf.io

import java.io.{File, FileOutputStream}
import java.nio.ByteBuffer

import org.seekloud.essf.box.Box

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 2:19 PM
  */
private[essf] class ESSFWriter(file: String) {

  import ESSFWriter.writeHead

  final val DEFAULT_BUFFER_SIZE = 32 * 1024
  private val fc = new FileOutputStream(new File(file)).getChannel
  private val defaultBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)


  def put(box: Box): ESSFWriter = {
    val boxSize = box.size
    defaultBuffer.clear()
    val buf =
      if (defaultBuffer.capacity() < boxSize) {
        ByteBuffer.allocate(boxSize)
      } else defaultBuffer
    writeHead(buf, boxSize, box.boxType)
    box.writePayload(buf)
    this
  }


  def close(): Unit = {
    fc.close()
  }


}

object ESSFWriter{

  def writeHead(buffer: ByteBuffer, boxSize: Int, boxType: String): Unit = {
    if (boxSize > Short.MaxValue) {
      buffer.putShort(1.toShort)
    } else {
      buffer.putShort(boxSize.toShort)
    }
    buffer.put(boxType.substring(0, 4).getBytes("utf-8"))
    if (boxSize > Short.MaxValue) {
      buffer.putInt(boxSize)
    }
  }

}
