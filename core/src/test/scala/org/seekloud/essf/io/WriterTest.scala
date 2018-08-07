package org.seekloud.essf.io

import org.seekloud.essf.box.EPIF_Box
import org.seekloud.essf.test.UnitSpec

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
    val file = "test_data/test1.essf"
    val writer = new ESSFWriter(file)
    val box = EPIF_Box( 200000, 150, 1000 )
    writer.put(box)
    writer.close()
    assert(true)

  }


}
