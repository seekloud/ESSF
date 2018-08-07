package org.seekloud.essf.box

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 2:04 PM
  */
trait Box {

  final val defaultHeadSize: Int = 6
  val boxType: String
  def size: Int
  def writePayload(buf: ByteBuffer): ByteBuffer

}


object Box {



}



