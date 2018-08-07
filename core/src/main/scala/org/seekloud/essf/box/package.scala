package org.seekloud.essf

import java.nio.ByteBuffer

import scala.collection.mutable
import scala.util.Try

/**
  * User: Taoz
  * Date: 8/7/2018
  * Time: 10:38 AM
  */
package object box {

  object BoxType {
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


  /*
    ===============  episode box ==================
    val ebif = "ebif"
    val stix = "stix"
    val epst = "stix"
    val eoep = "eoep"
   */



  final case class EBIF_Box(
    version: Byte,
    frameCount: Int,
    frameMilliSeconds: Short,
    snapshotCount: Int,
    createTime: Long
  ) extends Box {

    override val boxType: String = BoxType.ebif
    override def size: Int = defaultHeadSize + 1 + 4 + 2 + 4 + 8

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.put(version)
      buf.putInt(frameCount)
      buf.putShort(frameMilliSeconds)
      buf.putInt(snapshotCount)
      buf.putLong(createTime)
      buf
    }
  }

  object EBIF_Box {
    def readFromBuffer(buf: ByteBuffer): Try[EBIF_Box] = Try{
      val version = buf.get()
      val frameCount = buf.getInt()
      val frameMilliSeconds = buf.getShort()
      val snapshotCount = buf.getInt()
      val createTime = buf.getLong
      EBIF_Box(version, frameCount, frameMilliSeconds, snapshotCount, createTime)
    }
  }


  final case class STIX_Box(
    stateIndexMap : mutable.TreeMap[Int, Long]
  ) extends Box {

    override val boxType: String = BoxType.stix
    override def size: Int = {
      val len = defaultHeadSize + (stateIndexMap.size * 12) + 4
      if (len > Short.MaxValue) {
        len + 2
      } else len
    }

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(stateIndexMap.size)
      stateIndexMap.foreach { case (frameIndex, offset) =>
        buf.putInt(frameIndex)
        buf.putLong(offset)
      }
      buf
    }
  }

  object STIX_Box{
    def readFromBuffer(buf: ByteBuffer) = Try {
      val stateIndexMap = new mutable.TreeMap[Int, Long]()
      var len = buf.getInt()
      stateIndexMap.clear()
      stateIndexMap.sizeHint(len)
      while (len > 0) {
        val frameIndex = buf.getInt()
        val offset = buf.getLong()
        stateIndexMap.put(frameIndex, offset)
        len -= 1
      }
      STIX_Box(stateIndexMap)
    }
  }


  final case class EPST_Box(data: Int) extends Box {

    override val boxType: String = BoxType.epst
    override def size: Int = defaultHeadSize + 4

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(data)
      buf
    }

  }

  object EPST_Box {
    def readFromBuffer(buf: ByteBuffer) = Try{
      val data = buf.get()
      EPST_Box(data)
    }
  }




  final case class EOEP_Box() extends Box {

    override val boxType: String = BoxType.eoep
    override def size: Int = defaultHeadSize

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf
    }

  }

  object EOEP_Box{
    def readFromBuffer(buf: ByteBuffer) =Try {
      EOEP_Box()
    }
  }




  /*
   ===================   simulator box   =====================
    val smli = "smli"
    val smlv = "smlv"
    val smlm = "smli"
    val slen = "slev"
    val slst = "slst"
   */


  final case class SMLI_Box(id: String) extends Box {

    override val boxType: String = BoxType.smli

    override def size: Int = defaultHeadSize

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      val bytes = id.getBytes("utf-8")
      assert(bytes.length < 127)
      buf.put(bytes.length.toByte)
      buf.put(bytes)
      buf
    }
  }

  object SMLI_Box{
    def readFromBuffer(buf: ByteBuffer) = Try{
      val len = buf.get()
      val array = new Array[Byte](len)
      val id = new String(array, "UTF-8")
      SMLI_Box(id)
    }
  }


}
