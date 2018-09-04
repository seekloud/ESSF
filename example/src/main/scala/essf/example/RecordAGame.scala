package essf.example

import org.seekloud.essf.io.FrameOutputStream

/**
  * User: Taoz
  * Date: 8/10/2018
  * Time: 1:09 PM
  */
object RecordAGame {

  val utf8 = "utf-8"


  def main(args: Array[String]): Unit = {

    val targetFile = "test_data/example/record1.essf"
    val recorder = getRecorder(targetFile)
    val frames = getTestFrames

    recorder.putMutableInfo("playerJoin","1-1-2".getBytes(utf8))

    frames.foreach {
      case Some((events, stateOption)) =>
        recorder.writeFrame(events.getBytes(utf8), stateOption.map(_.getBytes(utf8)))
      case None =>
        recorder.writeEmptyFrame()
        recorder.putMutableInfo("playerJoin","1-1-2-4".getBytes(utf8))
    }

    recorder.putMutableInfo("playerJoin","1-1-2-4-5".getBytes(utf8))

    recorder.finish()
    println(s"Write finish: $targetFile")
  }



  def getRecorder(targetFile: String): FrameOutputStream = {
    val name = "birdfly"
    val version = "0.1"
    val gameInformation =
      """
        |{
        |"date":20180101,
        |"ip":"127.0.0.1",
        |"node":123,
        |"player":["314", "531","756"],
        |}
      """.stripMargin.getBytes(utf8)

    val initState = "helloworld".getBytes(utf8)
    val recorder = new FrameOutputStream(targetFile)
    recorder.init(name, version, gameInformation, initState)
    recorder
  }

  def getTestFrames: List[Option[(String, Option[String])]] = {

    List(
      Some("event91,event13", None),
      None,
      Some("event21,event21,event21,22222", None),
      Some("event51,event1,event1,", None),
      Some("event41,event1,event1,", Some("state1")),
      Some("event91,event1,event1,", None),
      None,
      None,
      Some("event1,event1,event1,", None),
      Some("event1,event1,event1,", Some("state2")),
      Some("event1,event1,event1,", None)
    )

  }


}
