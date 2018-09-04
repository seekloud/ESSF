package essf.example

import org.seekloud.essf.io.{FrameData, FrameInputStream}

/**
  * User: Taoz
  * Date: 8/10/2018
  * Time: 1:32 PM
  */
object RecallAGame {

  val utf8 = "utf-8"

  def bytes2str(bytes: Array[Byte]): String = {
    new String(bytes, utf8)
  }

  def main(args: Array[String]): Unit = {
    val targetFile = "test_data/example/record1.essf"
    val input = initInput(targetFile)
    val ls = readData(input)
    input.close()
    println("data list:")
    println(ls.mkString("\n"))
  }


  def initInput(targetFile: String): FrameInputStream = {
    val input = new FrameInputStream(targetFile)
    val info = input.init()

    val name = info.simulatorId
    val version = info.simulatorVersion
    val gameInfo = bytes2str(info.simulatorMetadata)
    val initState = bytes2str(info.simulatorInitState)

    println(s"name: $name")
    println(s"version: $version")
    println(s"gameInfo: $gameInfo")
    println(s"initState: $initState")
    println(s"mutableInfo: ${input.mutableInfoIterable.map(t => (t._1, bytes2str(t._2)))}")

    input
  }


  def readData(input: FrameInputStream): List[Option[(String, Option[String])]] = {
    var ls = List.empty[Option[(String, Option[String])]]

    while (input.hasMoreFrame) {
      input.readFrame() match {
        case Some(FrameData(idx, ev, stOp)) =>
          val data = if (ev.length > 0) {
            val s1 = bytes2str(ev)
            val s2 = stOp.map(bytes2str)
            Some((s1, s2))
          } else {
            if (stOp.isEmpty) {
              None
            } else {
              throw new RuntimeException("this game can not go to here.")
            }
          }
          ls ::= data
        case None =>
          println("get to the end, no more frame.")
      }
    }
    ls
  }



}
