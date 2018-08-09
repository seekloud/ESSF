package org.seekloud.essf.io

import org.seekloud.essf.test.UnitSpec

import scala.collection.mutable.ArrayBuffer

/**
  * User: Taoz
  * Date: 8/9/2018
  * Time: 10:59 AM
  */
class EpisodeAdvanceTest extends UnitSpec {

  import TestUtils._


  def tmpFile(file: String): String = {
    testFile("episodeAdvanceTest", file)
  }


  "A String" should "go to bytes and back correctly" in {
    val str1 = "abcdefg"
    val data1 = str1.getBytes(charset)
    val str2 = new String(data1, "utf-8")
    assert(str1 == str2)
  }


  "A Episode goto method" can "get snapshot frame by index" in {
    val file = tmpFile("gotoSnapshot1.essf")


    val frames = getRandomFrames(10, 0.9, 0.8)
    val output = getOutputStream(file)

    var snapshotMarks = List.empty[FrameData]

    frames.foreach {
      case Some(fd) =>
        val r = output.writeFrame(fd.eventsData, fd.stateData)
        if (fd.stateData.isDefined) {
          snapshotMarks ::= fd
        }
        if (fd.frameIndex != r) {
          throw new EssfIOException("frame index should equal.")
        }
      case None => output.writeEmptyFrame()
    }
    output.finish()
    snapshotMarks = snapshotMarks.reverse
    val (input, epInfo) = getInputStream(file)
    val arrayBuffer = new ArrayBuffer[FrameData]()
    snapshotMarks.foreach { f =>
      input.gotoSnapshot(f.frameIndex)
      arrayBuffer.append(input.readFrame().get)
    }

    val rst = arrayBuffer.toList
    input.close()

    /*
        println(s"FRAME COUNT=${epInfo.frameCount}")
        println(s"SNAPSHOT COUNT=${epInfo.snapshotCount}")

        println("ls1: +++++++++++++++++++++++++++++++++++++++++")
        println(s"${snapshotMarks.mkString("\n")}")
        println("ls2: +++++++++++++++++++++++++++++++++++++++++")
        println(s"${rst.mkString("\n")}")
    */

    assert(
      rst.equals(snapshotMarks) &&
      epInfo.frameCount == frames.length &&
      epInfo.snapshotCount == snapshotMarks.size
    )

  }


