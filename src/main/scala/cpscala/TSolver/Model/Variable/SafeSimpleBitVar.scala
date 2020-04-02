package cpscala.TSolver.Model.Variable

import java.util.concurrent.atomic._

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil._

import scala.collection.mutable.ArrayBuffer
//import java.lang.Long

class SafeSimpleBitVar(val name: String, val id: Int, num_vars: Int, vals: Array[Int], val helper: SearchHelper) extends PVar {
  override val capacity = vals.length
  // 如果论域大于64则返回false

  val limit = capacity
  var cur_level: Int = 0
  var cur_size = new AtomicInteger(capacity)
  var last_size: Int = capacity
  var binded_level = -1
  // 总层数
  val num_level = num_vars + 3

  //只用于记录size的历史信息
  val level_size = Array.fill(num_level)(-1)
  level_size(0) = capacity

  //  val bitTmp = new AtomicLong(Constants.ALLONELONG << (Constants.BITSIZE - limit))
  var bit_mark = new AtomicLong(0L)
  //  var mark_size = new AtomicInteger(0)
  val bit_doms = new AtomicLongArray(num_level)
  //  val bitDoms = new Array[Long](numLevel)

  // 初始化第0级的bitDom
  bit_doms.set(0, Constants.ALLONELONG << (Constants.BITSIZE - limit))
  var ii = 0

  override def newLevel(): Int = {
    val pre_level = cur_level
    cur_level += 1
    bit_doms.set(cur_level, bit_doms.get(pre_level))
    level_size(pre_level) = cur_size.get()
    level_size(cur_level) = -1
    return cur_level
  }

  override def backLevel(): Int = {
    // 当前level_size置-1
    // 若变量在当前层赋值，则撤销赋值
    level_size(cur_level) = -1
    if (binded_level == cur_level) {
      binded_level = Constants.kINTMAXINF
    }

    // 当前层size置0，这一层不要了
    level_size(cur_level) = -1
    // 回到上一层，当前size等于当前层的上一层的size
    cur_level -= 1
    // 当前size = 当前层的上一层
    cur_size.set(level_size(cur_level))
    return cur_level
  }

  //提交改动
  override def restrict(): Unit = {
    var previousBits: Long = 0L
    var newBits: Long = 0L
    previousBits = bit_mark.get()
    newBits = bit_doms.get(cur_level) & previousBits
    bit_doms.set(cur_level, newBits)
    //    bitDoms.compareAndSet(curLevel, previousBits, newBits)
    cur_size.set(java.lang.Long.bitCount(newBits))
  }

  override def size(): Int = java.lang.Long.bitCount(bit_doms.get(cur_level))

  override def bind(a: Int): Unit = {
    bit_doms.set(cur_level, Constants.MASK1(a))
    cur_size.set(1)
    binded_level = cur_level
  }

  override def isBind(): Boolean = {
    binded_level != -1
  }

  override def remove(a: Int): Unit = {
    var previousBits: Long = 0L
    var newBits: Long = 0L

    do {
      previousBits = bit_doms.get(cur_level)
      // Clear the relevant bit
      newBits = previousBits & Constants.MASK0(a)
      // Try to set the new bit mask, and loop round until successful
    } while (!bit_doms.compareAndSet(cur_level, previousBits, newBits))

    cur_size.decrementAndGet()
  }

  override def isEmpty(): Boolean = {
    return bit_doms.get(cur_level) == 0L
  }

  override def clearMark(): Unit = {
    bit_mark.set(0L)
  }

  override def mark(a: Int): Unit = {
    //    bitMark.set(bitMark.get() | Constants.MASK1(a))

    var previousBits: Long = 0L
    var newBits: Long = 0L

    do {
      previousBits = bit_mark.get()
      // Clear the relevant bit
      newBits = previousBits | Constants.MASK1(a)
      // Try to set the new bit mask, and loop round until successful
    } while (!bit_doms.compareAndSet(cur_level, previousBits, newBits))
  }

  override def fullMark(): Boolean = {
    return bit_mark.get() == bit_doms.get(cur_level)
    //    mark_size == cur_size
  }

  override def contains(a: Int): Boolean = {
    if (a == Constants.INDEXOVERFLOW) {
      return false
    }
    return (bit_doms.get(cur_level) & Constants.MASK1(a)) != 0L
  }

  override def minValue(): Int = {
    if (bit_doms.get(cur_level) != 0L) {
      return Constants.FirstLeft(bit_doms.get(cur_level))
    } else {
      return Constants.INDEXOVERFLOW
    }
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

  override def simpleMask(): Long = bit_doms.get(cur_level)

  def isSame(mask: Long): Boolean = {
    bit_doms.get(cur_level) == mask
  }

  // 提交改动后有改变，则返回true
  override def submitMask(mask: Long): Boolean = {
    var previousBits: Long = 0L
    var newBits: Long = 0L

    do {
      previousBits = bit_doms.get(cur_level)
      // Clear the relevant bit
      newBits = previousBits & mask
      // Try to set the new bit mask, and loop round until successful
    } while (!bit_doms.compareAndSet(cur_level, previousBits, newBits))

    if (previousBits != newBits) {
      return true
    }
    else {
      return false
    }
  }

  override def submitMaskAndGet(mask: Long): Long = {
    var previousBits: Long = 0L
    var newBits: Long = 0L

    do {
      previousBits = bit_doms.get(cur_level)
      // Clear the relevant bit
      newBits = previousBits & mask
      // Try to set the new bit mask, and loop round until successful
    } while (!bit_doms.compareAndSet(cur_level, previousBits, newBits))

    return newBits
  }

  override def getAndSubmitMask(mask: Long): Long = {
    var previousBits: Long = 0L
    var newBits: Long = 0L

    do {
      previousBits = bit_doms.get(cur_level)
      // Clear the relevant bit
      newBits = previousBits & mask
      // Try to set the new bit mask, and loop round until successful
    } while (!bit_doms.compareAndSet(cur_level, previousBits, newBits))

    return previousBits
  }

  override def lastValue(): Int = ???

  override def preValue(a: Int): Int = ???

  override def maxValue(a: Int): Int = ???

  override def get(index: Int): Int = ???

  override def isChanged(mask: Array[Long]): Boolean = ???
}
