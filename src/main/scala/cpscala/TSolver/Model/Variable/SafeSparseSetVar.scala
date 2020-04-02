package cpscala.TSolver.Model.Variable

import java.util.concurrent.atomic.{AtomicInteger, AtomicIntegerArray, AtomicLongArray}

import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper

import scala.collection.mutable.ArrayBuffer

class SafeSparseSetVar(val name: String, val id: Int, num_vars: Int, vals: Array[Int], val helper: SearchHelper) extends PVar {
  // 总层数
  val numLevel: Int = num_vars + 3
  // 搜索树当前层数
  var curLevel: Int = 0

  // 论域初始大小
  override val capacity = vals.length

  ////////////////////////
  // BIT 相关
  ////////////////////////
  // 论域比特组个数
  val numBit = Math.ceil(capacity.toDouble / Constants.BITSIZE.toDouble).toInt
  // 临时比特论域
  //  val bitTmp: Array[Long] = Array.fill[Long](numBit)(Constants.ALLONELONG)
  // 最后一个比特组的末尾无效位置清0
  //  bitTmp(numBit - 1) <<= (Constants.BITSIZE - capacity % Constants.BITSIZE)
  // 原子比特论域
  //  val bitDoms: Array[AtomicLongArray] = Array.fill[AtomicLongArray](numLevel)(new AtomicLongArray(bitTmp))

  //  val bitMark = new AtomicLongArray(numBit)
  //  var ii = 0
  //  while (ii < numBit) {
  //    bitMark.set(ii, 0)
  //    ii += 1
  //  }
  ////////////////////////

  // 用于多线程的时间戳部分
  //记录stamp所对应的值
  // 值为正就是仅删除值
  // 值为负就是仅保留值
  // 初始值为-1
  // 相当于dense数组
  val stamp2Val = ArrayBuffer.fill[Int](capacity)(Constants.INDEXOVERFLOW)
  // 记录删值时的stamp
  // 这里的值做为索引不分正负
  // 初始值全为0 代表存在
  // 相当于sparse数组
  val val2Stamp = new AtomicIntegerArray(capacity)


  // 原子时间戳
  // 大于等于此值都是在的
  // 从0开始
  val atomicStamp = new AtomicInteger(0)

  // 此层的第一个
  val stamps = Array.fill(capacity)(0)

  // 供别的方法使用
  override def getNumBit(): Int = numBit

  override def newLevel(): Int = {
    level += 1
    stamps(level) = atomicStamp.get()
    return level
  }

  override def backLevel(): Int = {
    // !!注意边界
    var i = atomicStamp.get()
    while (i > stamps(level)) {
      stamp2Val(i) = Constants.INDEXOVERFLOW
      i -= 1
    }

    stamps(level) = Constants.INDEXOVERFLOW
    if (bindLevel == level) {
      bindLevel = Constants.kINTMAXINF
    }
    level -= 1

    return level
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

  // 传入的一定是正数
  def getValueByPositiveStamp(s: Int): Int = {
    return stamp2Val(s)
  }

  // 第一个返回值是变量值，
  // 第二个返回值是动作：
  // 0 = 未知
  // 1 = 删值
  // 2 = 留值
  // 3 = 尚未写入
  def getValueByStamp(s: Int): (Int, Int) = {
    val a = stamp2Val(s)
    if (s < 0) {
      return (a, 2)
    } else if (s > 0) {
      //
      if (a == -1) {
        return (a, 3)
      } else {
        return (a, 1)
      }
    } else {
      return (Constants.kINTMININF, 0)
    }
  }

  override def remove(a: Int): Unit = {
    // 若为0，分配新值
    //    var newStamp = 0
    //    if (val2Stamp.compareAndSet(a, 0, {
    //      newStamp = atomicStamp.getAndIncrement();
    //      newStamp
    //    })) {
    //      stamp2Val(newStamp) = a
    //    }

    if (val2Stamp.compareAndSet(a, 0, Constants.kINTMAXINF)) {
      val newStamp = atomicStamp.getAndIncrement()
      stamp2Val(newStamp) = a
    }
  }


  // 原子地检查一个值是否有效，可与下一个函数合并
  def CheckValidityByStamp(index: Int): Boolean = {
    var capa = 0
    do {
      capa = val2Stamp.get(index)
      // 若大于0则表示该值删除了
      if (capa != 0) return false
    } while (!val2Stamp.compareAndSet(index, capa, Constants.kINTMAXINF))

    // 若为0，分配新值
    return true
  }

  //
  //  def IncreaseStampAndMarkValue(index: Int): Unit = {
  //    var capa = 0
  //    do {
  //      capa = atomicStamp.get()
  //      //      val2Stamp.set(index, capa)
  //    } while (!atomicStamp.compareAndSet(capa, capa + 1))
  //    //计算成功后再添入，不成功不添入stamp2Val默认为-1
  //    stamp2Val(capa + 1) = index
  //  }

  //  def IncreaseStampAndMarkValue(index: Int, atomicStamps: AtomicInteger): Unit = {
  //    var capa = 0
  //    do {
  //      capa = atomicStamp.get()
  //      val2Stamp.set(index, capa)
  //    } while (!atomicStamps.compareAndSet(capa, capa + 1))
  //    //计算成功后再添入，不成功不添入stamp2Val默认为-1
  //    stamp2Val(capa + 1) = index
  //  }

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

  override def submitMaskAndIsSame(mask: Array[Long]): (Boolean, Boolean) = {
    var previousBits: Long = 0L
    var newBits: Long = 0L
    var changed = false
    var same = true
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

      if (mask(i) != newBits) {
        same = false
      }

      i += 1
    }
    return (changed, same)
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