  it can "work with no snapshot" in {
    val file = tmpFile("gotoSnapshot1.essf")

    val output = getOutputStream(file)

    val frames: IndexedSeq[Option[FrameData]] = IndexedSeq(
      Some(FrameData(0, str2bytes("0aaa"), None)),
      Some(FrameData(1, str2bytes("1aaa"), None)),
      None,
      Some(FrameData(3, str2bytes("3aaa"), None)),
      None,
      None,
      None,
      Some(FrameData(7, str2bytes("6aaa"), None)),
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


    val (input, epInfo) = getInputStream(file)

    val expectList = IndexedSeq(
      FrameData(0, str2bytes("0aaa"), None),
      FrameData(1, str2bytes("1aaa"), None),
      FrameData(2, new Array[Byte](0), None),
      FrameData(3, str2bytes("3aaa"), None),
      FrameData(4, new Array[Byte](0), None),
      FrameData(5, new Array[Byte](0), None),
      FrameData(6, new Array[Byte](0), None)
    )

    input.readFrame() //throw one.

    val rst1 = input.gotoSnapshot(1)
    val target1 = input.readFrame().get

    val rst2 = input.gotoSnapshot(2)
    val target2 = input.readFrame().get

    val rst3 = input.gotoSnapshot(3)
    val target3 = input.readFrame().get

    val rst4 = input.gotoSnapshot(4)
    val rst8 = input.gotoFirstSnapshot()
    val target4 = input.readFrame().get

    val rst5 = input.gotoSnapshot(5)
    val rst9 = input.gotoLastSnapshot()
    val target5 = input.readFrame().get

    val rst6 = input.getFirstSnapshotFrameIndex
    val rst7 = input.getFirstSnapshotFrameIndex



    input.close()

    /*
        println(s"FRAME COUNT=${epInfo.frameCount}")
        println(s"SNAPSHOT COUNT=${epInfo.snapshotCount}")

        println("expectList: +++++++++++++++++++++++++++++++++++++++++")
        println(s"${expectList1.mkString("\n")}")

        println("targetList1: +++++++++++++++++++++++++++++++++++++++++")
        println(s"${targetList1.mkString("\n")}")
    */

    assert(
      epInfo.frameCount == frames.length &&
      rst1 == -1 &&
      rst2 == -1 &&
      rst3 == -1 &&
      rst4 == -1 &&
      rst5 == -1 &&
      rst6 == -1 &&
      rst7 == -1 &&
      rst8 == -1 &&
      rst9 == -1 &&
      target1.equals(expectList(1)) &&
      target2.equals(expectList(2)) &&
      target3.equals(expectList(3)) &&
      target4.equals(expectList(4)) &&
      target5.equals(expectList(5))
    )

  }


  it can "with frame position work correctly" in {
    val file = tmpFile("gotoSnapshot1.essf")

    val output = getOutputStream(file)

    val frames: IndexedSeq[Option[FrameData]] = IndexedSeq(
      Some(FrameData(0, str2bytes("0aaa"), None)),
      Some(FrameData(1, str2bytes("1aaa"), None)),
      None,
      Some(FrameData(3, str2bytes("3aaa"), None)),
      Some(FrameData(4, str2bytes("4aaa"), Some(str2bytes("ss1")))),
      Some(FrameData(5, str2bytes("5aaa"), None)),
      None,
      Some(FrameData(7, str2bytes("6aaa"), None)),
      Some(FrameData(8, str2bytes("8aaa"), None)),
      Some(FrameData(9, str2bytes("9aaa"), None)),
      Some(FrameData(10, str2bytes("10aaa"), Some(str2bytes("ss2")))),
      Some(FrameData(11, str2bytes("11aaa"), None)),
      None,
      None,
      None,
      Some(FrameData(15, str2bytes("15aaa"), None)),
      Some(FrameData(16, str2bytes("16aaa"), None)),
      Some(FrameData(17, str2bytes("17aaa"), None))
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


    val (input, epInfo) = getInputStream(file)

    val expectList1 = List(
      FrameData(4, str2bytes("4aaa"), Some(str2bytes("ss1"))),
      FrameData(5, str2bytes("5aaa"), None),
      FrameData(6, new Array[Byte](0), None),
      FrameData(7, str2bytes("6aaa"), None),
      FrameData(8, str2bytes("8aaa"), None),
      FrameData(9, str2bytes("9aaa"), None)
    )

    input.gotoSnapshot(5)
    val targetList1 = readFrames(6, input)

    input.gotoSnapshot(6)
    val targetList2 = readFrames(6, input)

    input.reset()
    input.gotoSnapshot(7)
    val targetList3 = readFrames(6, input)

    input.gotoSnapshot(8)
    val targetList4 = readFrames(6, input)


    val expectList2 = List(
      FrameData(10, str2bytes("10aaa"), Some(str2bytes("ss2"))),
      FrameData(11, str2bytes("11aaa"), None),
      FrameData(12, new Array[Byte](0), None),
      FrameData(13, new Array[Byte](0), None),
      FrameData(14, new Array[Byte](0), None),
      FrameData(15, str2bytes("15aaa"), None)
    )

    input.gotoSnapshot(10)
    val targetList5 = readFrames(6, input)

    input.gotoSnapshot(11)
    val targetList6 = readFrames(6, input)

    input.gotoSnapshot(12)
    val targetList7 = readFrames(6, input)

    input.gotoSnapshot(13)
    val targetList8 = readFrames(6, input)

    input.gotoSnapshot(14)
    val targetList9 = readFrames(6, input)

    input.gotoSnapshot(15)
    val targetList10 = readFrames(6, input)


    val expectList3 = expectList1

    input.gotoSnapshot(-1)
    val targetList11 = readFrames(6, input)

    input.gotoSnapshot(0)
    val targetList12 = readFrames(6, input)

    input.gotoSnapshot(1)
    val targetList13 = readFrames(6, input)

    input.gotoSnapshot(2)
    val targetList14 = readFrames(6, input)

    input.gotoSnapshot(3)
    val targetList15 = readFrames(6, input)

    input.gotoSnapshot(4)
    val targetList16 = readFrames(6, input)


    val firstSnapshotIndex = input.getFirstSnapshotFrameIndex
    val lastSnapshotIndex = input.getLastSnapshotFrameIndex


    val expectList4 = expectList1
    input.gotoFirstSnapshot()
    val targetList17 = readFrames(6, input)


    val expectList5 = expectList2
    input.gotoLastSnapshot()
    val targetList18 = readFrames(6, input)




    input.close()

    /*
        println(s"FRAME COUNT=${epInfo.frameCount}")
        println(s"SNAPSHOT COUNT=${epInfo.snapshotCount}")

        println("expectList: +++++++++++++++++++++++++++++++++++++++++")
        println(s"${expectList1.mkString("\n")}")

        println("targetList1: +++++++++++++++++++++++++++++++++++++++++")
        println(s"${targetList1.mkString("\n")}")
    */

    assert(
      epInfo.frameCount == frames.length &&
      expectList1.equals(targetList1) &&
      expectList1.equals(targetList2) &&
      expectList1.equals(targetList3) &&
      expectList1.equals(targetList4) &&
      expectList2.equals(targetList5) &&
      expectList2.equals(targetList6) &&
      expectList2.equals(targetList7) &&
      expectList2.equals(targetList8) &&
      expectList2.equals(targetList9) &&
      expectList2.equals(targetList10) &&
      expectList3.equals(targetList11) &&
      expectList3.equals(targetList12) &&
      expectList3.equals(targetList13) &&
      expectList3.equals(targetList14) &&
      expectList3.equals(targetList15) &&
      expectList3.equals(targetList15) &&
      expectList3.equals(targetList16) &&
      expectList4.equals(targetList17) &&
      expectList5.equals(targetList18) &&
      firstSnapshotIndex == 4 &&
      lastSnapshotIndex == 10
    )

  }


}
