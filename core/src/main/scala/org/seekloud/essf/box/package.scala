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
    val epif = "epif"
    val stix = "stix"
    val epst = "epst"
    val eofl = "eofl"

    val smli = "smli"
    val smlm = "smlm"
    val slfr = "slfr"
  }


  /*
    ===============  episode box ==================
    val flmt = "flmt"
    val ebif = "ebif"
    val stix = "stix"
    val epst = "stix"
    val eoep = "eoep"
   */


  final case class FLMT_Box(
    version: Byte,
    createTime: Long
  ) extends Box(BoxType.epif) {
    override lazy val payloadSize: Int = 1 + 8

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.put(version)
      buf.putLong(createTime)
      buf
    }
  }

  object FLMT_Box {
    def readFromBuffer(buf: ByteBuffer): Try[FLMT_Box] = Try {
      val version = buf.get()
      val createTime = buf.getLong
      FLMT_Box(version, createTime)
    }
  }


  final case class EPIF_Box(
    frameCount: Int,
    frameMilliSeconds: Short,
    snapshotCount: Int
  ) extends Box(BoxType.epif) {

    override lazy val payloadSize: Int = 4 + 2 + 4

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(frameCount)
      buf.putShort(frameMilliSeconds)
      buf.putInt(snapshotCount)
      buf
    }
  }

  object EPIF_Box {
    def readFromBuffer(buf: ByteBuffer): Try[EPIF_Box] = Try {
      val frameCount = buf.getInt()
      val frameMilliSeconds = buf.getShort()
      val snapshotCount = buf.getInt()
      EPIF_Box(frameCount, frameMilliSeconds, snapshotCount)
    }
  }


  final case class STIX_Box(
    stateIndexMap: mutable.TreeMap[Int, Long]
  ) extends Box(BoxType.stix) {

    override lazy val payloadSize: Int = 12 * stateIndexMap.size + 4

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(stateIndexMap.size)
      stateIndexMap.foreach { case (frameIndex, offset) =>
        buf.putInt(frameIndex)
        buf.putLong(offset)
      }
      buf
    }
  }

  object STIX_Box {
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


  final case class EPST_Box(data: Int) extends Box(BoxType.epst) {

    override lazy val payloadSize: Int = 4

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(data)
      buf
    }

  }

  object EPST_Box {
    def readFromBuffer(buf: ByteBuffer) = Try {
      val data = buf.get()
      EPST_Box(data)
    }
  }


  final case class EOFL_Box() extends Box(BoxType.eofl) {

    override lazy val payloadSize: Int = 0

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf
    }

  }

  object EOFL_Box {
    def readFromBuffer(buf: ByteBuffer) = Try {
      EOFL_Box()
    }
  }


  /*
   ===================   simulator box   =====================
    val smli = "smli"
    val smlm = "smlm"
    val slev = "slev"
   */


  final case class SMLI_Box(id: String, version: String) extends Box(BoxType.smli) {

    private val idBytes = id.getBytes("utf-8")
    private val verBytes = version.getBytes("utf-8")
    assert(idBytes.length < 127)
    assert(verBytes.length < 127)

    override lazy val payloadSize: Int = 1 + idBytes.length + 1 + verBytes.length

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.put(idBytes.length.toByte)
      buf.put(idBytes)
      buf.put(verBytes.length.toByte)
      buf.put(verBytes)
      buf
    }
  }

  object SMLI_Box {
    def readFromBuffer(buf: ByteBuffer) = Try {
      val idLen = buf.get()
      val idArr = new Array[Byte](idLen)
      buf.get(idArr)
      val id = new String(idArr, "UTF-8")

      val verLen = buf.get()
      val verArr = new Array[Byte](verLen)
      buf.get(verArr)
      val ver = new String(verArr, "UTF-8")

      SMLI_Box(id, ver)
    }
  }


  final case class SMLM_Box(metadata: Array[Byte]) extends Box(BoxType.smlm) {
    assert(metadata.length < Short.MaxValue)

    override lazy val payloadSize: Int = 2 + metadata.length

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putShort(metadata.length.toByte)
      buf.put(metadata)
      buf
    }
  }

  object SMLM_Box {
    def readFromBuffer(buf: ByteBuffer) = Try {
      val len = buf.getShort()
      val metadata = new Array[Byte](len)
      buf.get(metadata)
      SMLM_Box(metadata)
    }
  }


  final case class SLFR_Box(
    frameIndex: Int,
    eventsData: Array[Byte],
    currState: Option[Array[Byte]]
  ) extends Box(BoxType.slfr) {
    override lazy val payloadSize: Int = 4 + 4 + eventsData.length + 1 + currState.map(_.length + 4).getOrElse(0)

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(frameIndex)
      buf.putInt(eventsData.length.toByte)
      buf.put(eventsData)
      currState match {
        case Some(arr) =>
          buf.put(1.toByte)
          buf.putInt(arr.length)
          buf.put(arr)
        case None =>
          buf.put(0.toByte)
      }

      buf
    }
  }

  object SLFR_Box {
    def readFromBuffer(buf: ByteBuffer) = Try {
      val frameIndex = buf.getInt()
      val len = buf.getInt()
      val eventsData = new Array[Byte](len)
      buf.get(eventsData)
      val hasState = buf.get() == 1
      val state =
        if (hasState) {
          val l = buf.getInt()
          val stateData = new Array[Byte](l)
          buf.get(stateData)
          Some(stateData)
        } else {
          None
        }
      SLFR_Box(frameIndex, eventsData, state)
    }
  }


}
