package org.seekloud.essf.test

import org.seekloud.essf.box.EBIF_Box
import org.seekloud.essf.io.ESSFWriter

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 4:19 PM
  */
class WriterTest extends UnitSpec {


//  def withFile(testCode: (File, FileWriter) => Any) {
//    val file = File.createTempFile("hello", "world") // create the fixture
//    val writer = new FileWriter(file)
//    try {
//      writer.write("ScalaTest is ") // set up the fixture
//      testCode(file, writer) // "loan" the fixture to the test
//    }
//    finally writer.close() // clean up the fixture
//  }


  "A Writer" should "write box in file without exception" in {
    val file = "test1.essf"
    val writer = new ESSFWriter(file)
    val box = new EBIF_Box
    box.version = 1
    box.createTime = System.currentTimeMillis()
    box.snapshotCount = 1000
    box.frameCount= 200000
    box.frameMilliSeconds = 150

    writer.put(box)
    assert(true)

  }





}
