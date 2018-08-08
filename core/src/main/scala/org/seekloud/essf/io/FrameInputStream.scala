package org.seekloud.essf.io

import org.seekloud.essf.Utils
import org.seekloud.essf.box._

import scala.collection.mutable
import scala.collection.immutable
import scala.util.Random
import org.seekloud.essf.box.Boxes

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 11:56 PM
  */
class FrameInputStream(file: String) {

  private val boxReader = new ESSFReader(file)
  private var position = 0l
  private var epInfo: Option[EpisodeInfo] = None
  private var nextBox: Option[Box] = None
  private val snapshotIndexMap: java.util.TreeMap[Int, Long] = new java.util.TreeMap[Int, Long]()
  private var framePosition = 0


  def init(): (SimulatorInfo, EpisodeInfo) = {
    nextBox = Some(boxReader.get()) //First box must be exist.
    val fileMeta = readBox().asInstanceOf[Boxes.FileMeta]
    val boxPositionBox = readBox().asInstanceOf[Boxes.BoxPosition]
    val epInformation = readBox().asInstanceOf[Boxes.EpisodeInform]
    val simulatorInform = readBox().asInstanceOf[Boxes.SimulatorInform]
    val simulatorMeta = readBox().asInstanceOf[Boxes.SimulatorMetadata]
    val initState = readBox().asInstanceOf[Boxes.InitState]

    val snapshotIndexBox = readBox(boxPositionBox.snapshotPos).asInstanceOf[Boxes.SnapshotPosition]
    snapshotIndexBox.snapshotIndex.foreach {
      case (k, v) => snapshotIndexMap.put(k, v)
    }

    val info = EpisodeInfo(
      fileMeta.version,
      epInformation.frameCount,
      epInformation.snapshotCount,
      fileMeta.createTime)

    //println(s"init input: frameCount=${epInformation.frameCount}")

    epInfo = Some(info)
    (SimulatorInfo(
      simulatorInform.id,
      simulatorInform.version,
      simulatorMeta.metadata,
      initState.stateData
    ), info)
  }

  private[this] def readBox(): Box = {
    val box = nextBox.get
    nextBox = boxReader.get() match {
      case _: Boxes.EndOfFile => None
      case other => Some(other)
    }
    position += box.size
    box
  }

  private[this] def readBox(pos: Long): Box = {
    boxReader.get(pos)
  }

  def hasMoreFrame: Boolean = {
    framePosition < epInfo.get.frameCount
  }


  def readFrame(): Option[FrameData] = {
    val curr = framePosition

    @inline
    def updatePosition(): Unit = {
      readBox() // update, important
      framePosition += 1
    }

    nextBox match {
      case Some(Boxes.SimulatorFrame(idx, eData, sData)) =>
        assert(idx == framePosition, s"frame position mismatch: $idx != $framePosition")
        updatePosition()
        Some(FrameData(curr, eData, sData))
      case Some(Boxes.EmptyFrame()) =>
        updatePosition()
        Some(FrameData(curr, Utils.EmptyByteArray, None))
      case _ =>
        //println(s"!!! no more frame framePosition=$framePosition vs epInfo.get.frameCount=${epInfo.get.frameCount}")
        framePosition = epInfo.get.frameCount
        None
    }

  }

  def gotoSnapshot(idx: Int): Int = {
    val entry =
      if (idx < 0) {
        snapshotIndexMap.firstEntry()
      } else {
        snapshotIndexMap.floorEntry(idx)
      }
    val frameIdx = entry.getKey
    framePosition = frameIdx
    position = entry.getValue
    boxReader.position(position)
    nextBox = Some(boxReader.get()) //First box must be exist.
    frameIdx
  }

  def gotoSnapshotByRatio(ratio: Double): Int = {
    assert(ratio >= 0.000000000001)
    assert(ratio <= 1.000000000001)
    val idx = epInfo.get.frameCount * ratio
    gotoSnapshot(Math.ceil(idx).toInt)
  }

  def close(): Unit ={
    boxReader.close()
  }


}
