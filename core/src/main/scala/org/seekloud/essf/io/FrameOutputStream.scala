package org.seekloud.essf.io

import org.seekloud.essf.box.Boxes.BoxIndexes
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
  private[this] var boxWriter: Option[ESSFWriter] = None

  def init(
    simulatorId: String,
    dataVersion: String,
    simulatorMetadata: Array[Byte],
    initState: Array[Byte]
  ): FrameOutputStream = {
    boxWriter = Some(new ESSFWriter(targetFile))
    writeBox(Boxes.FileMeta(IO_VERSION, System.currentTimeMillis()))
    writeBox(Boxes.BoxIndexes(filePosition, -1l, -1l, -1l, -1l, -1l))
    writeBox(Boxes.EpisodeInform(0, 0))
    writeBox(Boxes.EpisodeStatus(false))
    writeBox(Boxes.SimulatorInform(simulatorId, dataVersion))
    writeBox(Boxes.SimulatorMetadata(simulatorMetadata))
    writeBox(Boxes.InitState(initState))
    writeBox(Boxes.BeginOfFrame())
    this
  }

  def fix(): Unit = {
    val input = new FrameInputStream(targetFile)
    val epInfos = input.init(withSnapshot = false)
    //assert(!epInfos.isFinished, "this file need no fix.")

    input.getBoxPositions.asMap.map {
      case (k, v) => boxPositions.put(k, v)
    }
    snapShotIndexSeq = input.getSnapshotIndexes.reverse

    var reachToEnd = false
    while (!reachToEnd) {
      try {
        input.readFrame() match {
          case Some(FrameData(_, eventsData, stateData)) =>
            if (stateData.isDefined) {
              snapShotIndexSeq ::= (currentFrame, filePosition)
            }
            filePosition = input.getFilePosition
            currentFrame = input.getFramePosition
          case None =>
            reachToEnd = true
        }
      } catch {
        case e: Exception =>
          input.getNextBox match {
            case Some(box) => box match {
              case b : Boxes.SimulatorFrame =>
                filePosition += b.size
                currentFrame += 1
              case _ =>
            }
            case None =>
          }
          println(s"fix [$targetFile] got an EXPECTED exception[${e.getClass}]: ${e.getMessage}")
          reachToEnd = true
      }
    }
    try {
      input.close()
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

    boxWriter = Some(new ESSFWriter(targetFile))
    boxWriter.get.position(filePosition)
    finish()
  }


  def continue(
    simulatorId: String,
    dataVersion: String,
    eventsData: Array[Byte],
    stateData: Array[Byte]
  ): Int = {
    val input = new FrameInputStream(targetFile)
    val epInfos = input.init()
    input.close()

    snapShotIndexSeq = input.getSnapshotIndexes.reverse
    currentFrame = epInfos.frameCount
    filePosition = input.getEndOfFramePosition
    boxWriter = Some(new ESSFWriter(targetFile))
    boxWriter.get.position(filePosition)

    input.getBoxPositions.asMap.map {
      case (k, v) => boxPositions.put(k, v)
    }

    updateEpisodeStatus(false)
    writeFrame(eventsData, Some(stateData))
  }

  private[this] def updateEpisodeInfo(): Unit = {
    //println(s"update frameCount=$currentFrame")
    val box = Boxes.EpisodeInform(currentFrame, snapShotIndexSeq.size)
    updateBox(box)
  }

  private[this] def updateEpisodeStatus(isFinish: Boolean): Unit = {
    val box = Boxes.EpisodeStatus(isFinish)
    updateBox(box)
  }

  private[this] def updateBoxPositionBox(): Unit = {
    val box = Boxes.BoxIndexes(
      boxPositions(BoxType.boxIndexes),
      boxPositions(BoxType.episodeInform),
      boxPositions(BoxType.episodeStatus),
      boxPositions(BoxType.endOfFrame),
      boxPositions(BoxType.snapshotPosition),
      boxPositions(BoxType.beginOfFrame)
    )
    updateBox(box)
  }

  private[this] def genSnapshotIndexBox(): Unit = {
    writeBox(Boxes.SnapshotPosition(snapShotIndexSeq))
  }

  private def needIndex(box: Box): Boolean = {
    box match {
      case _: Boxes.SimulatorFrame => false
      case _: Boxes.EmptyFrame => false
      case _: Boxes.SnapshotPosition => true
      case _: Boxes.EndOfFrame => true
      case _: Boxes.BoxIndexes => true
      case _: Boxes.EpisodeStatus => true
      case _: Boxes.EpisodeInform => true
      case _: Boxes.BeginOfFrame => true
      case _ => false
    }
  }

  private def writeBox(box: Box): Long = {
    val curr = filePosition
    if (needIndex(box)) {
      boxPositions.put(box.boxType, curr)
    }
    filePosition += box.size
    internalWriteBoxToFile(box)
    curr
  }

  private[this] def updateBox(box: Box): Long = {

    val canBeUpdated = box match {
      case _: Boxes.EpisodeInform => true
      case _: Boxes.BoxIndexes => true
      case _: Boxes.EpisodeStatus => true
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
      boxWriter.get.put(box)
    } else {
      boxWriter.get.put(box, position)
    }
  }


  def writeFrame(eventsData: Array[Byte], stateData: Option[Array[Byte]] = None): Int = {
    val frameNum = currentFrame
    //println(s"write frame: frameNum=$frameNum, after=${frameNum + 1}")
    if (stateData.isDefined) {
      snapShotIndexSeq ::= (frameNum, filePosition)
    }
    writeBox(Boxes.SimulatorFrame(frameNum, eventsData, stateData))
    currentFrame += 1
    frameNum
  }

  def writeConnectFrame(eventsData: Array[Byte], stateData: Array[Byte]): Int = {
    writeFrame(eventsData, Some(stateData))
  }

  def writeEmptyFrame(): Int = {
    val frameNum = currentFrame
    //println(s"write empty frame: frameNum=$frameNum, after=${frameNum + 1}")
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
    updateEpisodeStatus(isFinish = true)
    //wait till all write finished.
    boxWriter.foreach(_.close())
  }

}
