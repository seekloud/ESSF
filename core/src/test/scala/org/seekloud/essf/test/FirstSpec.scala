package org.seekloud.essf.test
import org.scalatest.FlatSpec

import scala.collection.mutable

/**
  * User: Taoz
  * Date: 8/6/2018
  * Time: 11:30 AM
  */
class FirstSpec extends FlatSpec {


  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new mutable.Stack[Int]
    stack.push(1)
    stack.push(2)
    assert(stack.pop() === 2)
    assert(stack.pop() === 1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new mutable.Stack[String]
    assertThrows[NoSuchElementException] {
      emptyStack.pop()
    }
  }

  it should "has zero length in beginning" in {
    val emptyStack = new mutable.Stack[String]
    assert(emptyStack.length + 1 == 0)
  }

}
