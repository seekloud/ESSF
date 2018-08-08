package org.seekloud.essf.io

import org.seekloud.essf.Utils
import org.seekloud.essf.test.UnitSpec

/**
  * User: Taoz
  * Date: 8/8/2018
  * Time: 4:32 PM
  */
class EpisodeTest extends UnitSpec {

  val charset = "utf-8"


  def tmpFile(file: String): String = {
    "test_data/episodeTest/" + file
  }

  val simulatorId = "bigSnake"
  val dataVersion = "1.0.3"


  "Episode Inform" should "init correctly" in {
    val file = tmpFile("test1.essf")
    val metadata = "abcdeflalalal你好世界.!@#$".getBytes(charset)
    val initState = "123456abcdef2lalal你好世界.!@#$".getBytes(charset)
    val output = new FrameOutputStream(file)
    output.init(simulatorId, dataVersion, metadata, initState)
    output.finish()

    val input = new FrameInputStream(file)
    val inform = input.init()
    input.close()

    assert(
      simulatorId == inform.id
      && dataVersion == inform.version
      && Utils.arrayEquals(metadata, inform.metadata)
      && Utils.arrayEquals(initState, inform.initState)
    )

  }


}
