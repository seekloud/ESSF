package org.seekloud.essf.io

import java.io.File

import org.seekloud.essf.Utils
import org.seekloud.essf.test.UnitSpec

import scala.collection.mutable

/**
  * Created by hongruying on 2018/9/2
  */
class MutableInfoTest extends UnitSpec {

  import TestUtils._


  def tmpFile(file: String): String = {
    testFile("mutableInfoMapTest", file)
  }


  def checkMutableInfoIterableEquals(iterable1: Iterable[(String, Array[Byte])], iterable2: Iterable[(String, Array[Byte])]) = {
    if(iterable1.size == iterable2.size){
      !iterable1.exists{
        case (key1, value1) =>
          (!iterable2.exists(_._1 == key1)) || !Utils.arrayEquals(iterable2.find(_._1 == key1).get._2, value1)
      }
    } else false
  }

  "mutable info map" should "be update and get correctly when write process" in {
    val file = tmpFile("updateMutableInfo1.essf")
    val output = getOutputStream(file)
    val updateMutableInfoAction: IndexedSeq[(String,Array[Byte])] = IndexedSeq(
      ("11", str2bytes("0aaa")),
      ("134", str2bytes("1bbb")),
      ("11", str2bytes("1aaa")),
      ("134", str2bytes("2bbb")),
      ("15", str2bytes("3ccc")),
      ("11", str2bytes("3aaa"))
    )

    val mutableInfoMap: mutable.HashMap[String, Array[Byte]] = mutable.HashMap.empty[String, Array[Byte]]
    var getMutableInfoSuccess: Boolean = true

    updateMutableInfoAction.foreach{
      case (key,value) =>
        mutableInfoMap.put(key, value)
        output.putMutableInfo(key, value)
        if (!Utils.arrayEquals(mutableInfoMap(key), output.getMutableInfo(key).get)) getMutableInfoSuccess = false
    }

    val targetIterable = output.mutableInfoIterable
    output.finish()

    assert(
      getMutableInfoSuccess &&
      checkMutableInfoIterableEquals(targetIterable, mutableInfoMap.toIterable)
    )
  }

  "mutable info map" should "be update and get correctly when read process" in {
    val file = tmpFile("updateMutableInfo2.essf")
    val output = getOutputStream(file)
    val updateMutableInfoAction: IndexedSeq[(String,Array[Byte])] = IndexedSeq(
      ("11", str2bytes("0aaa")),
      ("134", str2bytes("1bbb")),
      ("11", str2bytes("1aaa")),
      ("134", str2bytes("2bbb")),
      ("15", str2bytes("3ccc")),
      ("11", str2bytes("3aaa"))
    )

    val mutableInfoMap: mutable.HashMap[String, Array[Byte]] = mutable.HashMap.empty[String, Array[Byte]]
    var getMutableInfoSuccess: Boolean = true

    updateMutableInfoAction.foreach{
      case (key,value) =>
        mutableInfoMap.put(key, value)
        output.putMutableInfo(key, value)
    }

    output.finish()

    val (input, epInfo) = getInputStream(file)
    mutableInfoMap.foreach{
      case (key, value) =>
        if(input.getMutableInfo(key).isEmpty || !Utils.arrayEquals(input.getMutableInfo(key).get, value)) getMutableInfoSuccess = false
    }

    val targetIterable = input.mutableInfoIterable
    input.close()


    assert(
      getMutableInfoSuccess &&
        checkMutableInfoIterableEquals(targetIterable, mutableInfoMap.toIterable)
    )
  }

