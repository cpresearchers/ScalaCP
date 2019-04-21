package cpscala.TSolver.CpUtil

import scala.collection.mutable._

class RSBitMap(numOriVars: Int) {
  val numLevel = numOriVars + 1
  //  var numBit = 0
  //  val lastLimits = numTuples % Constants.BITSIZE
  val map = new HashMap[Int, Long]()
  val words = new ArrayBuffer[Long]
  //初始化limit, index, mask
  val limit = Array.fill(numLevel)(-1)
  // array of int,  index.length = p
  //  val index = Array.range(0, numBit)
  // array of long, mask.length = p
  var currentLevel = 0
  var preLevel = 0

  def set(index: Int): Unit = {
    val idx2 = INDEX.getIndex2D(index)
    //通过index 拿到index2 判断key
    //若key存在，则该处为置为1
    //若key不存在，则新建key并置位置为1，limit + 1
    if (map.contains(idx2.x)) {
      map(idx2.x) |= Constants.MASK1(idx2.y)
    } else {
      map += (idx2.x -> Constants.MASK1(idx2.y))
      limit(0) += 1
    }
  }

  def newLevel(level: Int): Unit = {
    if (currentLevel != level) {
      currentLevel = level
      limit(currentLevel) = limit(preLevel)
      preLevel = level
    }
  }

  def deleteLevel(level: Int): Unit = {
    limit(level) = -1
    preLevel = level - 1

    while (limit(preLevel) == -1) preLevel -= 1
    currentLevel = preLevel
  }

  def BackToLevel(level: Int): Unit = {
    limit(level) = -1
    preLevel = level - 1;
    while (limit(preLevel) == -1) preLevel -= 1
    currentLevel = preLevel
  }

  def isEmpty: Boolean = limit(currentLevel) == -1

  //
  //  def intersectWithMask(): Boolean = {
  //    //本表默认未修改
  //    var changed = false
  //    var w = 0L
  //    var currentWords = 0L
  //
  //    var i = limit(currentLevel)
  //    while (i >= 0) {
  //      val offset = index(i)
  //      currentWords = words(currentLevel)(offset)
  //      w = currentWords & mask(offset)
  //      if (w != currentWords) {
  //        words(currentLevel)(offset) = w
  //        //本表已修改
  //        changed = true
  //
  //        if (w == 0L) {
  //          val j = limit(currentLevel)
  //          index(i) = index(j)
  //          index(j) = offset
  //          limit(currentLevel) -= 1
  //
  //          map(index(i)) = i
  //          map(index(j)) = j
  //        }
  //      }
  //      i -= 1
  //    }
  //    //记录是否改变
  //    return changed
  //  }
  //
  //  def intersectIndex(m: Array[Long]): Int = {
  //    val currentLimit = limit(currentLevel)
  //    var i = 0
  //    while (i <= currentLimit) {
  //      val offset = index(i)
  //      if ((words(currentLevel)(offset) & m(offset)) != 0L) return offset
  //      i += 1
  //    }
  //    return -1
  //  }
  //
  def show(): Unit = {
    print("level = " + currentLevel + " ")
    for (k <- map) {
      printf("%d: %x ", k._1, k._2)
    }
    println()
  }
}

