package org.seekloud.essf.io

import java.io.{File, FileInputStream}
import java.nio.ByteBuffer

import org.seekloud.essf.box.{Box, BoxType, EBIF_Box}

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 3:55 PM
  */
private[essf] class ESSFReader(file: String) {


  private val fc = new FileInputStream(new File(file)).getChannel
  private val buffer = ByteBuffer.allocate(32 * 1024)


  def get(): Box = {
    fc.read(buffer)
    buffer.flip()
    var boxSize: Int = buffer.getShort()
    val typeStrArray = new Array[Byte](4)
    buffer.get(typeStrArray)
    val boxType = new String(typeStrArray, "utf-8")
    if(boxSize == 1) {
      boxSize = buffer.getInt()
    }

    val box = boxType match{
      case BoxType.ebif =>
        val box = new EBIF_Box
        box.readPayload(buffer)
        box
      case x =>
        throw new RuntimeException(s"unknown boxType error: $x")
    }
    box
  }


  def close(): Unit = {
    fc.close()
  }


}
