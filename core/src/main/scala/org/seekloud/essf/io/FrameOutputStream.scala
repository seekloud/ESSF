package org.seekloud.essf.io

import org.seekloud.essf.box._

import scala.collection.mutable

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 11:56 PM
  */
class FrameOutputStream(
  targetFile: String
) {

  private[this] val boxPositions = mutable.HashMap.empty[String, Long]

  private[this] var currentFrame = 0
  private[this] var filePosition = 0l
  private[this] var snapShotIndexSeq = List.empty[(Int, Long)]
  private[this] val boxWriter: ESSFWriter = new ESSFWriter(targetFile)

  def init(
    simulatorId: String,
    dataVersion: String,
    simulatorMetadata: Array[Byte],
    initState: Array[Byte]
  ): FrameOutputStream = {
    writeBox(Boxes.FileMeta(IO_VERSION, System.currentTimeMillis()))
    writeBox(Boxes.BoxPosition(-1l), indexIt = true)
    writeBox(Boxes.EpisodeInform(0, 0), indexIt = true)
    writeBox(Boxes.SimulatorInform(simulatorId, dataVersion))
    writeBox(Boxes.SimulatorMetadata(simulatorMetadata))
    writeBox(Boxes.InitState(initState))
    this
  }

  private[this] def updateEpisodeInfo(): Unit = {
    val box = Boxes.EpisodeInform(currentFrame + 1, snapShotIndexSeq.size)
    updateBox(box)
  }

  private[this] def updateBoxPositionBox(): Unit = {
    val box = Boxes.BoxPosition(boxPositions(BoxType.snapshotPosition))
    updateBox(box)
  }

  private[this] def genSnapshotIndexBox(): Unit = {
    writeBox(Boxes.SnapshotPosition(snapShotIndexSeq), indexIt = true)
  }

  private def writeBox(box: Box, indexIt: Boolean = false): Long = {
    val curr = filePosition
    if (indexIt) {
      boxPositions.put(box.boxType, curr)
    }
    filePosition += box.size
    internalWriteBoxToFile(box)
    curr
  }

  private[this] def updateBox(box: Box): Long = {

    val canBeUpdated = box match {
      case _: Boxes.EpisodeInform => true
      case _: Boxes.BoxPosition => true
      case _ => false
    }
    if (canBeUpdated) {
      val pos = boxPositions(box.boxType)
      internalWriteBoxToFile(box, pos)
      pos
    } else {
      throw new EssfIOException(s"[boxType=${box.boxType}] can not be updated.")
    }

  }

  private[this] def internalWriteBoxToFile(
    box: Box,
    position: Long = -1l
  ): Unit = {

    //TODO use a queue in other thread.
    if (position < 0) {
      boxWriter.put(box)
    } else {
      boxWriter.put(box, position)
    }
  }


  def writeFrame(eventsData: Array[Byte], stateData: Option[Array[Byte]] = None): Int = {
    val frameNum = currentFrame
    if (stateData.isDefined) {
      snapShotIndexSeq ::= (frameNum, filePosition)
    }
    writeBox(Boxes.SimulatorFrame(frameNum, eventsData, stateData))
    currentFrame += 1
    frameNum
  }

  def writeEmptyFrame(): Int = {
    val frameNum = currentFrame
    writeBox(Boxes.EmptyFrame())
    currentFrame += 1
    frameNum
  }


  def finish(): Unit = {
    writeBox(Boxes.EndOfFrame())
    updateEpisodeInfo()
    genSnapshotIndexBox()
    writeBox(Boxes.EndOfFile())
    updateBoxPositionBox()
    //wait till all write finished.
    boxWriter.close()
  }

}
