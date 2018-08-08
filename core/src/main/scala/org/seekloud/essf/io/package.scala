package org.seekloud.essf

/**
  * User: Taoz
  * Date: 8/7/2018
  * Time: 2:57 PM
  */
package object io {


  val IO_VERSION: Byte = 1.toByte


  final case class EpisodeInfo(
    version: Byte,
    frameCount: Int,
    snapshotCount: Int,
    createTime: Long)

  final case class SimulatorInfo(id: String, version: String, metadata: Array[Byte])

  final case class FrameData(frameIndex: Int, eventsData: Array[Byte], stateData: Option[Array[Byte]])


  class EssfIOException(msg: String = "") extends Exception {
    override def getMessage: String = msg
  }



}