  "mutable info map" should "work success in write process and read process." in {
    val file = tmpFile("updateMutableInfo3.essf")
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

    val updateMutableInfoAction: IndexedSeq[(Long,(String,Array[Byte]))] = IndexedSeq(
      (1,("11", str2bytes("0aaa"))),
      (1,("134", str2bytes("1bbb"))),
      (3,("11", str2bytes("1aaa"))),
      (4,("134", str2bytes("2bbb"))),
      (5,("15", str2bytes("3ccc"))),
      (5,("11", str2bytes("3aaa")))
    )

    val mutableInfoMap: mutable.HashMap[String, Array[Byte]] = mutable.HashMap.empty[String, Array[Byte]]

    frames.zipWithIndex.foreach {
      case (Some(fd),f) =>
        val r = output.writeFrame(fd.eventsData, fd.stateData)
        if (fd.frameIndex != r) {
          throw new EssfIOException("frame index should equal.")
        }
        updateMutableInfoAction.filter(_._1 == f).foreach{
          case (_, (key, value)) =>
            output.putMutableInfo(key,value)
            mutableInfoMap.put(key,value)
        }
      case (None,f) => output.writeEmptyFrame()
        updateMutableInfoAction.filter(_._1 == f).foreach{
          case (_, (key, value)) =>
            output.putMutableInfo(key,value)
            mutableInfoMap.put(key,value)
        }
    }
    output.finish()

    val (input, epInfo) = getInputStream(file)
    val mutableInfoIterable = input.mutableInfoIterable

    println(s"mutable Info length:${mutableInfoIterable.size}")


    val keyValueIsRight:Boolean = !mutableInfoMap.map{
      case (key, value) =>
        val valueOpt = input.getMutableInfo(key)
        valueOpt.nonEmpty && Utils.arrayEquals(value,valueOpt.get)
    }.exists(_ == false)
    val targets = readFrames(8, input)
    input.close()

    val expectList = List(
      FrameData(0, str2bytes("0aaa"), None),
      FrameData(1, str2bytes("1aaa"), None),
      FrameData(2, new Array[Byte](0), None),
      FrameData(3, str2bytes("3aaa"), None),
      FrameData(4, str2bytes("4aaa"), Some(str2bytes("ss11"))),
      FrameData(5, str2bytes("5aaa"), None),
      FrameData(6, new Array[Byte](0), None),
      FrameData(7, str2bytes("7aaa"), None)
    )

    val tmpMutableFile = new File(file+".tmp")

    assert(
        epInfo.frameCount == 8 &&
        targets.equals(expectList) &&
          keyValueIsRight &&
          checkMutableInfoIterableEquals(mutableInfoMap.toIterable,mutableInfoIterable) &&
          !tmpMutableFile.exists()
    )
  }


  "mutable info map" can "continue with a finished file." in {
    val file = tmpFile("updateMutableInfo4.essf")
    val output = getOutputStream(file)

    val mutableInfoMap: mutable.HashMap[String, Array[Byte]] = mutable.HashMap.empty[String, Array[Byte]]

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

    val updateMutableInfoAction: IndexedSeq[(Long,(String,Array[Byte]))] = IndexedSeq(
      (1,("11", str2bytes("0aaa"))),
      (1,("134", str2bytes("1bbb"))),
      (3,("11", str2bytes("1aaa"))),
      (4,("134", str2bytes("2bbb"))),
      (5,("15", str2bytes("3ccc"))),
      (5,("16", str2bytes("3ddd"))),
      (9,("19", str2bytes("4eee"))),
      (9,("134", str2bytes("5bbb"))),
      (11,("11", str2bytes("6aaa"))),
      (13,("144", str2bytes("7fff")))
    )

    frames.zipWithIndex.foreach {
      case (Some(fd), f) =>
        val r = output.writeFrame(fd.eventsData, fd.stateData)
        if (fd.frameIndex != r) {
          throw new EssfIOException("frame index should equal.")
        }
        updateMutableInfoAction.filter(_._1 == f).foreach{
          case (_, (key, value)) =>
            output.putMutableInfo(key,value)
            mutableInfoMap.put(key,value)
        }
      case (None, f) =>
        output.writeEmptyFrame()
        updateMutableInfoAction.filter(_._1 == f).foreach{
          case (_, (key, value)) =>
            output.putMutableInfo(key,value)
            mutableInfoMap.put(key,value)
        }
    }
    println(s"continue test: mutableInfoMap=${mutableInfoMap.size} and fileMutableInfoSize=${output.mutableInfoIterable.size} output finish")
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

    frames1.zipWithIndex.foreach {
      case (Some(fd), f) =>
        val r = continueOutput.writeFrame(fd.eventsData, fd.stateData)
        if (fd.frameIndex != r) {
          throw new EssfIOException("frame index should equal.")
        }
        updateMutableInfoAction.filter(_._1 == f + 9).foreach{
          case (_, (key, value)) =>
            continueOutput.putMutableInfo(key,value)
            mutableInfoMap.put(key,value)
        }
      case (None, f) =>
        continueOutput.writeEmptyFrame()
        updateMutableInfoAction.filter(_._1 == f + 9).foreach{
          case (_, (key, value)) =>
            continueOutput.putMutableInfo(key,value)
            mutableInfoMap.put(key,value)
        }
    }
    println(s"continue test: mutableInfoMap=${mutableInfoMap.size} and fileMutableInfoSize=${continueOutput.mutableInfoIterable.size} continueOutput finish")
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
    val mutableInfoIterable = input.mutableInfoIterable



    val getMutableInfoSuccess: Boolean = !mutableInfoMap.map{
      case (key, value) =>
        val valueOpt = input.getMutableInfo(key)
        valueOpt.nonEmpty && Utils.arrayEquals(value,valueOpt.get)
    }.exists(_ == false)
    val targets = readFrames(15, input)

    val tmpMutableFile = new File(file+".tmp")

    assert(
      continueIndex == 8 &&
        epInfo.frameCount == 15 &&
        epInfo.snapshotCount == 3 &&
        targets.equals(expectList) &&
        getMutableInfoSuccess &&
        checkMutableInfoIterableEquals(mutableInfoMap.toIterable,mutableInfoIterable) &&
        !tmpMutableFile.exists()
    )
  }


