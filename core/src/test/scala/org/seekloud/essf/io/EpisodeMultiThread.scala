package org.seekloud.essf.io

import org.scalatest.AsyncFlatSpec

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
/**
  * Created by hongruying on 2018/11/9
  */
class EpisodeMultiThread extends AsyncFlatSpec {

  implicit override def executionContext = scala.concurrent.ExecutionContext.Implicits.global

  import TestUtils._


  def tmpFile(file: String): String = {
    testFile("episodeMultiThread", file)
  }

  it can "read episode record success in multi-thread" in {
    val file = tmpFile("multithread.essf")

    val frames = getRandomFrames(36000, 0.3, 0.02)
    val output = getOutputStream(file)

    frames.foreach {
      case Some(FrameData(idx, d1, d2)) =>
        val r = output.writeFrame(d1, d2)
        assert(idx == r)
      case None => output.writeEmptyFrame()
    }
    output.finish()


    val futureReadTasks = (1 to 100).map(i => Future{readAllFrame(file, i)})

    Future.sequence(futureReadTasks) map{ rst =>
      assert(
        !rst.exists{ case (readRst, frameCount) =>
          (!readRst.equals(frames)) || frameCount != frames.length
        }
      )
    }

  }

  def readAllFrame(file:String, threadId:Int) = {
    println(s"start read file in thread-${threadId}")
    val (input, epInfo) = getInputStream(file)
    val arrayBuffer = new ArrayBuffer[Option[FrameData]]()
    while (input.hasMoreFrame) {
      input.readFrame() match {
        case Some(f) =>
          if (f.eventsData.length == 0 && f.stateData.isEmpty) {
            arrayBuffer.append(None)
          } else {
            arrayBuffer.append(Some(f))
          }
        case None =>
      }
    }

    val rst = arrayBuffer.toIndexedSeq
    input.close()
    println(s"read file finish in thread-${threadId}")
    (rst, epInfo.frameCount)
  }


}
