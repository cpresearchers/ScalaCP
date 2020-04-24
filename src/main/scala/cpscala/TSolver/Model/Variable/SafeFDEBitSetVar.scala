package cpscala.TSolver.Model.Variable

import java.util.concurrent.atomic.{AtomicInteger, AtomicIntegerArray, AtomicLong, AtomicLongArray}

import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper

import scala.collection.mutable.ArrayBuffer
//import scala.collection.parallel.immutable
import scala.collection.{immutable, mutable}

class SafeFDEBitSetVar(val name: String, val id: Int, numVars: Int, vals: Array[Int], val helper: SearchHelper) extends PVar {
  // 总层数
  val numLevel = numVars + 3

  override val capacity = vals.length
  val numBit = Math.ceil(capacity.toDouble / Constants.BITSIZE.toDouble).toInt
  val bitMark = new AtomicLongArray(numBit)
  //  var ii = 0
  //  while (ii < numBit) {
  //    bitMark.set(ii, 0)
  //    ii += 1
  //  }

  // 临时比特论域
  val bitTmp: Array[Long] = Array.fill[Long](numBit)(Constants.ALLONELONG)
  // 最后一个比特组的末尾无效位置清0
  bitTmp(numBit - 1) <<= (Constants.BITSIZE - capacity % Constants.BITSIZE)
  //  val bitDoms = Array.fill[Long](numLevel, numBit)(0L)
  val bitDoms: Vector[AtomicLongArray] = Vector.fill[AtomicLongArray](numLevel)(new AtomicLongArray(bitTmp))
  //    Array.fill[AtomicLongArray](numLevel)(new AtomicLongArray(bitTmp))

  //  var word = Array.fill(numBit)(0L)

  // 初始化第0级的bitDom
  //  var ii = 0
  //  while (ii < numBit) {
  //    bitDoms(0)(ii) = Constants.ALLONELONG
  //    ii += 1
  //  }
  //  bitDoms(0)(numBit - 1) <<= (Constants.BITSIZE - capacity % Constants.BITSIZE)
  //  word(numBit - 1) <<= (Constants.BITSIZE - capacity % Constants.BITSIZE)

  override def getNumBit(): Int = numBit

  override def newLevel(): Int = {
    val pre_level = level
    level += 1

    var i = 0
    while (i < numBit) {
      //      bitDoms(level)(i) = bitDoms(pre_level)(i)
      bitDoms(level).set(i, bitDoms(pre_level).get(i))
      i += 1

    }

    return level
  }

  override def backLevel(): Int = {
    // 若变量在当前层赋值，则撤销赋值
    if (bindLevel == level) {
      bindLevel = Constants.kINTMAXINF
    }
    level -= 1
    return level
  }

  //  //提交改动
  //  override def restrict(): Unit = {
  //    var i = 0
  //    while (i < numBit) {
  //      bitDoms(level)(i) &= bitMark(i)
  //      i += 1
  //    }
  //  }

  override def size(): Int = {
    var cursize = 0
    var i = 0
    while (i < numBit) {
      cursize += java.lang.Long.bitCount(bitDoms(level).get(i))
      i += 1
    }

    return cursize
  }

  override def bind(a: Int): Unit = {
    val (x, y) = INDEX.getXY(a)
    var i = 0
    while (i < numBit) {
      bitDoms(level).set(i, 0)
      i += 1
    }
    bitDoms(level).set(x, Constants.MASK1(y))
    bindLevel = level
  }

  override def isBind(): Boolean = {
    bindLevel != 1
  }

  override def remove(a: Int): Unit = {
    val (x, y) = INDEX.getXY(a)
    var previousBits: Long = 0L
    var newBits: Long = 0L

    do {
      previousBits = bitDoms(level).get(x)
      // Clear the relevant bit
      newBits = previousBits & Constants.MASK0(y)
      // Try to set the new bit mask, and loop round until successful
    } while (!bitDoms(level).compareAndSet(x, previousBits, newBits))
  }


  override def submitMask(mask: Array[Long]): Boolean = {
    var previousBits: Long = 0L
    var newBits: Long = 0L
    var changed = false
    var i = 0
    while (i < numBit) {
      do {
        previousBits = bitDoms(level).get(i)
        // Clear the relevant bit
        newBits = previousBits & mask(i)
        // Try to set the new bit mask, and loop round until successful
      } while (!bitDoms(level).compareAndSet(i, previousBits, newBits))

      if (previousBits != newBits) {
        changed = true
      }

      i += 1
    }
    return changed
  }

