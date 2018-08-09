package org.seekloud.essf

/**
  * User: Taoz
  * Date: 8/7/2018
  * Time: 2:57 PM
  */
package object io {


  val IO_VERSION: Byte = 1.toByte


  final case class EpisodeInfo(
    episodeVersion: Byte,
    frameCount: Int,
    snapshotCount: Int,
    createTime: Long,
    simulatorId: String,
    simulatorVersion: String,
    metadata: Array[Byte],
    initState: Array[Byte]
  )


  final case class FrameData(frameIndex: Int, eventsData: Array[Byte], stateData: Option[Array[Byte]]) {
    override def equals(obj: scala.Any): Boolean = {
      obj match {
        case FrameData(idx, ev, st) =>
          if (idx == frameIndex && Utils.arrayEquals(ev, eventsData)) {
            (stateData, st) match {
              case (Some(s1), Some(s2)) => Utils.arrayEquals(s1, s2)
              case (None, None) => true
              case _ => false
            }
          } else false
        case _ => false
      }
    }

    override def toString: String = {
      @inline
      def byte2Str(bytes: Array[Byte]) = {
        if (bytes.length == 0) {
          "[EMPTY]"
        } else {
          new String(bytes, "utf-8")
        }
      }

      val str1 = byte2Str(eventsData)
      val str2 = stateData.map(byte2Str).getOrElse("[NONE]")
      s"FrameData($frameIndex, $str1, $str2)"
    }

  }


  class EssfIOException(msg: String = "") extends Exception {
    override def getMessage: String = msg
  }


}
