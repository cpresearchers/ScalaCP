package cpscala.test

import cpscala.TSolver.CpUtil.Constants

import scala.collection.mutable.ArrayBuffer

object loopTest {

  def main(args: Array[String]): Unit = {
    //    val size = 10000000;
    //    whileTest(100000000)
    //    forTest(100000000)
    //    forRange(100000000)
    println()
    val c = Array.fill[Long](3)(Constants.ALLONELONG)
    val values = ArrayBuffer[Int](256)
    Constants.getValues(c, values)

    println(values.mkString(" "))

    val ac = 0x4L
    println(Constants.FirstLeft(ac))
    println(Constants.FirstRight(ac))
  }

  val ss = Array.range(0, 100)

  println(ss.length)

  //  def printTime(call: => Unit): Unit = {
  //    val startTime = System.currentTimeMillis()
  //    call
  //    println()
  //    println(s"use time : ${System.currentTimeMillis() - startTime}")
  //  }

  def whileTest(size: Int): Unit = {
    val startTime = System.currentTimeMillis()
    var s = 0
    var j = 0
    var b: Long = 0
    while (s < size) {
      b += ss(s % 100)
      s += 1
      j += 1
    }
    val end = System.currentTimeMillis() - startTime
    println(s"while s:$end")
  }

  def forTest(size: Int): Unit = {
    val startTime = System.currentTimeMillis()
    var s = 0
    var b: Long = 0
    for (j <- 0 until size) {
      b += ss(s % 100)
      s += 1
    }
    val end = System.currentTimeMillis() - startTime
    println(s"for s:$end")
  }

  def forRange(size: Int): Unit = {
    val new_size = size / 100
    val startTime = System.currentTimeMillis()
    var s = 0
    var b: Long = 0
    for (j <- 0 until new_size) {
      for (sss <- ss) {
        b += sss
        s += 1
      }
      //      b += ss(s % 100)
    }
    val end = System.currentTimeMillis() - startTime
    println(s"for range s:$end, s = $s")
  }

}