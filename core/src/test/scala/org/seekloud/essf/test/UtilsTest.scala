package org.seekloud.essf.test

import java.io.{FileOutputStream, FileWriter, OutputStreamWriter}

import org.seekloud.essf.io.TestUtils

/**
  * User: Taoz
  * Date: 8/9/2018
  * Time: 8:09 PM
  */
class UtilsTest extends UnitSpec {


  "a copyPartFile" should "can copy part file" in {

    val f1 = "test_data/utilstest/copypartfile1.txt"
    val writer = new OutputStreamWriter(new FileOutputStream(f1), "utf-8")
    writer.write("1234567890abcdefg")
    writer.close()
    val f2 = "test_data/utilstest/copypartfile2.txt"
    TestUtils.copyPartFile(f1, f2, 5)
    val rst = io.Source.fromFile(f2, "utf-8").getLines().toList.head
    assert(rst == "12345")

  }

}
