package org.seekloud.essf.io

import java.util.{Map, NoSuchElementException}

import org.seekloud.essf.Utils
import org.seekloud.essf.box.Boxes.BoxIndexes
import org.seekloud.essf.box.{Boxes, _}
import org.seekloud.essf.common.Constants

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
  private var boxPositions: Option[BoxIndexes] = None


  def getSnapshotIndexes: List[(Int, Long)] = {
    import collection.JavaConverters._
    snapshotIndexMap.asScala.iterator.toList
  }

  def getEndOfFramePosition: Long = boxPositions.get.endOfFramePos

  def getBoxPositions: BoxIndexes = boxPositions.get

  def getFilePosition: Long = position

  def getFramePosition: Int = framePosition


  def reset(): EpisodeInfo = {
    framePosition = 0
    snapshotIndexMap.clear()
    boxReader.position(0)
    init()
  }


  def init(withSnapshot: Boolean = true): EpisodeInfo = {
    nextBox = Some(boxReader.get()) //First box must be exist.
    val fileMeta = readBox().asInstanceOf[Boxes.FileMeta]
    boxPositions = Some(readBox().asInstanceOf[Boxes.BoxIndexes])
    val epInformation = readBox().asInstanceOf[Boxes.EpisodeInform]
    val epStatus = readBox().asInstanceOf[Boxes.EpisodeStatus]
    val simulatorInform = readBox().asInstanceOf[Boxes.SimulatorInform]
    val simulatorMeta = readBox().asInstanceOf[Boxes.SimulatorMetadata]
    val initState = readBox().asInstanceOf[Boxes.InitState]

    if (withSnapshot) {
      val snapshotIndexBox = readBox(boxPositions.get.snapshotPos).asInstanceOf[Boxes.SnapshotPosition]
      snapshotIndexBox.snapshotIndex.foreach {
        case (k, v) => snapshotIndexMap.put(k, v)
      }
    }
    //println(s"init input: frameCount=${epInformation.frameCount}")

    //[DANGEROUS HERE!!!] moving positions,
    val beginOfFramePos = boxPositions.get.beginOfFramePos
    boxReader.position(beginOfFramePos)
    nextBox = Some(boxReader.get())
    position = beginOfFramePos
    readBox()


    val info = EpisodeInfo(fileMeta.version,
      epInformation.frameCount,
      epInformation.snapshotCount,
      fileMeta.createTime,
      epStatus.isFinished,
      simulatorInform.id,
      simulatorInform.version,
      simulatorMeta.metadata,
      initState.stateData)

    if (withSnapshot && !epStatus.isFinished) {
      //warming here: "this file is incomplete, please fix it first."
      boxReader.close()
    }

    epInfo = Some(info)

    info
  }

  private[io] def getNextBox: Option[Box] = nextBox

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
        Some(FrameData(curr, Constants.EmptyByteArray, None))
      case _ =>
        //println(s"!!! no more frame framePosition=$framePosition vs epInfo.get.frameCount=${epInfo.get.frameCount}")
        framePosition = epInfo.get.frameCount
        None
    }

  }

  def getFirstSnapshotFrameIndex: Int = {
    try
      snapshotIndexMap.firstKey()
    catch {
      case _: NoSuchElementException => -1
    }
  }

  def getLastSnapshotFrameIndex: Int = {
    try
      snapshotIndexMap.lastKey()
    catch {
      case _: NoSuchElementException => -1
    }
  }

  def gotoFirstSnapshot(): Int = {
    gotoSnapshotByEntry(
      snapshotIndexMap.firstEntry()
    )
  }

  def gotoLastSnapshot(): Int = {
    gotoSnapshotByEntry(
      snapshotIndexMap.lastEntry()
    )
  }

  private[this] def gotoSnapshotByEntry(entry: Map.Entry[Int, Long]) = {
    if (entry == null) {
      -1
    } else {
      val frameIdx = entry.getKey
      framePosition = frameIdx
      position = entry.getValue
      boxReader.position(position)
      nextBox = Some(boxReader.get()) //First box must be exist.
      frameIdx
    }
  }

  def gotoSnapshot(idx: Int): Int = {
    val entry =
      if (idx < 0) {
        snapshotIndexMap.firstEntry()
      } else {
        val tmp = snapshotIndexMap.floorEntry(idx)
        if (tmp == null) {
          snapshotIndexMap.firstEntry()
        } else {
          tmp
        }
      }
    gotoSnapshotByEntry(entry)
  }

  def gotoSnapshotByRatio(ratio: Double): Int = {
    assert(ratio >= 0.000000000001)
    assert(ratio <= 1.000000000001)
    val idx = epInfo.get.frameCount * ratio
    gotoSnapshot(Math.ceil(idx).toInt)
  }

  def close(): Unit = {
    boxReader.close()
  }

}




