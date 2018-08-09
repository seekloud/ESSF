package org.seekloud.essf.io

import java.io.{File, FileOutputStream, RandomAccessFile}
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

import org.seekloud.essf.box.Box

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 2:19 PM
  */
private[essf] class ESSFWriter(file: String) {

  import ESSFWriter.writeHead

  final val DEFAULT_BUFFER_SIZE = 32 * 1024
  private val fc = new RandomAccessFile(new File(file), "rw").getChannel

  //private val fc = new FileOutputStream(new File(file)).getChannel
  private val defaultBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE)


  def position(pos: Long): Unit = {
    fc.position(pos)
  }

  def put(box: Box): ESSFWriter = {
    val boxSize = box.size
    val buf =
      if (defaultBuffer.capacity() < boxSize) {
        ByteBuffer.allocate(boxSize)
      } else defaultBuffer
    buf.clear()
    writeHead(buf, boxSize, box.boxType)
    box.writePayload(buf)
    buf.flip()
    fc.write(buf)
    this
  }

  def put(box: Box, position: Long): ESSFWriter = {
    val boxSize = box.size
    val buf =
      if (defaultBuffer.capacity() < boxSize) {
        ByteBuffer.allocate(boxSize)
      } else defaultBuffer
    buf.clear()
    writeHead(buf, boxSize, box.boxType)
    box.writePayload(buf)
    buf.flip()
    val mark = fc.position()
    fc.position(position)
    fc.write(buf)
    fc.position(mark)
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
    buffer.put(boxType.getBytes("utf-8"))
    if (boxSize > Short.MaxValue) {
      buffer.putInt(boxSize)
    }
  }

}
