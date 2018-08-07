package org.seekloud.essf.io

import org.seekloud.essf.box.BoxType
import org.seekloud.essf.test.UnitSpec

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 5:10 PM
  */
class ReaderTest extends UnitSpec {


  "A reader" should "read data success" in {

    val file = "test_data/test1.essf"
    val reader = new ESSFReader(file)
    val box = reader.get()
    println(s"box: $box")
    assert(box.boxType == BoxType.epif)

  }

}
