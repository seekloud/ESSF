package org.seekloud.essf.io

import org.seekloud.essf.Utils
import org.seekloud.essf.test.UnitSpec

import scala.collection.mutable.ArrayBuffer

/**
  * User: Taoz
  * Date: 8/8/2018
  * Time: 4:32 PM
  */
class EpisodeBaseTest extends UnitSpec {

  import TestUtils._


  def tmpFile(file: String): String = {
    testFile("episodeBase", file)
  }


  "An Episode" should "init correctly" in {
    val file = tmpFile("initCorrectly.essf")
    val metadata = "abcdeflalalal你好世界.!@#$".getBytes(charset)
    val initState = "123456abcdef2lalal你好世界.!@#$".getBytes(charset)
    val output = new FrameOutputStream(file)
    output.init(simulatorId, dataVersion, metadata, initState)
    output.finish()

    val input = new FrameInputStream(file)
    val epInfo = input.init()
    input.close()

    assert(
      simulatorId == epInfo.simulatorId
      && dataVersion == epInfo.simulatorVersion
      && Utils.arrayEquals(metadata, epInfo.simulatorMetadata)
      && Utils.arrayEquals(initState, epInfo.simulatorInitState)
      && epInfo.frameCount == 0
    )
  }


  it should "reset correctly" in {
    val file = tmpFile("initCorrectly.essf")
    val metadata = "abcdeflalalal你好世界.!@#$".getBytes(charset)
    val initState = "123456abcdef2lalal你好世界.!@#$".getBytes(charset)
    val output = new FrameOutputStream(file)
    output.init(simulatorId, dataVersion, metadata, initState)
    output.finish()

    val input = new FrameInputStream(file)
    input.init()
    val epInfo = input.reset()

    assert(
      simulatorId == epInfo.simulatorId
      && dataVersion == epInfo.simulatorVersion
      && Utils.arrayEquals(metadata, epInfo.simulatorMetadata)
      && Utils.arrayEquals(initState, epInfo.simulatorInitState)
      && epInfo.frameCount == 0
    )
  }


  it can "keep one frame" in {
    val file = tmpFile("keepOneFrame1.essf")
    //val data1 = rdm.nextString(5142).getBytes(charset)
    val data1 = "abcdefg".getBytes(charset)

    val output = getOutputStream(file)
    output.writeFrame(data1)
    output.finish()

    val (input, epInfo) = getInputStream(file)
    val f1 = input.readFrame()
    val f2 = input.readFrame()
    input.close()

//    println(s"f1:$f1")
//    println(s"f2:$f2")

    assert(
      f1.contains(FrameData(0, data1, None)) &&
      epInfo.frameCount == 1 &&
      f2.isEmpty
    )
  }


  it can "keep one frame without data" in {
    val file = tmpFile("keepOneFrame2.essf")
    val data1 = new Array[Byte](0)

    val output = getOutputStream(file)
    output.writeFrame(data1)
    output.finish()

    val (input, epInfo) = getInputStream(file)
    val f1 = input.readFrame()
    val f2 = input.readFrame()
    input.close()

    assert(
      f1.contains(FrameData(0, data1, None)) &&
      epInfo.frameCount == 1 &&
      f2.isEmpty
    )
  }


  it can "keep one empty frame" in {
    val file = tmpFile("keepOneFrame3.essf")

    val output = getOutputStream(file)
    output.writeEmptyFrame()
    output.finish()

    val (input, epInfo) = getInputStream(file)
    val f1 = input.readFrame()
    val f2 = input.readFrame()
    input.close()

    assert(
      f1.contains(FrameData(0, new Array[Byte](0), None)) &&
      epInfo.frameCount == 1 &&
      f2.isEmpty
    )
  }


  it can "keep one frame with snapshot" in {
    val file = tmpFile("keepOneFrame4.essf")
    val data1 = rdm.nextString(5100).getBytes(charset)
    val data2 = rdm.nextString(1512001).getBytes(charset)

    val output = getOutputStream(file)
    output.writeFrame(data1, Some(data2))
    output.finish()

    val (input, epInfo) = getInputStream(file)
    val f1 = input.readFrame()
    val f2 = input.readFrame()
    input.close()

    assert(
      f1.contains(FrameData(0, data1, Some(data2))) &&
      epInfo.frameCount == 1 &&
      f2.isEmpty
    )
  }

