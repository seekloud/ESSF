package org.seekloud.essf.io

import java.io.File

import org.seekloud.essf.box.{Box, Boxes}

import scala.annotation.tailrec


/**
  * Created by hongruying on 2018/9/1
  */
private[essf] class TemporaryBuffer(targetFile: String){


  private[this] lazy val tmpBufferFile = s"$targetFile.tmp"
  private[this] var tmpBufferWriter: Option[ESSFWriter] = None

  def init(): Unit = {
    val tmpFile = new File(tmpBufferFile)
    if(tmpFile.exists()){
      tmpFile.delete()
    }
    tmpBufferWriter = Some(new ESSFWriter(tmpBufferFile))
    tmpBufferWriter.foreach(_.position(0L))

  }



  def write2Buffer(box:Box): Unit = {
    tmpBufferWriter.foreach{ writer =>
      writer.put(box)
    }
  }


  // fix file, get boxIndex and mutableInfoMap from tmp file
  def readBuffer(): Iterable[Box] = {
    val tmpFile = new File(tmpBufferFile)
    if(tmpFile.exists() && tmpFile.length() > 0){
      val reader = new ESSFReader(tmpBufferFile)
      var boxes: List[Box] = Nil
      try {
        while (true){
          boxes = reader.get() :: boxes
        }
      } catch {
        case e: Exception =>
          println(s"fix [$tmpBufferFile] got an EXPECTED exception[${e.getClass}]: ${e.getMessage}")
//          e.printStackTrace()
      } finally {
        reader.close()
      }
      boxes.reverse
    } else Nil
  }

  def close(): Unit = {
    tmpBufferWriter.foreach(_.close())
    val tmpFile = new File(tmpBufferFile)
    if(tmpFile.exists()){
      tmpFile.delete()
    }
  }


}