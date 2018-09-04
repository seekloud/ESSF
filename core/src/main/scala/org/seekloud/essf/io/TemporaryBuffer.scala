package org.seekloud.essf.io

import java.io.File
import org.seekloud.essf.box.{Box, Boxes}


/**
  * Created by hongruying on 2018/9/1
  */
private[essf] class TemporaryBuffer(targetFile: String){


  private[this] lazy val tmpBufferFile = s"$targetFile.tmp"
  private[this] var tmpBufferWriter: Option[ESSFWriter] = None

  def init(): Unit = {
    tmpBufferWriter = Some(new ESSFWriter(tmpBufferFile))
  }

  def refreshBuffer(boxes:Iterable[Box]): Unit = {
    tmpBufferWriter.foreach{ writer =>
      writer.position(0L)
      writer.put(Boxes.TmpBufferBoxNum(boxes.size))
      boxes.foreach(writer.put)
    }
  }


  // fix file, get boxIndex and mutableInfoMap from tmp file
  def readBuffer(): Iterable[Box] = {
    val tmpFile = new File(tmpBufferFile)
    if(tmpFile.exists() && tmpFile.length() > 0){
      val reader = new ESSFReader(tmpBufferFile)
      var boxes: List[Box] = Nil
      try {
        var tmpBufferBoxNum = reader.get().asInstanceOf[Boxes.TmpBufferBoxNum].boxNum
        while (tmpBufferBoxNum > 0){
          boxes = reader.get() :: boxes
          tmpBufferBoxNum -= 1
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
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