  override def isEmpty(): Boolean = {
    var i = 0
    while (i < numBit) {
      if (bitDoms(level).get(i) != 0L) {
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

  //  override def mark(a: Int): Unit = {
  //    val (x, y) = INDEX.getXY(a)
  //    // mark中没有并且没有被删掉才加入mark
  //    if ((bitMark(x) & Constants.MASK1(y) & bitDoms(level)(x)) == 0L) {
  //      bitMark(x) |= Constants.MASK1(y)
  //    }
  //  }

  override def mark(a: Int): Unit = {
    val (x, y) = INDEX.getXY(a)
    var previousBits: Long = 0L
    var newBits: Long = 0L

    do {
      previousBits = bitMark.get(x)
      // Clear the relevant bit
      newBits = previousBits | Constants.MASK1(y)
      // Try to set the new bit mask, and loop round until successful
    } while (!bitDoms(level).compareAndSet(x, previousBits, newBits))
  }

  //  override def fullMark(): Boolean = {
  //    var i = 0
  //    while (i < numBit) {
  //      if (bitMark(i) != bitDoms(level)(i)) {
  //        return false
  //      }
  //      i += 1
  //    }
  //    return true
  //  }
  override def fullMark(): Boolean = {
    var i = 0
    while (i < numBit) {
      if (bitMark.get(i) != bitDoms(level).get(i)) {
        return false
      }
      i += 1
    }
    return true
  }

  //  override def contains(a: Int): Boolean = {
  //    if (a == Constants.INDEXOVERFLOW) {
  //      return false
  //    }
  //    val (x, y) = INDEX.getXY(a)
  //    return (bitDoms(level)(x) & Constants.MASK1(y)) != 0L
  //  }

  override def contains(a: Int): Boolean = {
    val (x, y) = INDEX.getXY(a)
    return (bitDoms(level).get(x) & Constants.MASK1(y)) != 0L
  }

  //  override def minValue(): Int = {
  //    var i = 0
  //    while (i < numBit) {
  //      if (bitDoms(level)(i) != 0L) {
  //        return INDEX.getIndex(i, Constants.FirstLeft(bitDoms(level)(i)))
  //      }
  //      i += 1
  //    }
  //    return Constants.INDEXOVERFLOW
  //  }

  override def minValue(): Int = {
    var i = 0
    while (i < numBit) {
      val a = bitDoms(level).get(i)
      if (a != 0L) {
        return i * Constants.BITSIZE + Constants.FirstLeft(a)
      }
      i += 1
    }
    return Constants.INDEXOVERFLOW
  }

  //  override def nextValue(a: Int): Int = {
  //    var b = a + 1
  //    while (b < capacity) {
  //      if (contains(b))
  //        return b
  //      else
  //        b += 1
  //    }
  //    return Constants.INDEXOVERFLOW
  //  }

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

  override def get(index: Int): Int = ???

  //  override def mask(m: Array[Long]): Unit = {
  //    var i = 0
  //    while (i < numBit) {
  //      m(i) = bitDoms(level)(i)
  //      i += 1
  //    }
  //  }

  override def mask(mask: Array[Long]): Unit = {
    var i = 0
    while (i < numBit) {
      mask(i) = bitDoms(level).get(i)
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
      val a = bitDoms(level).get(i)
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

  override def removeValues(words: Array[Long]): Boolean = {
    //本表默认未修改
    var changed = false
    var newBits = 0L
    var previousBits = 0L

    var i = 0
    while (i < numBit) {
      do {
        previousBits = bitDoms(level).get(i)
        // Clear the relevant bit
        newBits = previousBits & words(i)
        // Try to set the new bit mask, and loop round until successful
      } while (!bitDoms(level).compareAndSet(i, previousBits, newBits))

      if (previousBits != newBits) {
        changed = true
      }

      i += 1
    }
    //记录是否改变
    return changed
  }

  override def getAtomicBitDom(): AtomicLongArray = {
    val c = bitDoms(level)
    return c
  }


  //  def getLastRemovedValuesByMask(oldSize: Long, vals: ArrayBuffer[Int]): Int = ???

  override def show(): Unit = {
    print("var = " + id + ", level = " + level + " size = " + size() + " ")
    //    for (i <- 0 until numBit) {
    //      printf(bitDoms(level).get(i).toBinaryString)
    //    }

    println("[ " + Constants.getValues(bitDoms(level)).mkString(" ") + " ]")
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
        newBits = bitDoms(level).get(i) & previousBits
        // Try to set the new bit mask, and loop round until successful
      } while (!bitDoms(level).compareAndSet(level, previousBits, newBits))
      //      cur_size.set(java.lang.Long.bitCount(newBits))
      i += 1
    }
  }

  override def submitMaskAndIsSame(mask: Array[Long]): (Boolean, Boolean) = {
    var previousBits: Long = 0L
    var newBits: Long = 0L
    var changed = false
    var same = true
    var i = 0
    while (i < numBit) {
      do {
        previousBits = bitDoms(level).get(i)
        // Clear the relevant bit
        newBits = previousBits & mask(i)
        // Try to set the new bit mask, and loop round until successful
      } while (!bitDoms(level).compareAndSet(i, previousBits, newBits))

      if (previousBits != newBits) {
        changed = true
      }

      if (mask(i) != newBits) {
        same = false
      }

      i += 1
    }
    return (changed, same)
  }
}
