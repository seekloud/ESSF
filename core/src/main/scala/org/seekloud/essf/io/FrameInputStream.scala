package org.seekloud.essf.io

import org.seekloud.essf.box._

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 11:56 PM
  */
class FrameInputStream(file: String) {

  val VERSION = 1.toByte

  private val boxReader = new ESSFReader(file)
  private var position = 0
  private var epInfo: Option[EpisodeInfo] = None
  private var nextBox: Option[Box] = None
  private var nextFrame: Option[FrameData] = None

  def init(): SimulatorInfo = {
    nextBox = Some(boxReader.get()) //First box must be exist.
    val fileMeta = readBox().asInstanceOf[FLMT_Box]
    val epInformation = readBox().asInstanceOf[EPIF_Box]
    val simulatorId = readBox().asInstanceOf[SMLI_Box]
    val simulatorMeta = readBox().asInstanceOf[SMLM_Box]
    val info = EpisodeInfo(
      fileMeta.version,
      epInformation.frameCount,
      epInformation.frameMilliSeconds,
      epInformation.snapshotCount,
      fileMeta.createTime)


    epInfo = Some(info)
    SimulatorInfo(simulatorId.id, simulatorId.version, simulatorMeta.metadata)
  }

  private[this] def readBox(): Box = {
    val box = nextBox.get
    nextBox = boxReader.get() match {
      case _: EOFL_Box => None
      case other => Some(other)
    }
    position += box.size
    box
  }

  private def hasNext: Boolean = nextBox.isDefined



  def readFrame(): FrameData = {

    val eventBox = readBox().asInstanceOf[SLFR_Box]



    ???
  }

}