  it should "work with incomplete file" in {
    val file = tmpFile("updateMutableInfo5.essf")
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

    val updateMutableInfoAction: IndexedSeq[(Long,(String,Array[Byte]))] = IndexedSeq(
      (1,("11", str2bytes("0aaa"))),
      (1,("134", str2bytes("1bbb"))),
      (3,("11", str2bytes("1aaa"))),
      (4,("134", str2bytes("2bbb"))),
      (5,("15", str2bytes("3ccc"))),
      (5,("11", str2bytes("3aaa")))
    )

    val mutableInfoMap: mutable.HashMap[String, Array[Byte]] = mutable.HashMap.empty[String, Array[Byte]]

    frames.zipWithIndex.foreach {
      case (Some(fd),f) =>
        val r = output.writeFrame(fd.eventsData, fd.stateData)
        if (fd.frameIndex != r) {
          throw new EssfIOException("frame index should equal.")
        }
        updateMutableInfoAction.filter(_._1 == f).foreach{
          case (_, (key, value)) =>
            output.putMutableInfo(key,value)
            mutableInfoMap.put(key,value)
        }
      case (None,f) => output.writeEmptyFrame()
        updateMutableInfoAction.filter(_._1 == f).foreach{
          case (_, (key, value)) =>
            output.putMutableInfo(key,value)
            mutableInfoMap.put(key,value)
        }
    }

    val tmpMutableFileExists = new File(file+".tmp").exists()


    val fixOutput = new FrameOutputStream(file)
    fixOutput.fix()

    val (input, epInfo) = getInputStream(file)
    val mutableInfoIterable = input.mutableInfoIterable
    println(s"mutable Info length:${mutableInfoMap.size}")


    val keyValueIsRight:Boolean = !mutableInfoMap.map{
      case (key, value) =>
        val valueOpt = input.getMutableInfo(key)
        valueOpt.nonEmpty && Utils.arrayEquals(value,valueOpt.get)
    }.exists(_ == false)
    val targets = readFrames(8, input)
    input.close()

    val expectList = List(
      FrameData(0, str2bytes("0aaa"), None),
      FrameData(1, str2bytes("1aaa"), None),
      FrameData(2, new Array[Byte](0), None),
      FrameData(3, str2bytes("3aaa"), None),
      FrameData(4, str2bytes("4aaa"), Some(str2bytes("ss11"))),
      FrameData(5, str2bytes("5aaa"), None),
      FrameData(6, new Array[Byte](0), None),
      FrameData(7, str2bytes("7aaa"), None)
    )

    val tmpMutableFile = new File(file+".tmp")

    assert(
      tmpMutableFileExists &&
        epInfo.frameCount == 8 &&
        targets.equals(expectList) &&
        keyValueIsRight &&
        checkMutableInfoIterableEquals(mutableInfoMap.toIterable,mutableInfoIterable)
    )
  }



}
