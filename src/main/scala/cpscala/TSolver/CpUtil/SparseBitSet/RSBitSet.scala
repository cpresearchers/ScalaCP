package cpscala.TSolver.CpUtil.SparseBitSet

import cpscala.TSolver.CpUtil.Constants

class RSBitSet(id: Int, numTuples: Int, numVars: Int) {
  val numLevel = numVars + 1
  val numBit = Math.ceil(numTuples.toDouble / Constants.BITSIZE.asInstanceOf[Double]).toInt
  val lastLimits = numTuples % Constants.BITSIZE
  // array of rlong, words.length = p
  // lastLimit 取值为[0, 63]
  // 若lastLimit = 0, lastWord不改变
  // otherwise, lastWord <<= 64 - lastLimit
  // array of int,  index.length = p
  // array of rlong, words.length = p
  val words = Array.ofDim[Long](numLevel, numBit)
  words(0) = Array.fill(numBit)(Constants.ALLONELONG);
  if (lastLimits != 0) words(0)(numBit - 1) <<= 64 - lastLimits
  //初始化limit, index, mask
  // rint
  val limit = Array.fill(numLevel)(-1)
  limit(0) = numBit - 1
  // array of int,  index.length = p
  val index = Array.range(0, numBit)
  // array of long, mask.length = p
  val mask = new Array[Long](numBit)
  var level = 0

  def newLevel(): Unit = {
    val preLevel = level
    level += 1
    limit(level) = limit(preLevel)

    var i = limit(preLevel)
    while (i >= 0) {
      val offset = index(i)
      words(level)(offset) = words(preLevel)(offset)
      i -= 1
    }
  }

  def backLevel(): Unit = {
    limit(level) = -1
    level -= 1
  }

  def isEmpty(): Boolean = limit(level) == -1

  def clearMask(): Unit = {
    val currentLimit = limit(level)
    var i = 0
    while (i <= currentLimit) {
      val offset = index(i)
      mask(offset) = 0L
      i += 1
    }
  }

  def reverseMask(): Unit = {
    val currentLimit = limit(level)
    var i = 0
    while (i <= currentLimit) {
      val offset = index(i)
      mask(offset) = ~mask(offset)
      i += 1
    }
  }

  def addToMask(m: Array[Long]): Unit = {
    val currentLimit = limit(level)
    var i = 0
    while (i <= currentLimit) {
      val offset = index(i)
      mask(offset) = mask(offset) | m(offset)
      i += 1
    }
  }

  def intersectWithMask(): Unit = {
    var w = 0L
    var currentWords = 0L

    var i = limit(level)
    while (i >= 0) {
      val offset = index(i)
      currentWords = words(level)(offset)
      w = currentWords & mask(offset)
      if (w != currentWords) {
        words(level)(offset) = w
        if (w == 0L) {
          val j = limit(level)
          index(i) = index(j)
          index(j) = offset
          limit(level) -= 1
        }
      }
      i -= 1
    }
  }

  def intersectIndex(m: Array[Long]): Int = {
    val currentLimit = limit(level)
    var i = 0
    while (i <= currentLimit) {
      val offset = index(i)
      if ((words(level)(offset) & m(offset)) != 0L) return offset
      i += 1
    }
    return -1
  }

  def show(): Unit = {
    //    print(" name = " + id + ", level = " + level + " ")
    for (i <- 0 until numBit) {
      println(s"   index: ${i}   word : ${Constants.toFormatBinaryString(words(level)(i))}")
    }

    for (l <- 0 until limit(level)) {
      println(s"   ${l} non-zero index: ${index(l)}")
    }
  }
}
