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

  val boxType: String
  def size: Int

  def writePayload(buf: ByteBuffer): ByteBuffer
  def readPayload(buf: ByteBuffer): Box


}


object Box {



}

object BoxType{
  val ebif = "ebif"
  val stix = "stix"
  val epst = "stix"
  val eoep = "eoep"

  val smli = "smli"
  val smlv = "smlv"
  val smlm = "smli"
  val slen = "slev"
  val slst = "slst"
}



final class EOEP_Box extends Box {

  override val boxType: String = BoxType.eoep
  override def size: Int = 2 + 4

  //payload
  //no payload for eoep box.

  override def readPayload(buf: ByteBuffer): EOEP_Box = {
    this
  }

  override def writePayload(buf: ByteBuffer): ByteBuffer = {
    buf
  }

}

final class EBIF_Box extends Box {

  override val boxType: String = BoxType.ebif
  override def size: Int = (2 + 4) + 1 + 4 + 2 + 4 + 8

  //payload
  var version: Byte = 1
  var frameCount: Int = -1
  var frameMilliSeconds: Short = -1
  var snapshotCount: Int = -1
  var createTime: Long = -1l

  override def readPayload(buf: ByteBuffer): EBIF_Box = {
    version = buf.get()
    frameCount = buf.getInt()
    frameMilliSeconds = buf.getShort()
    snapshotCount = buf.getInt()
    createTime = buf.getLong
    this
  }

  override def writePayload(buf: ByteBuffer): ByteBuffer = {
    buf.put(version)
    buf.putInt(frameCount)
    buf.putShort(frameMilliSeconds)
    buf.putInt(snapshotCount)
    buf.putLong(createTime)
    buf
  }


  override def toString = s"EBIF_Box($boxType, $version, $frameCount, $frameMilliSeconds, $snapshotCount, $createTime, $size)"
}
