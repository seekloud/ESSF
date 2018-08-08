package org.seekloud.essf.io

import org.scalatest.Assertion
import org.seekloud.essf.box.{Box, Boxes}
import org.seekloud.essf.test.UnitSpec

/**
  * User: Taoz
  * Date: 8/8/2018
  * Time: 3:32 PM
  */
class BoxesTest extends UnitSpec {



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


  "BoxPosition Box" should "write and read keeping the same" in {
    val targetBox = Boxes.BoxPosition(12345678901234l)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "EpisodeStatus Box" should "write and read keeping the same" in {
    val targetBox = Boxes.EpisodeStatus(1234)
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
    val targetBox = Boxes.SimulatorMetadata("tmpdata".getBytes("utf-8"))
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }


  "SimulatorFrame Box" should "write and read keeping the same" in {
    val data1 = "tmpdataSimulatorInform".getBytes("utf-8")
    val data2 = "tmpdataSimulatorInform".getBytes("utf-8")
    val targetBox = Boxes.SimulatorFrame(4567, data1, None)
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }

  it should "write and read keeping the same with snapshot" in {
    val data1 = "tmpdataSimulatorInform".getBytes("utf-8")
    val data2 = "tmpdataSimulatorInform".getBytes("utf-8")
    val targetBox = Boxes.SimulatorFrame(14567, data1, Some(data2))
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".1essf"))
  }


  "EndOfFrame Box" should "write and read keeping the same" in {
    val targetBox = Boxes.EndOfFrame()
    writeAndRead(targetBox, tmpFile(targetBox.boxType + ".essf"))
  }
}
