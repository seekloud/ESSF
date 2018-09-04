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

  import org.seekloud.essf.common.Constants._

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


  final case class EpisodeStatus(
    isFinished: Boolean
  ) extends Box(BoxType.episodeStatus) {

    override lazy val payloadSize: Int = 1

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      if (isFinished) {
        buf.put(1.toByte)
      } else {
        buf.put(0.toByte)
      }
      buf
    }

  }

  object EpisodeStatus {
    def readFromBuffer(buf: ByteBuffer) = Try {
      val isFinished = buf.get() == 1
      EpisodeStatus(isFinished)
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

  final case class SimulatorInform(id: String, version: String) extends Box(BoxType.simulatorInform) {

    private val idBytes = id.getBytes(utf8)
    private val verBytes = version.getBytes(utf8)
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
      val id = new String(idArr, utf8)

      val verLen = buf.get()
      val verArr = new Array[Byte](verLen)
      buf.get(verArr)
      val ver = new String(verArr, utf8)

      SimulatorInform(id, ver)
    }
  }


  final case class SimulatorMetadata(metadata: Array[Byte]) extends Box(BoxType.simulatorMetadata) {

    override lazy val payloadSize: Int = 4 + metadata.length

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(metadata.length)
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
      val len = buf.getInt()
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
      buf.putInt(eventsData.length)
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

    override def equals(obj: scala.Any): Boolean = {
      obj match {
        case InitState(data) =>
          Utils.arrayEquals(data, stateData)
        case _ => false
      }
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


  final case class BeginOfFrame() extends Box(BoxType.beginOfFrame) {
    override lazy val payloadSize: Int = 0
    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf
    }
  }

  object BeginOfFrame {
    def readFromBuffer(buf: ByteBuffer) = Try {
      BeginOfFrame()
    }
  }

  final case class MutableInfoMap(
                                        infoMap:Map[String,Array[Byte]]
                                        ) extends Box(BoxType.mutableInfoMap){

    override lazy val payloadSize: Int = {
      4 + infoMap.map{
        case (key,value) =>
          8 + key.getBytes(utf8).length + value.length
      }.sum
    }

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(infoMap.size)
      infoMap.foreach{
        case (key,value) =>
          val keyBytes = key.getBytes(utf8)
          buf.putInt(keyBytes.length)
          buf.put(keyBytes)
          buf.putInt(value.length)
          buf.put(value)
      }
      buf
    }

    override def equals(obj: scala.Any): Boolean = {
      obj match {
        case MutableInfoMap(map) =>
          if(map.size == infoMap.size){
            ! map.exists{
              case (key,value) =>
                infoMap.get(key).isEmpty || !Utils.arrayEquals(value, infoMap(key))
            }
          } else false
        case _ => false
      }
    }
  }

  object MutableInfoMap {
    def readFromBuffer(buf: ByteBuffer) = Try {
      var map = Map.empty[String,Array[Byte]]
      var mapSize = buf.getInt()
      while (mapSize > 0){
        val keyLen = buf.getInt()
        val keyBytes = new Array[Byte](keyLen)
        buf.get(keyBytes)
        val valueLen = buf.getInt()
        val value = new Array[Byte](valueLen)
        buf.get(value)
        map += (new String(keyBytes, utf8) -> value)
        mapSize -= 1
      }

      MutableInfoMap(map)
    }
  }


  final case class TmpBufferBoxNum(
                                   boxNum:Int
                                 ) extends Box(BoxType.tmpBufferBoxNum){

    override lazy val payloadSize: Int = 4

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(boxNum)
      buf
    }

  }

  object TmpBufferBoxNum {
    def readFromBuffer(buf: ByteBuffer) = Try {
      val num = buf.getInt()
      TmpBufferBoxNum(num)
    }
  }

  final case class BoxIndexPosition(
                                    position: Long
                                  ) extends Box(BoxType.boxIndexPosition){

    override lazy val payloadSize: Int = 8

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putLong(position)
      buf
    }

  }

  object BoxIndexPosition {
    def readFromBuffer(buf: ByteBuffer) = Try {
      val position = buf.getLong()
      BoxIndexPosition(position)
    }
  }

  final case class BoxIndexes(
                               indexMap: Map[String,Long]
                             ) extends Box(BoxType.boxIndexes) {
    override lazy val payloadSize: Int = {
      4 + indexMap.map{
        case (boxType, _) =>
          12 + boxType.getBytes(utf8).length
      }.sum
    }

    override def writePayload(buf: ByteBuffer): ByteBuffer = {
      buf.putInt(indexMap.size)
      indexMap.foreach{
        case (key,value) =>
          val keyBytes = key.getBytes(utf8)
          buf.putInt(keyBytes.length)
          buf.put(keyBytes)
          buf.putLong(value)
      }
      buf
    }

  }

  object BoxIndexes {
    def readFromBuffer(buf: ByteBuffer): Try[BoxIndexes] = Try {
      var map = Map.empty[String,Long]
      var mapSize = buf.getInt()
      while (mapSize > 0){
        val keyLen = buf.getInt()
        val keyBytes = new Array[Byte](keyLen)
        buf.get(keyBytes)
        val value = buf.getLong()
        map += (new String(keyBytes, utf8) -> value)
        mapSize -= 1
      }

      BoxIndexes(map)
    }
  }





}