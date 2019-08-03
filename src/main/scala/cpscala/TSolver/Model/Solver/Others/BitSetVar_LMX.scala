package cpscala.TSolver.Model.Solver.Others

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

class BitSetVar_LMX(val name: String, val id: Int, numVars: Int, vals: Array[Int], val helper: LMXSearchHelper, val parallelism: Int) extends Var {
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
  //  val tmpLevels = new LMXSparseSet(parallelism, startTmpLevel)

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

  def newTmpLevel(m: MultiLevel): Int = {
    // 先获取最新层
    var i = 0
    while (i < numBit) {
      bitDoms(m.tmpLevel)(i) = bitDoms(level)(i)
      i += 1
    }
    return m.tmpLevel
  }

  def deleteTmpLevel(m: MultiLevel) = {
    //    tmpLevels.remove(m)
  }

  override def backLevel(): Int = {
    // 若变量在当前层赋值，则撤销赋值
    if (bindLevel == level) {
      bindLevel = Constants.kINTINF
    }
    level -= 1
    return level
  }

  def backLevel(i: Int): Int = {
    // 若变量在当前层赋值，则撤销赋值
    if (bindLevel > i) {
      bindLevel = Constants.kINTINF
    }
    level -= i
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
    helper.domainLock.lock()
    var curr_size = 0
    for (a <- bitDoms(level)) {
      curr_size += java.lang.Long.bitCount(a)
    }
    //    bitDoms(level).foreach(a => curr_size += java.lang.Long.bitCount(a))
    helper.domainLock.unlock()
    return curr_size

  }

  def size(m: MultiLevel): Int = {
    var curr_size = 0
    for (a <- bitDoms(m.tmpLevel)) {
      curr_size += java.lang.Long.bitCount(a)
    }
    return curr_size
  }

  override def bind(a: Int): Unit = {
    helper.domainLock.lock()
    val (x, y) = INDEX.getXY(a)
    var i = 0
    while (i < numBit) {
      bitDoms(level)(i) = 0
      i += 1
    }
    bitDoms(level)(x) = Constants.MASK1(y)
    bindLevel = level
    helper.domainLock.unlock()
  }

  //  override def isBind(): Boolean = {
  //    bindLevel != 1
  //}

  override def remove(a: Int): Unit = {
    helper.domainLock.lock()
    val (x, y) = INDEX.getXY(a)
    bitDoms(level)(x) &= Constants.MASK0(y)
    helper.domainLock.unlock()
  }

  def remove(a: Int, m: MultiLevel): Unit = {
    val (x, y) = INDEX.getXY(a)
    bitDoms(m.tmpLevel)(x) &= Constants.MASK0(y)
  }

  override def isEmpty(): Boolean = {
    for (a <- bitDoms(level)) {
      if (a != 0) {
        return false
      }
    }
    return true
  }

  def isEmpty(m: MultiLevel): Boolean = {
    for (a <- bitDoms(m.tmpLevel)) {
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

  def contains(a: Int, m: MultiLevel): Boolean = {
    if (a == Constants.INDEXOVERFLOW) {
      return false
    }
    val (x, y) = INDEX.getXY(a)
    return (bitDoms(m.tmpLevel)(x) & Constants.MASK1(y)) != 0L
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

  def minValue(m: MultiLevel): Int = {
    var i = 0
    while (i < numBit) {
      if (bitDoms(m.tmpLevel)(i) != 0L) {
        return INDEX.getIndex(i, Constants.FirstLeft(bitDoms(m.tmpLevel)(i)))
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

  def nextValue(a: Int, m: MultiLevel): Int = {
    var b = a + 1
    while (b < capacity) {
      if (contains(b, m))
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

  def mask(m: Array[Long], a: Int): Unit = {
    var i = 0
    while (i < numBit) {
      m(i) = bitDoms(a)(i)
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


  def getValidValues(values: ArrayBuffer[Int], m: MultiLevel): Int = {
    values.clear()
    var j = 0
    var end = 0
    var i = 0
    var base = 0

    while (i < numBit) {
      val a = bitDoms(m.tmpLevel)(i)
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

  override def show(): Unit = {
    var sss = bitDoms(level)(0).toBinaryString
    var ii = sss.length
    while (ii < 64) {
      sss = '0' + sss
      ii += 1
    }
    println(s"id:${id},${sss}")
  }
}
