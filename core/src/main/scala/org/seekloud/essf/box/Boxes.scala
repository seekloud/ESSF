package org.seekloud.essf.box

import java.nio.ByteBuffer

import org.seekloud.essf.Utils

import scala.util.Try

/**
  * User: Taoz
  * Date: 8/8/2018
  * Time: 1:33 PM
  */
object Boxes {

  /*
    ===============  episode box ==================
    val fileMeta = "flmt"
    val boxPosition = "bxps"
    val episodeInform = "epif"
    val snapshotPosition = "snps"
    val episodeStatus = "epst"
    val endOfFile = "eofl"
   */


  final case class FileMeta(
    version: Byte,
    createTime: Long
  ) extends Box(BoxType.fileMeta) {
    override lazy val payloadSize: Int = 1 + 8

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.put(version)
      buf.putLong(createTime)
      buf
    }
  }

  object FileMeta {
    def readFromBuffer(buf: ByteBuffer): Try[FileMeta] = Try {
      val version = buf.get()
      val createTime = buf.getLong
      FileMeta(version, createTime)
    }
  }


  final case class BoxPosition(
    snapshotPos: Long
  ) extends Box(BoxType.boxPosition) {
    override lazy val payloadSize: Int = 8

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putLong(snapshotPos)
      buf
    }
  }

  object BoxPosition {
    def readFromBuffer(buf: ByteBuffer): Try[BoxPosition] = Try {
      val stateIndexPosition = buf.getLong
      BoxPosition(stateIndexPosition)
    }
  }


  final case class EpisodeInform(frameCount: Int, snapshotCount: Int) extends Box(BoxType.episodeInform) {

    override lazy val payloadSize: Int = 4 + 4

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(frameCount)
      buf.putInt(snapshotCount)
      buf
    }
  }

  object EpisodeInform {
    def readFromBuffer(buf: ByteBuffer): Try[EpisodeInform] = Try {
      val frameCount = buf.getInt()
      val snapshotCount = buf.getInt()
      EpisodeInform(frameCount, snapshotCount)
    }
  }


  final case class SnapshotPosition(
    snapshotIndex: List[(Int, Long)]
  ) extends Box(BoxType.snapshotPosition) {

    override lazy val payloadSize: Int = 12 * snapshotIndex.size + 4

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(snapshotIndex.size)
      snapshotIndex.foreach { case (frameIndex, offset) =>
        buf.putInt(frameIndex)
        buf.putLong(offset)
      }
      buf
    }
  }

  object SnapshotPosition {
    def readFromBuffer(buf: ByteBuffer) = Try {
      var ls = List.empty[(Int, Long)]
      var len = buf.getInt()
      while (len > 0) {
        val frameIndex = buf.getInt()
        val pos = buf.getLong()
        ls ::= (frameIndex, pos)
        len -= 1
      }
      SnapshotPosition(ls.reverse)
    }
  }


  final case class EpisodeStatus(data: Int) extends Box(BoxType.episodeStatus) {

    override lazy val payloadSize: Int = 4

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(data)
      buf
    }

  }

  object EpisodeStatus {
    def readFromBuffer(buf: ByteBuffer) = Try {
      val data = buf.getInt()
      EpisodeStatus(data)
    }
  }


  final case class EndOfFile() extends Box(BoxType.endOfFile) {
    override lazy val payloadSize: Int = 0

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf
    }
  }

  object EndOfFile {
    def readFromBuffer(buf: ByteBuffer) = Try {
      EndOfFile()
    }
  }


  /*
   ===================   simulator box   =====================
    val simulatorInform = "smli"
    val simulatorMetadata = "smlm"
    val simulatorFrame = "slfr"
    val endOfFrame = "eofr"
   */


  final case class SimulatorInform(id: String, version: String) extends Box(BoxType.simulatorInform) {

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

  object SimulatorInform {
    def readFromBuffer(buf: ByteBuffer) = Try {
      val idLen = buf.get()
      val idArr = new Array[Byte](idLen)
      buf.get(idArr)
      val id = new String(idArr, "UTF-8")

      val verLen = buf.get()
      val verArr = new Array[Byte](verLen)
      buf.get(verArr)
      val ver = new String(verArr, "UTF-8")

      SimulatorInform(id, ver)
    }
  }


  final case class SimulatorMetadata(metadata: Array[Byte]) extends Box(BoxType.simulatorMetadata) {
    assert(metadata.length < Short.MaxValue)

    override lazy val payloadSize: Int = 2 + metadata.length

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putShort(metadata.length.toByte)
      buf.put(metadata)
      buf
    }

    override def equals(obj: scala.Any): Boolean = {
      obj match {
        case SimulatorMetadata(data) =>
          Utils.arrayEquals(data, metadata)
        case _ => false
      }
    }
  }

  object SimulatorMetadata {
    def readFromBuffer(buf: ByteBuffer) = Try {
      val len = buf.getShort()
      val metadata = new Array[Byte](len)
      buf.get(metadata)
      SimulatorMetadata(metadata)
    }
  }


  final case class SimulatorFrame(
    frameIndex: Int,
    eventsData: Array[Byte],
    currState: Option[Array[Byte]] = None
  ) extends Box(BoxType.simulatorFrame) {
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

    override def equals(obj: scala.Any): Boolean = {
      obj match {
        case SimulatorFrame(oIdx, oEvents, oSnapshot) =>
          if (oIdx == frameIndex) {
            if (Utils.arrayEquals(eventsData, oEvents)) {
              (currState, oSnapshot) match {
                case (None, None) => true
                case (Some(d1), Some(d2)) => Utils.arrayEquals(d1, d2)
                case _ => false
              }
            } else false
          } else false
        case _ => false
      }
    }
  }

  object SimulatorFrame {
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
      SimulatorFrame(frameIndex, eventsData, state)
    }
  }


  final case class InitState(
    stateData: Array[Byte]
  ) extends Box(BoxType.initState) {
    override lazy val payloadSize: Int = 4 + stateData.length

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(stateData.length)
      buf.put(stateData)
      buf
    }
  }

  object InitState {
    def readFromBuffer(buf: ByteBuffer) = Try {
      val len = buf.getInt()
      val arr = new Array[Byte](len)
      buf.get(arr)
      InitState(arr)
    }
  }

  final case class EmptyFrame() extends Box(BoxType.emptyFrame) {
    override lazy val payloadSize: Int = 0

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf
    }
  }

  object EmptyFrame {
    def readFromBuffer(buf: ByteBuffer) = Try {
      EmptyFrame()
    }
  }


  final case class EndOfFrame() extends Box(BoxType.endOfFrame) {
    override lazy val payloadSize: Int = 0

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf
    }
  }

  object EndOfFrame {
    def readFromBuffer(buf: ByteBuffer) = Try {
      EndOfFrame()
    }
  }


}
