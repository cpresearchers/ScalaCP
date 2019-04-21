package cpscala.TSolver.Model.Variable

import java.util.concurrent.atomic.{AtomicIntegerArray, AtomicLongArray}

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil.{Constants, INDEX}

class SafeBitSetVar(val name: String, val id: Int, num_vars: Int, vals: Array[Int], val helper: SearchHelper) extends PVar {
  // 总层数
  val numLevel: Int = num_vars + 3
  // 搜索树当前层数
  var curLevel: Int = 0

  // 论域初始大小
  override val capacity = vals.length
  // 论域比特组个数
  val numBit = Math.ceil(capacity.toDouble / Constants.BITSIZE.toDouble).toInt
  // 临时比特论域
  val bitTmp: Array[Long] = Array.fill[Long](numBit)(Constants.ALLONELONG)
  // 最后一个比特组的末尾无效位置清0
  bitTmp(numBit - 1) <<= (Constants.BITSIZE - capacity % Constants.BITSIZE)
  // 原子比特论域
  val bitDoms: Array[AtomicLongArray] = Array.fill[AtomicLongArray](numLevel)(new AtomicLongArray(bitTmp))

  val bitMark = new AtomicLongArray(numBit)
  var ii = 0
  while (ii < numBit) {
    bitMark.set(ii, 0)
    ii += 1
  }

  override def getNumBit(): Int = numBit

  override def newLevel(): Int = {
    val pre_level = curLevel
    curLevel += 1

    var i = 0
    while (i < numBit) {
      bitDoms(curLevel).set(i, bitDoms(pre_level).get(i))
      i += 1
    }
    return curLevel
  }

  override def backLevel(): Int = {
    // 若变量在当前层赋值，则撤销赋值
    if (bindLevel == curLevel) {
      bindLevel = Constants.kINTINF
    }
    curLevel -= 1
    return curLevel
  }

  //提交改动
  override def restrict(): Unit = {
    var previousBits: Long = 0L
    var newBits: Long = 0L

    var i = 0
    while (i < numBit) {
      do {
        previousBits = bitMark.get(i)
        // Clear the relevant bit
        newBits = bitDoms(curLevel).get(i) & previousBits
        // Try to set the new bit mask, and loop round until successful
      } while (!bitDoms(curLevel).compareAndSet(curLevel, previousBits, newBits))
      //      cur_size.set(java.lang.Long.bitCount(newBits))
      i += 1
    }
  }

  override def size(): Int = {
    var cursize = 0
    var i = 0
    while (i < numBit) {
      cursize += java.lang.Long.bitCount(bitDoms(curLevel).get(i))
      i += 1
    }

    return cursize
  }

  override def bind(a: Int): Unit = {
    val (x, y) = INDEX.getXY(a)
    var i = 0
    while (i < numBit) {
      bitDoms(curLevel).set(i, 0)
      i += 1
    }
    bitDoms(curLevel).set(x, Constants.MASK1(y))
    bindLevel = curLevel
  }

  override def isBind(): Boolean = {
    bindLevel != 1
  }

  override def remove(a: Int): Unit = {
    val (x, y) = INDEX.getXY(a)
    var previousBits: Long = 0L
    var newBits: Long = 0L

    do {
      previousBits = bitDoms(curLevel).get(x)
      // Clear the relevant bit
      newBits = previousBits & Constants.MASK0(y)
      // Try to set the new bit mask, and loop round until successful
    } while (!bitDoms(curLevel).compareAndSet(x, previousBits, newBits))
  }

  override def isEmpty(): Boolean = {
    var i = 0
    while (i < numBit) {
      if (bitDoms(curLevel).get(i) != 0L) {
        return false
      }
      i += 1
    }

    return true
  }

  override def clearMark(): Unit = {
    var i = 0
    while (i < numBit) {
      bitMark.set(i, 0L)
      i += 1
    }
  }

  override def mark(a: Int): Unit = {
    val (x, y) = INDEX.getXY(a)
    var previousBits: Long = 0L
    var newBits: Long = 0L

    do {
      previousBits = bitMark.get(x)
      // Clear the relevant bit
      newBits = previousBits | Constants.MASK1(y)
      // Try to set the new bit mask, and loop round until successful
    } while (!bitDoms(curLevel).compareAndSet(x, previousBits, newBits))
  }

  override def fullMark(): Boolean = {
    var i = 0
    while (i < numBit) {
      if (bitMark.get(i) != bitDoms(curLevel).get(i)) {
        return false
      }
      i += 1
    }
    return true
  }

  override def contains(a: Int): Boolean = {
    val (x, y) = INDEX.getXY(a)
    return (bitDoms(curLevel).get(x) & Constants.MASK1(y)) != 0L
  }

  override def minValue(): Int = {
    var i = 0
    while (i < numBit) {
      val a = bitDoms(curLevel).get(i)
      if (a != 0L) {
        return i * Constants.BITSIZE + Constants.FirstLeft(a)
      }
      i += 1
    }
    return Constants.INDEXOVERFLOW
  }

  override def nextValue(a: Int): Int = {
    var b = a + 1
    while (b < capacity && !contains(b)) {
      b += 1
    }

    if (b < capacity) {
      return b
    }
    else {
      return Constants.INDEXOVERFLOW
    }
  }

  override def lastValue(): Int = ???

  override def preValue(a: Int): Int = ???

  override def maxValue(a: Int): Int = ???

  override def mask(mask: Array[Long]): Unit = {
    var i = 0
    while (i < numBit) {
      mask(i) = bitDoms(curLevel).get(i)
      i += 1
    }
  }

  override def submitMask(mask: Array[Long]): Boolean = {
    var previousBits: Long = 0L
    var newBits: Long = 0L
    var changed = false
    var i = 0
    while (i < numBit) {
      do {
        previousBits = bitDoms(curLevel).get(i)
        // Clear the relevant bit
        newBits = previousBits & mask(i)
        // Try to set the new bit mask, and loop round until successful
      } while (!bitDoms(curLevel).compareAndSet(i, previousBits, newBits))

      if (previousBits != newBits) {
        changed = true
      }

      i += 1
    }
    return changed
  }

  override def submitMaskAndGet(mask: Array[Long]): Long = ???

  override def getAndSubmitMask(mask: Array[Long]): Long = ???

  override def show(): Unit = {
    println(s"     var: ${id}, size: ${size()}")
    var i = 0
    while (i < numBit) {
      //      println(s"     mask${i}: ${Constants.toFormatBinaryString(bitDoms(curLevel).get(i))}")
      i += 1
    }
  }

  override def get(index: Int): Int = ???

  override def isChanged(mask: Array[Long]): Boolean = {
    var ii = 0
    while (ii < numBit) {
      if (bitDoms(level).get(ii) != mask(ii)) {
        return true
      }
      ii += 1
    }
    return false
  }
}
