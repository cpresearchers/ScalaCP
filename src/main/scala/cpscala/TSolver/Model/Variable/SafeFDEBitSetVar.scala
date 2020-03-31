package cpscala.TSolver.Model.Variable

import java.util.concurrent.atomic.{AtomicInteger, AtomicIntegerArray, AtomicLong, AtomicLongArray}

import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.CpUtil.SearchHelper.{PFDESearchHelper, SearchHelper}

import scala.collection.mutable.ArrayBuffer

class SafeFDEBitSetVar(val name: String, val id: Int, numVars: Int, vals: Array[Int], val helper: PFDESearchHelper) extends PVar {
  // 总层数
  val numLevel = numVars + 3

  override val capacity = vals.length
  val numBit = Math.ceil(capacity.toDouble / Constants.BITSIZE.toDouble).toInt
  val bitMark = Array.fill[Long](numBit)(0L)
  val bitDoms = Array.fill[Long](numLevel, numBit)(0L)
  //  val bitDoms = Array.fill[AtomicLongArray](numLevel)(new AtomicLongArray(numBit))
  var word = Array.fill(numBit)(0L)

  // 初始化第0级的bitDom
  var ii = 0
  while (ii < numBit) {
    bitDoms(0)(ii) = Constants.ALLONELONG
    //    bitDoms(0).set(ii, Constants.ALLONELONG)
    ii += 1
  }
  bitDoms(0)(numBit - 1) <<= (Constants.BITSIZE - capacity % Constants.BITSIZE)
  //  bitDoms(0).set(numBit - 1, bitDoms(0).get(numBit) << (Constants.BITSIZE - capacity % Constants.BITSIZE))
  word(numBit - 1) <<= (Constants.BITSIZE - capacity % Constants.BITSIZE)

  // 用于多线程的时间戳部分
  //记录stamp所对应的值
  // 值为正就是仅删除值
  // 值为负就是仅保留值
  val stamp2Val = ArrayBuffer.fill[Int](capacity)(-1)
  // 记录删值时的stamp
  // 这里的值做为索引不分正负
  //  val val2Stamp = Array.fill[Long](capacity)(-1)
  // 初值为最大值
  val val2Stamp = new AtomicIntegerArray(capacity)
  var ii = 0
  while (ii < capacity) {
    val2Stamp.set(ii, Int.MaxValue)
    ii += 1
  }

  // 原子时间戳
  // 大于等于此值都是在的
  val atomicStamp = new AtomicInteger(0)

  // 此层的第一个
  var baseStamp = 0
  val stamps = Array.fill(numVars)(Constants.INDEXOVERFLOW)
  var topStamp: Int

  override def getNumBit(): Int = numBit

  override def newLevel(): Int = {
    // 检查时间戳
    //    checkStamps()
    stamps(level) = atomicStamp.get()

    val pre_level = level
    level += 1

    var i = 0
    while (i < numBit) {
      bitDoms(level)(i) = bitDoms(pre_level)(i)
      i += 1
    }

    return level
  }

  override def backLevel(): Int = {
    // 检查时间戳

    // 若变量在当前层赋值，则撤销赋值
    if (bindLevel == level) {
      bindLevel = Constants.kINTINF
    }
    level -= 1

    atomicStamp.set(stamps(level))

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

  // safe remove value a
  // 安全删值版本
  override def remove(a: Int): Unit = {

    // 一个值a的stamp有效 那么a.s
    if (CheckValidityByStamp(a, val2Stamp)) {
      // bitdom上删值
      val (x, y) = INDEX.getXY(a)
      bitDoms(level)(x) &= Constants.MASK0(y)

      // 再原子增一次
      // 由于不用
      //      val2Stamp.set(a, s)

      // 全局时间戳自增
      //      val s = atomicStamp.incrementAndGet()
      IncreaseStampAndMarkValue(a, atomicStamp)
    }

    //    if (val2Stamp.updateAndGet(a, x -> x < baseStamp ? baseStamp: x) == newValue)

    //    val previousBits = bitDoms(level)(x)
    //    // Clear the relevant bit
    //    val newBits = Constants.MASK0(y) & previousBits
    //    // Try to set the new bit mask, and loop round until successful
    //    return !bitDoms(level).compareAndSet(level, previousBits, newBits)

  }


  //  def tryIncrementAndGetIfLessThan(stamps: AtomicLong, upperbound: Int): Boolean = {
  //    while (true) {
  //      val current = stamps.get()
  //      if (current >= baseStamp) return false
  //      if (stamps.compareAndSet(current, Long.MaxValue)) return true
  //    }
  //  }


  // 原子地检查一个值是否有效
  def CheckValidityByStamp(index: Int, atomicStamps: AtomicIntegerArray): Boolean = {
    var capa = 0
    do {
      capa = atomicStamps.get(index)
      // 若大于等于baseStamp 则该值已删除返回false
      if (capa <= baseStamp) return false
    } while (!atomicStamps.compareAndSet(index, capa, Int.MaxValue))
    return true
  }

  // 原子地检查一个值是否有效，可与下一个函数合并
  def CheckValidityByStamp(index: Int): Boolean = {
    var capa = 0
    do {
      capa = val2Stamp.get(index)
      // 若大于等于baseStamp 则该值已删除返回false
      if (capa <= baseStamp) return false
    } while (!val2Stamp.compareAndSet(index, capa, Int.MaxValue))
    return true
  }

  def IncreaseStampAndMarkValue(index: Int, atomicStamps: AtomicInteger): Boolean = {
    var capa = 0
    do {
      capa = atomicStamp.get()
      val2Stamp.set(index, capa)
      stamp2Val(capa) = index
    } while (!atomicStamps.compareAndSet(capa, Int.MaxValue))
  }

  // return true if the assignment was made, false otherwise
  def greaterThanCAS(index: Int, newValue: Int): Boolean = val2Stamp.getAndUpdate(index, (x) => if (x < newValue) newValue else x) < newValue

  override def isEmpty(): Boolean = {
    for (a <- bitDoms(level)) {
      if (a != 0) {
        return false
      }
    }
    return true
  }

//  def getDeltaValues(s: Int) = Int {
//    return
//
//  }

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

  override def removeValues(words: Array[Long]): Boolean = {
    //本表默认未修改
    var changed = false
    var w = 0L
    var currentWords = 0L
    var i = 0
    while (i < numBit) {
      currentWords = bitDoms(level)(i)
      w = currentWords & words(i)
      if (w != currentWords) {
        bitDoms(level)(i) = w
        //本表已修改
        changed = true
      }
      i += 1
    }
    //记录是否改变
    return changed
  }

  override def getBitDom(): Array[Long] = {
    var i = 0
    while (i < numBit) {
      word(i) = bitDoms(level)(i)
      i += 1
    }
    return word
  }

  //  def getLastRemovedValuesByMask(oldSize: Long, vals: ArrayBuffer[Int]): Int = ???

  override def show(): Unit = {
    print("var = " + id + ", level = " + level + " ")
    for (i <- 0 until numBit) {
      printf("%x ", bitDoms(level)(i))
    }
    println()
  }
}

