package org.seekloud.essf.io

import org.seekloud.essf.Utils
import org.seekloud.essf.test.UnitSpec

import scala.collection.immutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * User: Taoz
  * Date: 8/8/2018
  * Time: 4:32 PM
  */
class EpisodeTest extends UnitSpec {

  import Utils._

  val charset = "utf-8"


  def tmpFile(file: String): String = {
    "test_data/episodeTest/" + file
  }

  val simulatorId = "bigSnake"
  val dataVersion = "1.0.3"


  "An Episode" should "init correctly" in  {
    val file = tmpFile("initCorrectly.essf")
    val metadata = "abcdeflalalal你好世界.!@#$".getBytes(charset)
    val initState = "123456abcdef2lalal你好世界.!@#$".getBytes(charset)
    val output = new FrameOutputStream(file)
    output.init(simulatorId, dataVersion, metadata, initState)
    output.finish()

    val input = new FrameInputStream(file)
    val (inform, epInfo) = input.init()
    input.close()

    assert(
      simulatorId == inform.id
      && dataVersion == inform.version
      && Utils.arrayEquals(metadata, inform.metadata)
      && Utils.arrayEquals(initState, inform.initState)
      && epInfo.frameCount == 0
    )
  }

  def getOutput(file: String): FrameOutputStream = {
    val metadata = "abcdeflalaaalal你好世界.!@#$".getBytes(charset)
    val initState = "cc123456abcdef2lalal你好世界.!@#$".getBytes(charset)
    val output = new FrameOutputStream(file)
    output.init(simulatorId, dataVersion, metadata, initState)
    output
  }


  def getInput(file: String): (FrameInputStream, EpisodeInfo) = {
    val input = new FrameInputStream(file)
    val (_, epInfo) = input.init()
    (input, epInfo)
  }


  it can "keep one frame" in {
    val file = tmpFile("keepOneFrame1.essf")
    val data1 = rdm.nextString(5142).getBytes(charset)

    val output = getOutput(file)
    output.writeFrame(data1)
    output.finish()

    val (input, epInfo) = getInput(file)
    val f1 = input.readFrame()
    val f2 = input.readFrame()
    input.close()

    assert(
      f1.contains(FrameData(0, data1, None)) &&
      epInfo.frameCount == 1 &&
      f2.isEmpty
    )
  }


  it can "keep one frame without data" in {
    val file = tmpFile("keepOneFrame2.essf")
    val data1 = new Array[Byte](0)

    val output = getOutput(file)
    output.writeFrame(data1)
    output.finish()

    val (input, epInfo) = getInput(file)
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

    val output = getOutput(file)
    output.writeEmptyFrame()
    output.finish()

    val (input, epInfo) = getInput(file)
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

    val output = getOutput(file)
    output.writeFrame(data1, Some(data2))
    output.finish()

    val (input, epInfo) = getInput(file)
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

    val output = getOutput(file)
    output.writeFrame(data1, Some(data2))
    output.writeFrame(data3)
    output.finish()

    val (input, epInfo) = getInput(file)
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
    val data3 = rdm.nextString(15101).getBytes(charset)

    val output = getOutput(file)
    output.writeFrame(data1, Some(data2))
    output.writeEmptyFrame()
    output.finish()

    val (input, epInfo) = getInput(file)
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

    val output = getOutput(file)
    output.writeEmptyFrame()
    output.writeFrame(data1, Some(data2))
    output.finish()

    val (input, epInfo) = getInput(file)
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


  def getRandomFrames(
    len: Int,
    ratio1: Double,
    ratio2: Double
  ): immutable.IndexedSeq[Option[FrameData]] = {

    val data1 = new Array[Byte](8 * 1024)
    val data2 = new Array[Byte](29 * 1024)





    def getFrame(idx: Int): Option[FrameData] = {
      rdm.nextDouble() match {
        case x if x > ratio1 =>
          rdm.nextBytes(data1)
          rdm.nextDouble() match {
            case y if y > ratio2 =>
              rdm.nextBytes(data2)
              Some(FrameData(idx, data1, Some(data2)))
            case _ => Some(FrameData(idx, data1, None))
          }
        case _ => None
      }
    }

    (0 until len).map(i => getFrame(i))
  }


  it can "keep many frames" in {
    val file = tmpFile("keepManyFrame2.essf")

    val frames = getRandomFrames(10000, 0.5, 0.95)
/*    val data1 = "hello".getBytes()
    val data2 = "ok".getBytes()
    val frames = IndexedSeq(
      Some(FrameData(0, data1, None)),
      None,
      Some(FrameData(2, data2, None))
    )*/
    val output = getOutput(file)

    frames.foreach{
      case Some(FrameData(idx, d1, d2)) =>
        val r = output.writeFrame(d1, d2)
        assert(idx == r)
      case None => output.writeEmptyFrame()
    }
    output.finish()

    val (input, epInfo) = getInput(file)
    val arrayBuffer = new ArrayBuffer[Option[FrameData]]()
    while(input.hasMoreFrame) {
      input.readFrame() match {
        case Some(f) =>
          if(f.eventsData.length == 0) {
            arrayBuffer.append(None)
          } else {
            arrayBuffer.append(Some(f))
          }
        case None =>
      }
    }

    val rst = arrayBuffer.toIndexedSeq
    input.close()

/*
    println("++++++++++++++++++++++++++++++++++++++++++++++++ ")
    println(frames.mkString("\n"))
    println("++++++++++++++++++++++++++++++++++++++++++++++++ ")
    println(rst.mkString("\n"))
*/


    println(s"SNAPSHOT COUNT=${epInfo.snapshotCount}")

    assert(
      rst.equals(frames) &&
      epInfo.frameCount == frames.length
    )
  }


}
