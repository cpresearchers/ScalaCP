//package cpscala.TSolver.Model.Variable
//
//import java.util.concurrent.atomic.{AtomicInteger, AtomicIntegerArray, AtomicLongArray}
//
//import cpscala.TSolver.CpUtil.{Constants, INDEX}
//import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
//
//import scala.collection.mutable.ArrayBuffer
//
//class SafeSparseSetVar(val name: String, val id: Int, num_vars: Int, vals: Array[Int], val helper: SearchHelper) extends PVar {
//  // 总层数
//  val numLevel: Int = num_vars + 3
//  // 搜索树当前层数
//  var curLevel: Int = 0
//
//  // 论域初始大小
//  override val capacity = vals.length
//  // [原子] 论域大小
//  var currentSize = new AtomicInteger(capacity)
//
//  // 记录每一层论域大小用于回溯
//  val sizeLevel: Array[Int] = Array.fill(numLevel)(Constants.INDEXOVERFLOW)
//  val dense = new AtomicIntegerArray(capacity)
//  val sparse = new AtomicIntegerArray(capacity)
//
//  var ii = 0
//  while (ii < capacity) {
//    dense.set(ii, Constants.markValue(ii))
//    sparse.set(ii, Constants.markValue(ii))
//    ii += 1
//  }
//
//  ////////////////////////
//  // BIT 相关
//  ////////////////////////
//  // 论域比特组个数
//  val numBit = math.ceil(capacity.toDouble / Constants.BITSIZE.toDouble).toInt
//  // 临时比特论域
//  //  val bitTmp: Array[Long] = Array.fill[Long](numBit)(Constants.ALLONELONG)
//  // 最后一个比特组的末尾无效位置清0
//  //  bitTmp(numBit - 1) <<= (Constants.BITSIZE - capacity % Constants.BITSIZE)
//  // 原子比特论域
//  //  val bitDoms: Array[AtomicLongArray] = Array.fill[AtomicLongArray](numLevel)(new AtomicLongArray(bitTmp))
//
//  //  val bitMark = new AtomicLongArray(numBit)
//  //  var ii = 0
//  //  while (ii < numBit) {
//  //    bitMark.set(ii, 0)
//  //    ii += 1
//  //  }
//  ////////////////////////
//
//  // 用于多线程的时间戳部分
//  // 记录stamp所对应的值
//  // 值部由两个部分组成，通过标识部和值部组成。
//  // 值部可存储十亿级的数据
//  // 标识部可以表达两类情况
//  // 值为正就是仅删除值
//  // 值为负就是仅保留值
//  // 初始值为-1
//  // 相当于dense数组
//  //  val stamp2Val = ArrayBuffer.fill[Int](capacity)(Constants.INDEXOVERFLOW)
//
//
//  // 记录删值时的stamp
//  // 这里的值做为索引不分正负
//  // 初始值全为0 代表存在
//  // 相当于sparse数组
//  // 初值为 0^mark~capacity^mark
//  //  val val2Stamp = new AtomicIntegerArray(capacity)
//
//  // 记录stamp所对应的值
//  // 值部由两个部分组成，通过标识部和值部组成。
//  // 值部可存储十亿级的数据
//  // 标识部可以表达两类情况
//  // 值为正就是仅删除值
//  // 值为负就是仅保留值
//  // 由于所有的值没
//  // 初始值为markedValue
//  // 原子交换后改为
//  // 相当于dense数组
//  // 但删值在前留值在后
//  // 初值为 0^mark~capacity^mark
//  //  val stamp2Val = new AtomicIntegerArray(capacity)
//  //
//  //  var ii = 0
//  //  while (ii < capacity) {
//  //    val2Stamp.set(ii, Constants.markValue(ii))
//  //    stamp2Val.set(ii, Constants.markValue(ii))
//  //    ii += 1
//  //  }
//
//  // 原子时间戳
//  // 大于等于此值都是在的
//  // 从0开始
//  // mark就是删值，非就是留值
//  //  val atomicStamp = new AtomicInteger(0)
//
//  //   为每层记录其stamp，默认为-1
//  //  val stamps = Array.fill(capacity)(Constants.INDEXOVERFLOW)
//
//  // 供别的方法使用
//  override def getNumBit(): Int = numBit
//
//  override def newLevel(): Int = {
//    level += 1
//    sizeLevel(level) = currentSize.get()
//    return level
//  }
//
//  override def backLevel(): Int = {
//    sizeLevel(level) = Constants.INDEXOVERFLOW
//    if (bindLevel == level) {
//      bindLevel = Constants.kINTMAXINF
//    }
//    level -= 1
//
//    // dense恢复
//    var i = currentSize.get()
//    while (i > stamps(level)) {
//      //      stamp2Val(i) = Constants.INDEXOVERFLOW
//      val a = stamp2Val.get(i)
//      stamp2Val.set(i, Constants.markValue(a))
//      val j = capacity - i
//      val b = stamp2Val.get(j)
//      stamp2Val.set(j, Constants.markValue(b))
//      i -= 1
//    }
//
//    atomicStamp.set(sta)
//
//    return level
//  }
//
//  //提交改动
//  override def restrict(): Unit = {
//    //    var previousBits: Long = 0L
//    //    var newBits: Long = 0L
//    //
//    //    var i = 0
//    //    while (i < numBit) {
//    //      do {
//    //        previousBits = bitMark.get(i)
//    //        // Clear the relevant bit
//    //        newBits = bitDoms(curLevel).get(i) & previousBits
//    //        // Try to set the new bit mask, and loop round until successful
//    //      } while (!bitDoms(curLevel).compareAndSet(curLevel, previousBits, newBits))
//    //      //      cur_size.set(java.lang.Long.bitCount(newBits))
//    //      i += 1
//    //    }
//  }
//
//  override def size(): Int = {
//    return currentSize.get()
//  }
//
//  // 串行程序使用
//  override def bind(a: Int): Unit = {
//    //    bindLevel = level
//    //    val (x, y) = INDEX.getXY(a)
//    //    var i = 0
//    //    while (i < numBit) {
//    //      bitDoms(curLevel).set(i, 0)
//    //      i += 1
//    //    }
//    //    bitDoms(curLevel).set(x, Constants.MASK1(y))
//    //    bindLevel = curLevel
//
//    bindLevel = level
//    if (val2Stamp.compareAndSet(a, 0, Constants.kINTMAXINF)) {
//      val newStamp = atomicStamp.getAndIncrement()
//      val2Stamp.set(a, Constants.markValue(newStamp))
//      stamp2Val.set(newStamp, a)
//    }
//  }
//
//  // 并行使用，无并行问题
//  override def isBind(): Boolean = {
//    bindLevel != Constants.kINTMAXINF
//  }
//
//  //  // 传入的一定是正数
//  //  def getValueByPositiveStamp(s: Int): Int = {
//  //    return stamp2Val(s)
//  //  }
//
//  // 第一个返回值是变量值，
//  // 第二个返回值是动作：
//  // 0 = 未知
//  // 1 = 删值
//  // 2 = 留值
//  // 3 = 尚未写入
//  def getValueByStamp(s: Int): (Int, Int) = {
//    if (s < 0) {
//      val a = stamp2Val.get(math.abs(s))
//      return (Constants.resolveMark(a)._2, 2)
//    } else if (s > 0) {
//      val a = stamp2Val.get(s)
//      if (a == -1) {
//        return (a, 3)
//      } else {
//        return (a, 1)
//      }
//    } else {
//      return (Constants.kINTMININF, 0)
//    }
//  }
//
//  override def remove(a: Int): Unit = {
//    // 若为0，分配新值
//    //    var newStamp = 0
//    //    if (val2Stamp.compareAndSet(a, 0, {
//    //      newStamp = atomicStamp.getAndIncrement();
//    //      newStamp
//    //    })) {
//    //      stamp2Val(newStamp) = a
//    //    }
//
//    //    if (val2Stamp.compareAndSet(a, 0, Constants.kINTMAXINF)) {
//    //      val newStamp = atomicStamp.getAndIncrement()
//    //      stamp2Val(newStamp) = a
//    //    }
//
//    //    var capa = 0
//    //    var pos = 0
//    //    do {
//    //      capa = currentSize.get()
//    //      pos = sparse.get(a)
//    //      if (pos >= capa) {
//    //        return
//    //      }
//    //    } while (currentSize.compareAndSet(capa, capa + 1))
//    //    currentSize.compareAndExchange()
//    //    sparse.compareAndExchange(a)
//
//    // deleted: 是否原子删除
//    // ori: a在dense的原位置
//    // unmarkedValue: a在dense的umarked位置
//    // y: 是demark位置
//    val (deleted, ori_pos, sourcePosition) = tryDemarkIfMarked(a)
//    if (deleted) {
//      // 删的值的位置是unmarkedValue
//      // dest_pos是当前论域大小减1，也是交换的位置，
//      val destPosition = currentSize.decrementAndGet()
//      val destValue = dense.get(destPosition)
//      val swapedMarkedPosition = dense.compareAndExchange(destPosition, destValue, sourcePosition)
//      // cc是marked的要去掉
//      val swapValue = Constants.demarkValue(swapedMarkedPosition)
//
//    }
//
//
//  }
//
//  def tryDecrementIfGreaterThan(capacity: AtomicInteger, least: Int): Boolean = {
//    var capa = 0
//    do {
//      capa = capacity.get
//      if (capa <= least) return false
//    } while (
//      !capacity.compareAndSet(capa, capa - 1))
//    true
//  }
//
//  def tryDemarkIfMarked(a: Int): (Boolean, Int, Int) = {
//    var ori = 0
//    var pos = 0
//    var marked = false
//    do {
//      ori = sparse.get(a)
//      // 拿到是否mark，及解析后的pos
//      (marked, pos) = Constants.resolveMarkBoolean(ori)
//      if (pos < currentSize.get()) {
//        return (false, ori, pos)
//      }
//    } while (sparse.compareAndSet(a, ori, pos))
//    return (true, ori, pos)
//  }
//
//
//  // 原子地检查一个值是否有效，可与下一个函数合并
//  def CheckValidityByStamp(index: Int): Boolean = {
//    var capa = 0
//    do {
//      capa = val2Stamp.get(index)
//      // 若大于0则表示该值删除了
//      if (capa != 0) return false
//    } while (!val2Stamp.compareAndSet(index, capa, Constants.kINTMAXINF))
//
//    // 若为0，分配新值
//    return true
//  }
//
//  //
//  //  def IncreaseStampAndMarkValue(index: Int): Unit = {
//  //    var capa = 0
//  //    do {
//  //      capa = atomicStamp.get()
//  //      //      val2Stamp.set(index, capa)
//  //    } while (!atomicStamp.compareAndSet(capa, capa + 1))
//  //    //计算成功后再添入，不成功不添入stamp2Val默认为-1
//  //    stamp2Val(capa + 1) = index
//  //  }
//
//  //  def IncreaseStampAndMarkValue(index: Int, atomicStamps: AtomicInteger): Unit = {
//  //    var capa = 0
//  //    do {
//  //      capa = atomicStamp.get()
//  //      val2Stamp.set(index, capa)
//  //    } while (!atomicStamps.compareAndSet(capa, capa + 1))
//  //    //计算成功后再添入，不成功不添入stamp2Val默认为-1
//  //    stamp2Val(capa + 1) = index
//  //  }
//
//  override def isEmpty(): Boolean = {
//    var i = 0
//    while (i < numBit) {
//      if (bitDoms(curLevel).get(i) != 0L) {
//        return false
//      }
//      i += 1
//    }
//
//    return true
//  }
//
//  override def clearMark(): Unit = {
//    var i = 0
//    while (i < numBit) {
//      bitMark.set(i, 0L)
//      i += 1
//    }
//  }
//
//  override def mark(a: Int): Unit = {
//    val (x, y) = INDEX.getXY(a)
//    var previousBits: Long = 0L
//    var newBits: Long = 0L
//
//    do {
//      previousBits = bitMark.get(x)
//      // Clear the relevant bit
//      newBits = previousBits | Constants.MASK1(y)
//      // Try to set the new bit mask, and loop round until successful
//    } while (!bitDoms(curLevel).compareAndSet(x, previousBits, newBits))
//  }
//
//  override def fullMark(): Boolean = {
//    var i = 0
//    while (i < numBit) {
//      if (bitMark.get(i) != bitDoms(curLevel).get(i)) {
//        return false
//      }
//      i += 1
//    }
//    return true
//  }
//
//  override def contains(a: Int): Boolean = {
//    val (x, y) = INDEX.getXY(a)
//    return (bitDoms(curLevel).get(x) & Constants.MASK1(y)) != 0L
//  }
//
//  override def minValue(): Int = {
//    var i = 0
//    while (i < numBit) {
//      val a = bitDoms(curLevel).get(i)
//      if (a != 0L) {
//        return i * Constants.BITSIZE + Constants.FirstLeft(a)
//      }
//      i += 1
//    }
//    return Constants.INDEXOVERFLOW
//  }
//
//  override def nextValue(a: Int): Int = {
//    var b = a + 1
//    while (b < capacity && !contains(b)) {
//      b += 1
//    }
//
//    if (b < capacity) {
//      return b
//    }
//    else {
//      return Constants.INDEXOVERFLOW
//    }
//  }
//
//  override def lastValue(): Int = ???
//
//  override def preValue(a: Int): Int = ???
//
//  override def maxValue(a: Int): Int = ???
//
//  override def mask(mask: Array[Long]): Unit = {
//    var i = 0
//    while (i < numBit) {
//      mask(i) = bitDoms(curLevel).get(i)
//      i += 1
//    }
//  }
//
//  override def submitMask(mask: Array[Long]): Boolean = {
//    var previousBits: Long = 0L
//    var newBits: Long = 0L
//    var changed = false
//    var i = 0
//    while (i < numBit) {
//      do {
//        previousBits = bitDoms(curLevel).get(i)
//        // Clear the relevant bit
//        newBits = previousBits & mask(i)
//        // Try to set the new bit mask, and loop round until successful
//      } while (!bitDoms(curLevel).compareAndSet(i, previousBits, newBits))
//
//      if (previousBits != newBits) {
//        changed = true
//      }
//
//      i += 1
//    }
//    return changed
//  }
//
//  override def submitMaskAndIsSame(mask: Array[Long]): (Boolean, Boolean) = {
//    var previousBits: Long = 0L
//    var newBits: Long = 0L
//    var changed = false
//    var same = true
//    var i = 0
//    while (i < numBit) {
//      do {
//        previousBits = bitDoms(curLevel).get(i)
//        // Clear the relevant bit
//        newBits = previousBits & mask(i)
//        // Try to set the new bit mask, and loop round until successful
//      } while (!bitDoms(curLevel).compareAndSet(i, previousBits, newBits))
//
//      if (previousBits != newBits) {
//        changed = true
//      }
//
//      if (mask(i) != newBits) {
//        same = false
//      }
//
//      i += 1
//    }
//    return (changed, same)
//  }
//
//  override def submitMaskAndGet(mask: Array[Long]): Long = ???
//
//  override def getAndSubmitMask(mask: Array[Long]): Long = ???
//
//  override def show(): Unit = {
//    println(s"     var: ${id}, size: ${size()}")
//    var i = 0
//    while (i < numBit) {
//      //      println(s"     mask${i}: ${Constants.toFormatBinaryString(bitDoms(curLevel).get(i))}")
//      i += 1
//    }
//  }
//
//  override def get(index: Int): Int = ???
//
//  override def isChanged(mask: Array[Long]): Boolean = {
//    var ii = 0
//    while (ii < numBit) {
//      if (bitDoms(level).get(ii) != mask(ii)) {
//        return true
//      }
//      ii += 1
//    }
//    return false
//  }
//}
//
//object SafeSparseSetVar {
//  val kDEFAULTVALUE = -2
//}