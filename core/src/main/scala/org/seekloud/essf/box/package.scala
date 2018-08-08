package org.seekloud.essf

import java.nio.ByteBuffer

import scala.collection.immutable
import scala.util.Try

/**
  * User: Taoz
  * Date: 8/7/2018
  * Time: 10:38 AM
  */
package object box {

  object BoxType {
    val fileMeta = "flmt"
    val boxPosition = "bxps"
    val episodeInform = "epif"
    val snapshotPosition = "snps"
    val episodeStatus = "epst"
    val endOfFile = "eofl"

    val simulatorInform = "smli"
    val simulatorMetadata = "smlm"
    val simulatorFrame = "slfr"
    val endOfFrame = "eofr"
  }


}
