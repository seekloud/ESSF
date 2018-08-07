package org.seekloud.essf.io

import org.seekloud.essf.box._

import scala.concurrent.Future

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 11:56 PM
  */
class FrameOutputStream(
  targetFile: String
) {


  val VERSION: Byte = 1.toByte

  private var currentFrame = 0
  private var snapshotCount = 0
  private var position = 0
  private var snapShotIndexSeq = List.empty[(Int, Long)]
  private val boxWriter: ESSFWriter = new ESSFWriter(targetFile)

  def init(
    simulatorId: String,
    dataVersion: String,
    frameMilliSeconds: Short,
    simulatorMetadata: Array[Byte]
  ): FrameOutputStream = {
    writeBox(FLMT_Box(VERSION, System.currentTimeMillis()))
    writeBox(EPIF_Box(0, frameMilliSeconds, 0))
    writeBox(SMLI_Box(simulatorId, dataVersion))
    writeBox(SMLM_Box(simulatorMetadata))
    this
  }


  private[this] def updateEpisodeInfo(): Unit = {
  }

  private[this] def genSnapshotIndexBox(): Unit = {
  }

  private[this] def writeEOF(): Unit = {
    writeBox(EOFL_Box())
  }

  private[this] def writeBox(box: Box): Int = {
    val curr = position
    position += box.size
    internalWriteBoxToFile(box)
    curr
  }

  private[this] def internalWriteBoxToFile(box: Box): Unit = {
    //TODO use a queue in other thread.
    boxWriter.put(box)
  }


  def writeFrame(eventsData: Array[Byte], stateData: Option[Array[Byte]]): Int = {
    val frameNum = currentFrame
    if(stateData.isDefined) {
      snapShotIndexSeq ::= (frameNum, position)
      snapshotCount += 1
    }
    writeBox(SLFR_Box(frameNum, eventsData, stateData))
    currentFrame += 1
    frameNum
  }


  def finish(): Unit = {
    updateEpisodeInfo()
    genSnapshotIndexBox()
    writeEOF()

    //wait till all write finished.
    boxWriter.close()
  }

}
