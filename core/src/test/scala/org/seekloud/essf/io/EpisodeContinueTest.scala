package org.seekloud.essf.io

import org.seekloud.essf.test.UnitSpec

/**
  * User: Taoz
  * Date: 8/9/2018
  * Time: 4:19 PM
  */
class EpisodeContinueTest extends UnitSpec {




  import TestUtils._


  def tmpFile(file: String): String = {
    testFile("episodeContinueTest", file)
  }


  "A Episode" can " continue with a finished file " in {
    val file = tmpFile("continue1.essf")
    val output = getOutputStream(file)

    val frames: IndexedSeq[Option[FrameData]] = IndexedSeq(
      Some(FrameData(0, str2bytes("0aaa"), None)),
      Some(FrameData(1, str2bytes("1aaa"), None)),
      None,
      Some(FrameData(3, str2bytes("3aaa"), None)),
      Some(FrameData(4, str2bytes("4aaa"), Some(str2bytes("ss11")))),
      Some(FrameData(5, str2bytes("5aaa"), None)),
      None,
      Some(FrameData(7, str2bytes("7aaa"), None))
    )

    frames.foreach {
      case Some(fd) =>
        val r = output.writeFrame(fd.eventsData, fd.stateData)
        if (fd.frameIndex != r) {
          throw new EssfIOException("frame index should equal.")
        }
      case None => output.writeEmptyFrame()
    }
    output.finish()

    val continueOutput = new FrameOutputStream(file)
    val eData = "8aaa".getBytes()
    val sData = "ss22".getBytes()
    val continueIndex = continueOutput.continue(simulatorId, dataVersion, eData, sData)

    val frames1: IndexedSeq[Option[FrameData]] = IndexedSeq(
      Some(FrameData(9, str2bytes("9aaa"), None)),
      Some(FrameData(10, str2bytes("10aaa"), None)),
      None,
      Some(FrameData(12, str2bytes("12aaa"), None)),
      Some(FrameData(13, str2bytes("13aaa"), Some(str2bytes("ss33")))),
      Some(FrameData(14, str2bytes("15aaa"), None))
    )

    frames1.foreach {
      case Some(fd) =>
        val r = continueOutput.writeFrame(fd.eventsData, fd.stateData)
        if (fd.frameIndex != r) {
          throw new EssfIOException("frame index should equal.")
        }
      case None => continueOutput.writeEmptyFrame()
    }
    continueOutput.finish()

    val expectList = List(
      FrameData(0, str2bytes("0aaa"), None),
      FrameData(1, str2bytes("1aaa"), None),
      FrameData(2, new Array[Byte](0), None),
      FrameData(3, str2bytes("3aaa"), None),
      FrameData(4, str2bytes("4aaa"), Some(str2bytes("ss11"))),
      FrameData(5, str2bytes("5aaa"), None),
      FrameData(6, new Array[Byte](0), None),
      FrameData(7, str2bytes("7aaa"), None),
      FrameData(8, str2bytes("8aaa"), Some(str2bytes("ss22"))),
      FrameData(9, str2bytes("9aaa"), None),
      FrameData(10, str2bytes("10aaa"), None),
      FrameData(11, new Array[Byte](0), None),
      FrameData(12, str2bytes("12aaa"), None),
      FrameData(13, str2bytes("13aaa"), Some(str2bytes("ss33"))),
      FrameData(14, str2bytes("15aaa"), None)
    )

    val (input, epInfo) = getInputStream(file)
    val targets = readFrames(15, input)

    assert(
      continueIndex == 8 &&
      epInfo.frameCount == 15 &&
      epInfo.snapshotCount == 3 &&
      targets.equals(expectList)
    )

  }


  it should "work with incomplete file" in {
    val file = tmpFile("continue2.essf")
    val output = getOutputStream(file)

    val frames: IndexedSeq[Option[FrameData]] = IndexedSeq(
      Some(FrameData(0, str2bytes("0aaa"), None)),
      Some(FrameData(1, str2bytes("1aaa"), None)),
      None,
      Some(FrameData(3, str2bytes("3aaa"), None)),
      Some(FrameData(4, str2bytes("4aaa"), Some(str2bytes("ss11")))),
      Some(FrameData(5, str2bytes("5aaa"), None)),
      None,
      Some(FrameData(7, str2bytes("7aaa"), None)),
      Some(FrameData(8, str2bytes("8aaa"), None))
    )

    frames.foreach {
      case Some(fd) =>
        val r = output.writeFrame(fd.eventsData, fd.stateData)
        if (fd.frameIndex != r) {
          throw new EssfIOException("frame index should equal.")
        }
      case None => output.writeEmptyFrame()
    }
    output.finish()

    val file2 = tmpFile("continue2_1.essf")

    val (input0, epInfo) = getInputStream(file)
    input0.readFrame()
    input0.readFrame()
    input0.readFrame()
    input0.readFrame()
    input0.readFrame()
    input0.readFrame()
    input0.close()
    val length = input0.getFilePosition + 5

    println(s"length: $length")

    copyPartFile(file, file2, length)

    val fixer = new FrameOutputStream(file2)
    fixer.fix()

    val expectedList1 = List(
      FrameData(0, str2bytes("0aaa"), None),
      FrameData(1, str2bytes("1aaa"), None),
      FrameData(2, new Array[Byte](0), None),
      FrameData(3, str2bytes("3aaa"), None),
      FrameData(4, str2bytes("4aaa"), Some(str2bytes("ss11"))),
      FrameData(5, str2bytes("5aaa"), None)
    )


    val expectedList2 = List(
      FrameData(4, str2bytes("4aaa"), Some(str2bytes("ss11"))),
      FrameData(5, str2bytes("5aaa"), None)
    )


    val (input1, epInfo1) = getInputStream(file2)
    val targets1 = readFrames(6, input1)

    input1.gotoSnapshot(4)
    val targets2 = readFrames(2, input1)

    input1.gotoSnapshot(5)
    val targets3 = readFrames(2, input1)



    assert(
      targets1.equals(expectedList1) &&
      targets2.equals(expectedList2) &&
      targets3.equals(expectedList2) &&
      epInfo1.frameCount == 6 &&
      epInfo1.snapshotCount == 1
    )


  }


}
