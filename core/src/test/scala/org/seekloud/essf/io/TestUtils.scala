package org.seekloud.essf.io

import scala.collection.immutable
import scala.util.Random

/**
  * User: Taoz
  * Date: 8/9/2018
  * Time: 10:59 AM
  */
object TestUtils {


  val charset = "utf-8"

  val simulatorId = "bigSnake"

  val dataVersion = "1.0.3"

  val rdm = new Random(System.currentTimeMillis())

  val readableChars = Array(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
  )

  def testFile(dir: String, fileName: String): String = {
    "test_data/" + dir + "/" + fileName
  }

  def str2bytes(str: String): Array[Byte] = {
    str.getBytes(charset)
  }

  def bytes2str(bytes: Array[Byte]): String = {
    if(bytes.length == 0) {
      "[EMPTY]"
    } else {
      new String(bytes, charset)
    }
  }

  def readFrames(n: Int, input: FrameInputStream): List[FrameData] = {
    (0 until n).map { _ =>
      input.readFrame().get
    }.toList
  }

  def getOutputStream(
    file: String
  ): FrameOutputStream = {
    val metadata = "abcdeflalaaalal你好世界.!@#$".getBytes(charset)
    val initState = "cc123456abcdef2lalal你好世界.!@#$".getBytes(charset)
    val output = new FrameOutputStream(file)
    output.init(simulatorId, dataVersion, metadata, initState)
    output
  }


  def getInputStream(file: String): (FrameInputStream, EpisodeInfo) = {
    val input = new FrameInputStream(file)
    val epInfo = input.init()
    (input, epInfo)
  }


  def getReadableString(length: Int): String = {
    val range = readableChars.length
    val rst = (0 until length).map { _ =>
      readableChars(rdm.nextInt(range))
    }.mkString("")
    rst
  }

  def getRandomFrames(
    len: Int,
    eventRatio: Double,
    snapshotRatio: Double
  ): immutable.IndexedSeq[Option[FrameData]] = {


    def getFrame(idx: Int): Option[FrameData] = {
      val data1 = getReadableString(6 ).getBytes(charset)
      val data2 = getReadableString(7 ).getBytes(charset)
      rdm.nextDouble() match {
        case x if x < eventRatio =>
          rdm.nextDouble() match {
            case y if y < snapshotRatio =>
              Some(FrameData(idx, data1, Some(data2)))
            case _ => Some(FrameData(idx, data1, None))
          }
        case _ => rdm.nextDouble() match {
          case y if y < snapshotRatio =>
            Some(FrameData(idx, new Array[Byte](0), Some(data2)))
          case _ => None
        }
      }
    }

    (0 until len).map(i => getFrame(i))
  }


}
