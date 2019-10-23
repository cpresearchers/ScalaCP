package cpscala.test

import cpscala.TSolver.CpUtil.Constants

import scala.collection.immutable.{IntMap, SortedMap}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object loopTest {

  def main(args: Array[String]): Unit = {
    //    val size = 10000000;
    //    whileTest(100000000)
    //    forTest(100000000)
    //    forRange(100000000)
    //    println()
    //    val c = Array.fill[Long](3)(Constants.ALLONELONG)
    //    val values = ArrayBuffer[Int](256)
    //    Constants.getValues(c, values)
    //
    //    println(values.mkString(" "))
    //
    //    val ac = 0x4L
    //    println(Constants.FirstLeft(ac))
    //    println(Constants.FirstRight(ac))
    println("xixi")
    val m = mutable.HashMap[Int, Int](1 -> 2, 2 -> 3)
    println(m(1))
    val c = SortedMap[Int, Int]()
    val xs = Array[Int](6, 35, 14, 43, 62, 21, 0)
    val xst = mutable.TreeMap[Int, Int](6 -> 0, 35 -> 1, 14 -> 2, 43 -> 3, 62 -> 4, 21 -> 5, 0 -> 6)
    println("xst:", xst)
    println(xst.keys.toArray.mkString(" "))
    println(xst.values.toArray.mkString(" "))
    val d = xs.zipWithIndex.toMap
    val f = xs.sorted.toArray.zipWithIndex.toMap
    println("xs", xs.sorted.mkString(" "))
    //    f.to(IntMap[Int])
    val g = IntMap[Int]() ++ f
    //    val g = f.to(IntMap[Int]())
    println("f", f)
    println("g", g)
    println(d)
    val e = SortedMap[Int, Any]() ++ d
    println(e)
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