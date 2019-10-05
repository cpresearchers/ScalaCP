package cpscala.TSolver.CpUtil.SparseBitSet

import cpscala.TSolver.CpUtil.Constants

import scala.collection.mutable.ArrayBuffer

class PRSBitSet(id: Int, numTuples: Int, numVars: Int) {
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

  def isEmpty(Izero: ArrayBuffer[Int]): Boolean = {
    if (limit(level) + 1 == Izero.length) return true
    var j = limit(level)
    while (words(level)(index(j)) == 0L) j -= 1
    var offset = 0
    for (i <- Izero) {
      if (i <= j) {
        offset = index(i)
        index(i) = index(j)
        index(j) = offset
        while (words(level)(index(j)) == 0L) j -= 1
      }
    }
    limit(level) -= Izero.length
    false
  }

  def clearMask(id: Int, activeParallel: Int): Unit = {
    val step = (limit(level) + 1).toFloat / activeParallel
    var i = (id * step).toInt
    val end = ((id + 1) * step).toInt
    while (i < end) {
      val offset = index(i)
      mask(offset) = 0L
      i += 1
    }
  }

  def reverseMask(id: Int, activeParallel: Int): Unit = {
    val step = (limit(level) + 1).toFloat / activeParallel
    var i = (id * step).toInt
    val end = ((id + 1) * step).toInt
    while (i < end) {
      val offset = index(i)
      mask(offset) = ~mask(offset)
      i += 1
    }
  }

  def addToMask(m: Array[Long], id: Int, activeParallel: Int): Unit = {
    val step = (limit(level) + 1).toFloat / activeParallel
    var i = (id * step).toInt
    val end = ((id + 1) * step).toInt
    while (i < end) {
      val offset = index(i)
      mask(offset) = mask(offset) | m(offset)
      i += 1
    }
  }

  def intersectWithMask(id: Int, activeParallel: Int, pIzero: ArrayBuffer[Int]): Unit = {
    var w = 0L
    var currentWords = 0L

    val step = (limit(level) + 1).toFloat / activeParallel
    var i = (id * step).toInt
    val end = ((id + 1) * step).toInt
    while (i < end) {
      val offset = index(i)
      currentWords = words(level)(offset)
      w = currentWords & mask(offset)
      if (w != currentWords) {
        words(level)(offset) = w
        if (w == 0L) {
          pIzero += i
        }
      }
      i += 1
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
