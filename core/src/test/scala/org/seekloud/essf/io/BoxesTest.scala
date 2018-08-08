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


  val dir = "test_data/boxesTest/"

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
    val tmpFile = dir + targetBox.boxType + ".essf"
    writeAndRead(targetBox, tmpFile)
  }


  "EpisodeInform Box" should "write and read keeping the same" in {
    val targetBox = Boxes.EpisodeInform(12345678, 12345)
    val tmpFile = dir + targetBox.boxType + ".essf"
    writeAndRead(targetBox, tmpFile)
  }


  "BoxPosition Box" should "write and read keeping the same" in {
    val targetBox = Boxes.BoxPosition(12345678901234l)
    val tmpFile = dir + targetBox.boxType + ".essf"
    writeAndRead(targetBox, tmpFile)
  }


  "EpisodeStatus Box" should "write and read keeping the same" in {
    val targetBox = Boxes.EpisodeStatus(1234)
    val tmpFile = dir + targetBox.boxType + ".essf"
    writeAndRead(targetBox, tmpFile)
  }

  "SnapshotPosition Box" should "write and read keeping the same" in {
    val targetBox = Boxes.SnapshotPosition(List(
      (1, 123L),
      (2, 123L),
      (3, 123L),
      (4, 123L)
    ))
    val tmpFile = dir + targetBox.boxType + ".essf"
    writeAndRead(targetBox, tmpFile)
  }

  it should "write and read keeping the same with empty data." in {
    val targetBox = Boxes.SnapshotPosition(Nil)
    val tmpFile = dir + targetBox.boxType + "1.essf"
    writeAndRead(targetBox, tmpFile)
  }


  "EndOfFile Box" should "write and read keeping the same" in {
    val targetBox = Boxes.EndOfFile()
    val tmpFile = dir + targetBox.boxType + ".essf"
    writeAndRead(targetBox, tmpFile)
  }


  "SimulatorInform Box" should "write and read keeping the same" in {
    val targetBox = Boxes.SimulatorInform("tank", "1.0.1")
    val tmpFile = dir + targetBox.boxType + ".essf"
    writeAndRead(targetBox, tmpFile)
  }


  "SimulatorMetadata Box" should "write and read keeping the same" in {
    val targetBox = Boxes.SimulatorMetadata("tmpdata".getBytes("utf-8"))
    val tmpFile = dir + targetBox.boxType + ".essf"
    writeAndRead(targetBox, tmpFile)
  }


  "SimulatorFrame Box" should "write and read keeping the same" in {
    val data1 = "tmpdataSimulatorInform".getBytes("utf-8")
    val data2 = "tmpdataSimulatorInform".getBytes("utf-8")
    val targetBox = Boxes.SimulatorFrame(4567, data1, None)
    val tmpFile = dir + targetBox.boxType + ".essf"
    writeAndRead(targetBox, tmpFile)
  }

  it should "write and read keeping the same with snapshot" in {
    val data1 = "tmpdataSimulatorInform".getBytes("utf-8")
    val data2 = "tmpdataSimulatorInform".getBytes("utf-8")
    val targetBox = Boxes.SimulatorFrame(14567, data1, Some(data2))
    val tmpFile = dir + targetBox.boxType + ".1essf"
    writeAndRead(targetBox, tmpFile)
  }



  "EndOfFrame Box" should "write and read keeping the same" in {
    val targetBox = Boxes.EndOfFrame()
    val tmpFile = dir + targetBox.boxType + ".essf"
    writeAndRead(targetBox, tmpFile)
  }
}
