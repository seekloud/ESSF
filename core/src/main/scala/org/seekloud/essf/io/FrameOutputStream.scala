package org.seekloud.essf.io

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 11:56 PM
  */
class FrameOutputStream(
  targetFile: String,
  frameMilliSeconds: Int
) {


  private var frameCount = 0
  private var snapshotCount = 0

  def writeEvent(bytes: Array[Byte]): Unit = {
    frameCount += 1

  }

  def frameDone(stateOption: Option[Array[Byte]] = None): Unit = {
    snapshotCount += 1


  }

  def close(): Unit = {

  }

}