  it can "keep one frame with empty events but some state" in {
    val file = tmpFile("keepOneFrame5.essf")
    val data1 = new Array[Byte](0)
    val data2 = rdm.nextString(1512001).getBytes(charset)

    val output = getOutputStream(file)
    output.writeFrame(data1, Some(data2))
    output.finish()

    val (input, epInfo) = getInputStream(file)
    val f1 = input.readFrame()
    val f2 = input.readFrame()
    input.close()

    assert(
      f1.contains(FrameData(0, data1, Some(data2))) &&
      epInfo.frameCount == 1 &&
      f2.isEmpty
    )
  }


  it can "keep two frames" in {
    val file = tmpFile("keepTwoFrame.essf")
    val data1 = rdm.nextString(512000).getBytes(charset)
    val data2 = rdm.nextString(112001).getBytes(charset)
    val data3 = rdm.nextString(1512001).getBytes(charset)

    val output = getOutputStream(file)
    output.writeFrame(data1, Some(data2))
    output.writeFrame(data3)
    output.finish()

    val (input, epInfo) = getInputStream(file)
    val f1 = input.readFrame()
    val f2 = input.readFrame()
    val f3 = input.readFrame()
    val f4 = input.readFrame()
    input.close()

    assert(
      f1.contains(FrameData(0, data1, Some(data2))) &&
      f2.contains(FrameData(1, data3, None)) &&
      epInfo.frameCount == 2 &&
      f3.isEmpty &&
      f4.isEmpty
    )
  }

  it can "keep two frames with last one empty" in {
    val file = tmpFile("keepTwoFrame1.essf")
    val data1 = rdm.nextString(5000).getBytes(charset)
    val data2 = rdm.nextString(1121).getBytes(charset)

    val output = getOutputStream(file)
    output.writeFrame(data1, Some(data2))
    output.writeEmptyFrame()
    output.finish()

    val (input, epInfo) = getInputStream(file)
    val f1 = input.readFrame()
    val f2 = input.readFrame()
    val f3 = input.readFrame()
    val f4 = input.readFrame()
    input.close()

    assert(
      f1.contains(FrameData(0, data1, Some(data2))) &&
      f2.contains(FrameData(1, new Array[Byte](0), None)) &&
      epInfo.frameCount == 2 &&
      f3.isEmpty &&
      f4.isEmpty
    )
  }


  it can "keep two frames with first one empty" in {
    val file = tmpFile("keepTwoFrame2.essf")
    val data1 = rdm.nextString(512000).getBytes(charset)
    val data2 = rdm.nextString(112001).getBytes(charset)
    val data3 = rdm.nextString(1512001).getBytes(charset)

    val output = getOutputStream(file)
    output.writeEmptyFrame()
    output.writeFrame(data1, Some(data2))
    output.finish()

    val (input, epInfo) = getInputStream(file)
    val f1 = input.readFrame()
    val f2 = input.readFrame()
    val f3 = input.readFrame()
    val f4 = input.readFrame()
    input.close()

    assert(
      f1.contains(FrameData(0, new Array[Byte](0), None)) &&
      f2.contains(FrameData(1, data1, Some(data2))) &&
      epInfo.frameCount == 2 &&
      f3.isEmpty &&
      f4.isEmpty
    )
  }


  it can "keep many frames" in {
    val file = tmpFile("keepManyFrame.essf")

    val frames = getRandomFrames(36000, 0.3, 0.02)
    val output = getOutputStream(file)

    frames.foreach {
      case Some(FrameData(idx, d1, d2)) =>
        val r = output.writeFrame(d1, d2)
        assert(idx == r)
      case None => output.writeEmptyFrame()
    }
    output.finish()

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

    /*    println(s"FRAME COUNT=${epInfo.frameCount}")
        println(s"SNAPSHOT COUNT=${epInfo.snapshotCount}")*/

    assert(
      rst.equals(frames) &&
      epInfo.frameCount == frames.length
    )
  }


  it can "keep many frames with reset" in {
    val file = tmpFile("keepManyFrame2.essf")
    val len = 36000

    val frames = getRandomFrames(len, 0.3, 0.02)
    val output = getOutputStream(file)

    frames.foreach {
      case Some(FrameData(idx, d1, d2)) =>
        val r = output.writeFrame(d1, d2)
        assert(idx == r)
      case None => output.writeEmptyFrame()
    }
    output.finish()


    val (input, epInfo) = getInputStream(file)

    (0 until 5).foreach { _ =>
      val stop = rdm.nextInt(len)
      (0 until stop).foreach(_ => input.readFrame())
      input.reset()
//      println(s"RESET input at $stop")
    }

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

    /*    println(s"FRAME COUNT=${epInfo.frameCount}")
        println(s"SNAPSHOT COUNT=${epInfo.snapshotCount}")*/

    assert(
      rst.equals(frames) &&
      epInfo.frameCount == frames.length
    )
  }


}
