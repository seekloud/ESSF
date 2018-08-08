package org.seekloud.essf

/**
  * User: Taoz
  * Date: 8/8/2018
  * Time: 4:09 PM
  */
object Utils {

  def arrayEquals[A](arr1: Array[A], arr2: Array[A]): Boolean = {
    if (arr1.length == arr2.length) {
      var rst = true
      var i = arr1.length
      while (i > 0) {
        i -= 1
        if (!arr1(i).equals(arr2(i))) {
          rst = false
          i = -1
        }
      }
      rst
    } else false
  }

  val EmptyByteArray = new Array[Byte](0)

}
