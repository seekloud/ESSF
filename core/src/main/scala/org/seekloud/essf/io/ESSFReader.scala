package org.seekloud.essf.io

import java.io.{File, FileInputStream}
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

import org.seekloud.essf.box
import org.seekloud.essf.box._

import scala.util.Try
import org.seekloud.essf.common.Constants._

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 3:55 PM
  */
private[essf] class ESSFReader(file: String) {



  private val fc = new FileInputStream(new File(file)).getChannel
  private val defaultBuffer = ByteBuffer.allocateDirect(DEFAULT_BOX_BUFFER_SIZE)

  import ESSFReader.readHead

  def get(): Box = {
    val (boxSize, boxType, payloadSize) = readHead(fc)
    val buf =
      if (defaultBuffer.capacity() < payloadSize) {
        ByteBuffer.allocate(payloadSize)
      } else defaultBuffer
    buf.clear()
    buf.limit(payloadSize)
    fc.read(buf)
    if (buf.hasRemaining) {
      throw new EssfIOException("buffer not full.")
    }
    buf.flip()
    decodeBox(boxType, buf)
  }

  def get(pos: Long): Box = {
    val mark = fc.position()
    fc.position(pos)
    val box = get()
    fc.position(mark)
    box
  }

  def position(pos: Long): Unit = {
    fc.position(pos)
  }

  private[this] def decodeBox(boxType: String, buf: ByteBuffer): Box = {
    val boxTry: Try[Box] = boxType match {
      case BoxType.`fileMeta` => Boxes.FileMeta.readFromBuffer(buf)
      case BoxType.`boxIndexes` => Boxes.BoxIndexes.readFromBuffer(buf)
      case BoxType.episodeInform => Boxes.EpisodeInform.readFromBuffer(buf)
      case BoxType.episodeStatus => Boxes.EpisodeStatus.readFromBuffer(buf)
      case BoxType.snapshotPosition => Boxes.SnapshotPosition.readFromBuffer(buf)
      case BoxType.`endOfFile` => Boxes.EndOfFile.readFromBuffer(buf)
      case BoxType.`initState` => Boxes.InitState.readFromBuffer(buf)
      case BoxType.`emptyFrame` => Boxes.EmptyFrame.readFromBuffer(buf)
      case BoxType.`simulatorInform` => Boxes.SimulatorInform.readFromBuffer(buf)
      case BoxType.`simulatorMetadata` => Boxes.SimulatorMetadata.readFromBuffer(buf)
      case BoxType.`simulatorFrame` => Boxes.SimulatorFrame.readFromBuffer(buf)
      case BoxType.`endOfFrame` => Boxes.EndOfFrame.readFromBuffer(buf)
      case BoxType.`beginOfFrame` => Boxes.BeginOfFrame.readFromBuffer(buf)
      case BoxType.mutableInfoMap => Boxes.MutableInfoMap.readFromBuffer(buf)
      case BoxType.tmpBufferBoxNum => Boxes.TmpBufferBoxNum.readFromBuffer(buf)
      case BoxType.boxIndexPosition => Boxes.BoxIndexPosition.readFromBuffer(buf)


      //TODO
      case x =>
        throw new EssfIOException(s"unknown boxType error: $x")
    }
    boxTry.get
  }

  def close(): Unit = {
    fc.close()
  }

}

object ESSFReader {


  val HEAD_BUFF_SIZE = 32
  val HEAD_OF_HEAD = 6
  val LARGE_SIZE_SIZE = 4
  private[this] val hBuff = ByteBuffer.allocateDirect(HEAD_BUFF_SIZE)


  private def readHead(fc: FileChannel): (Int, String, Int) = {
    hBuff.clear()
    hBuff.limit(HEAD_OF_HEAD)
    fc.read(hBuff)
    hBuff.flip()
    var boxSize: Int = hBuff.getShort()
    val tmpArr = new Array[Byte](4)
    hBuff.get(tmpArr)
    val boxType = new String(tmpArr, utf8)
    var payloadSize = boxSize - HEAD_OF_HEAD
    if (boxSize == 1) {
      hBuff.clear()
      hBuff.limit(LARGE_SIZE_SIZE)
      fc.read(hBuff)
      hBuff.flip()
      boxSize = hBuff.getInt()
      payloadSize = boxSize - HEAD_OF_HEAD - LARGE_SIZE_SIZE
    }

    (boxSize, boxType, payloadSize)
  }



}
