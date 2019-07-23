package cpscala.TSolver.Model.Solver.Others

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

class BitSetVar_LMX(val name: String, val id: Int, numVars: Int, vals: Array[Int], val helper: SearchHelper, val parallelism: Int) extends Var {
  //// 各种层
  // 总层数
  val numLevel = numVars + parallelism + 3
  val numTmpLevels = parallelism
  // 临时层数启始层
  val startTmpLevel = numVars + 3
  // 主线程搜索层
  val mainLevel = numVars + 3
  // 当前顶层，包括临时层
  val topLevel = numVars + 3
  // 初始化所有临时层
  //  val tmpLevels = Array.fill[MultiLevel](numTmpLevels)(new MultiLevel(INDEX.kOVERFLOW, INDEX.kOVERFLOW))
  override val capacity = vals.length
  val numBit = Math.ceil(capacity.toDouble / Constants.BITSIZE.toDouble).toInt
  val bitMark = Array.fill[Long](numBit)(0L)
  val bitDoms = Array.fill[Long](numLevel, numBit)(0L)
  val tmpLevels = new LMXSparseSet(parallelism, startTmpLevel)

  // 初始化第0级的bitDom
  var ii = 0
  while (ii < numBit) {
    bitDoms(0)(ii) = Constants.ALLONELONG
    ii += 1
  }
  bitDoms(0)(numBit - 1) <<= (Constants.BITSIZE - capacity % Constants.BITSIZE)

  override def getNumBit(): Int = numBit

  override def newLevel(): Int = {
    val pre_level = level
    level += 1

    var i = 0
    while (i < numBit) {
      bitDoms(level)(i) = bitDoms(pre_level)(i)
      i += 1
    }

    return level
  }

  def newTmpLevel(): Int = {

  }

  override def backLevel(): Int = {
    // 若变量在当前层赋值，则撤销赋值
    if (bindLevel == level) {
      bindLevel = Constants.kINTINF
    }
    level -= 1
    return level
  }

  //提交改动
  override def restrict(): Unit = {
    var i = 0
    while (i < numBit) {
      bitDoms(level)(i) &= bitMark(i)
      i += 1
    }
  }

  override def size(): Int = {
    var curr_size = 0
    for (a <- bitDoms(level)) {
      curr_size += java.lang.Long.bitCount(a)
    }
    //    bitDoms(level).foreach(a => curr_size += java.lang.Long.bitCount(a))
    return curr_size
  }

  override def bind(a: Int): Unit = {
    val (x, y) = INDEX.getXY(a)
    var i = 0
    while (i < numBit) {
      bitDoms(level)(i) = 0
      i += 1
    }
    bitDoms(level)(x) = Constants.MASK1(y)
    bindLevel = level
  }

  override def isBind(): Boolean = {
    bindLevel != 1
  }

  override def remove(a: Int): Unit = {
    val (x, y) = INDEX.getXY(a)
    bitDoms(level)(x) &= Constants.MASK0(y)
  }

  override def isEmpty(): Boolean = {
    for (a <- bitDoms(level)) {
      if (a != 0) {
        return false
      }
    }
    return true
  }

  override def clearMark(): Unit = {
    var i = 0
    while (i < numBit) {
      bitMark(i) = 0L
      i += 1
    }
  }

  override def mark(a: Int): Unit = {
    val (x, y) = INDEX.getXY(a)
    // mark中没有并且没有被删掉才加入mark
    if ((bitMark(x) & Constants.MASK1(y) & bitDoms(level)(x)) == 0L) {
      bitMark(x) |= Constants.MASK1(y)
    }
  }

  override def fullMark(): Boolean = {
    var i = 0
    while (i < numBit) {
      if (bitMark(i) != bitDoms(level)(i)) {
        return false
      }
      i += 1
    }
    return true
  }

  override def contains(a: Int): Boolean = {
    if (a == Constants.INDEXOVERFLOW) {
      return false
    }
    val (x, y) = INDEX.getXY(a)
    return (bitDoms(level)(x) & Constants.MASK1(y)) != 0L
  }

  override def minValue(): Int = {
    var i = 0
    while (i < numBit) {
      if (bitDoms(level)(i) != 0L) {
        return INDEX.getIndex(i, Constants.FirstLeft(bitDoms(level)(i)))
      }
      i += 1
    }
    return Constants.INDEXOVERFLOW
  }

  override def nextValue(a: Int): Int = {
    var b = a + 1
    while (b < capacity) {
      if (contains(b))
        return b
      else
        b += 1
    }
    return Constants.INDEXOVERFLOW
  }

  override def lastValue(): Int = ???

  override def preValue(a: Int): Int = ???

  override def maxValue(a: Int): Int = ???

  override def get(index: Int): Int = ???

  override def mask(m: Array[Long]): Unit = {
    var i = 0
    while (i < numBit) {
      m(i) = bitDoms(level)(i)
      i += 1
    }
  }

  override def getValidValues(values: ArrayBuffer[Int]): Int = {
    values.clear()
    var j = 0
    var end = 0
    var i = 0
    var base = 0

    while (i < numBit) {
      val a = bitDoms(level)(i)
      base = i * Constants.BITSIZE
      if (a != 0) {
        j = Constants.FirstLeft(a)
        end = Constants.FirstRight(a)
        while (j <= end) {
          if ((a & Constants.MASK1(j)) != 0) {
            values += (j + base)
          }
          j += 1
        }
      }
      i += 1
    }
    return values.length
  }

}
