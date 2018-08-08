package org.seekloud.essf.box

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 2:04 PM
  */
abstract class Box(final val boxType: String) {

  lazy val size: Int = {
    val len = 6 + payloadSize
    if (len > Short.MaxValue) {
      len + 4
    } else len
  }

  val payloadSize: Int

  def writePayload(buf: ByteBuffer): ByteBuffer

}


object Box {


}



