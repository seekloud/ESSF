package org.seekloud.essf.io

import org.scalatest.Assertion
import org.seekloud.essf.box.{Box, BoxType, Boxes}
import org.seekloud.essf.test.UnitSpec

/**
  * User: Taoz
  * Date: 8/8/2018
  * Time: 3:32 PM
  */
class BoxesTest extends UnitSpec {


  import TestUtils._

  def tmpFile(file: String) = {
    "test_data/boxesTest/" + file
  }

  def writeBox(box: Box, file: String): Unit = {
    val writer = new ESSFWriter(file)
    writer.put(box)
    writer.close()
  }

  def readBox(file: String): Box = {
    val reader = new ESSFReader(file)
    reader.get()
  }

  def writeAndRead(targetBox: Box, file: String): Assertion = {
    writeBox(targetBox, file)
    val box = readBox(file)
    assert(box == targetBox)
  }

  "FileMeta Box" should "write and read keeping the same" in {
    val targetBox = Boxes.FileMeta(123.toByte, 123456789l)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "EpisodeInform Box" should "write and read keeping the same" in {
    val targetBox = Boxes.EpisodeInform(12345678, 12345)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "BoxIndexes Box" should "write and read keeping the same" in {
    val map = Map(
      BoxType.boxIndexPosition -> 111L,
      BoxType.episodeInform -> 222L,
      BoxType.episodeStatus -> 3333L,
      BoxType.endOfFrame -> 444L,
      BoxType.snapshotPosition -> 144441L,
      BoxType.beginOfFrame -> 123232321L,
      BoxType.mutableInfoMap -> 1001L
    )
    val targetBox = Boxes.BoxIndexes(map)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "EpisodeStatus Box" should "write and read keeping the same" in {
    val targetBox = Boxes.EpisodeStatus(true)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }

  "SnapshotPosition Box" should "write and read keeping the same" in {
    val targetBox = Boxes.SnapshotPosition(List(
      (1, 123L),
      (2, 123L),
      (3, 123L),
      (4, 123L)
    ))
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }

  it should "write and read keeping the same with empty data." in {
    val targetBox = Boxes.SnapshotPosition(Nil)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + "1.essf"))
  }


  "EndOfFile Box" should "write and read keeping the same" in {
    val targetBox = Boxes.EndOfFile()
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "SimulatorInform Box" should "write and read keeping the same" in {
    val targetBox = Boxes.SimulatorInform("tank", "1.0.1")
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "SimulatorMetadata Box" should "write and read keeping the same" in {
    val data = rdm.nextString(1027).getBytes()
    val targetBox = Boxes.SimulatorMetadata(data)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  it should "write and read keeping the same with no data" in {
    val data = new Array[Byte](0)
    val targetBox = Boxes.SimulatorMetadata(data)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + "1.essf"))
  }


  it should "write and read keeping the same with large data" in {
    val data = rdm.nextString(1027000).getBytes()
    val targetBox = Boxes.SimulatorMetadata(data)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + "2.essf"))
  }


  "SimulatorFrame Box" should "write and read keeping the same" in {
    val data1 = rdm.nextString(1027).getBytes()
    val targetBox = Boxes.SimulatorFrame(4567, data1, None)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }

  it should "write and read keeping the same with snapshot" in {
    val data1 = rdm.nextString(107).getBytes()
    val data2 = rdm.nextString(219).getBytes()
    val targetBox = Boxes.SimulatorFrame(14567, data1, Some(data2))
    writeAndRead(targetBox, tmpFile(targetBox.boxType + "1.essf"))
  }

  it should "write and read keeping the same with large data" in {
    val data1 = rdm.nextString(1027000).getBytes()
    val data2 = rdm.nextString(2019000).getBytes()
    val targetBox = Boxes.SimulatorFrame(14567, data1, Some(data2))
    writeAndRead(targetBox, tmpFile(targetBox.boxType + "2.essf"))
  }

  it should "write and read keeping the same with no data" in {
    val data1 = new Array[Byte](0)
    val data2 = new Array[Byte](0)
    val targetBox = Boxes.SimulatorFrame(14567, data1, Some(data2))
    writeAndRead(targetBox, tmpFile(targetBox.boxType + "3.essf"))
  }


  "EndOfFrame Box" should "write and read keeping the same" in {
    val targetBox = Boxes.EndOfFrame()
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "BeginOfFrame Box" should "write and read keeping the same" in {
    val targetBox = Boxes.BeginOfFrame()
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "EmptyFrame Box" should "write and read keeping the same" in {
    val targetBox = Boxes.EmptyFrame()
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "InitState Box" should "write and read keeping the same" in {
    val targetBox = Boxes.InitState(
      TestUtils.getReadableString(519).getBytes("utf-8")
    )
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }

  "Mutable Info Box" should "write and read keeping the same" in {
    val map = Map(
      getReadableString(10) -> getReadableString(20).getBytes("utf-8"),
      getReadableString(13) -> getReadableString(20).getBytes("utf-8"),
      getReadableString(12) -> getReadableString(20).getBytes("utf-8")
    )
    val targetBox = Boxes.MutableInfoMap(map)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "Tmp Buffer Boxes Num Box" should "write and read keeping the same" in {
    val targetBox = Boxes.TmpBufferBoxNum(100)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }

  "Box Index Position Box" should "write and read keeping the same" in {
    val targetBox = Boxes.BoxIndexPosition(10000L)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "Update Mutable Info Box" should "write and read keeping the same" in {
    val targetBox = Boxes.UpdateMutableInfo("test", str2bytes("test123"))
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


}