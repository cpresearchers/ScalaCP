package cpscala.TSolver.CpUtil

import scala.collection.mutable.ArrayBuffer

class PWRSparseBitSet(id: Int, numTuples: Int, numVars: Int) extends PWRSBitSet(id, numTuples, numVars) {

  val offsetCon = new ArrayBuffer[Int]() //获取offset
  val saveWord =  Array.fill(num_bit)(0L)
  var savelimit=0
  if (lastLimits != 0){
    saveWord(num_bit - 1) <<= 64 - lastLimits
  }

  def initIntersection(sets: ArrayBuffer[PWRSparseBitSet]): Unit = {
    clearWord()
    limit(0) = -1
    var i = 0
    while (i <= sets(0).limit(0)) {
      val offset = sets(0).index(i)
      var bits = sets(0).words(0)(offset)
      if (bits != 0) {
        for (set <- sets) {
          bits = bits & set.words(0)(offset)
        }
      }
      if (bits != 0) {
        words(0)(offset) = bits
        limit(0) += 1
        index(limit(0)) = offset
      }
      i += 1
    }
  }

  def clearWord():Unit={
    for(i<-0 until num_bit){
      words(0)(i)=0L
    }
  }

  def intersectIndex(block: Block): Int = {
    block.commonIndices.getIndeces(offsetCon)
    for (offset <- offsetCon) {
      var intersection = words(currentLevel)(offset);
      if (intersection != 0) {
        for (set <- block.sets) {
          intersection = intersection & set.words(0)(offset)
        }
      }
      if (intersection != 0)
        return offset
    }
    return -1
  }

  def intersectIndex(bits: PWRSparseBitSet): Int = {
    val currentLimit = limit(currentLevel)
    var i = 0
    while (i <= currentLimit) {
      val offset = index(i)
      if ((words(currentLevel)(offset) & bits.words(0)(offset)) != 0L) return offset
      i += 1
    }
    return -1
  }

  def removeBlock(block: Block): Boolean = {

    var change = false
    var i = limit(currentLevel)
    block.commonIndices.getIndeces(offsetCon)
    while (i >= 0) {
      val offset = index(i)
      if (offsetCon.contains(offset)) {
        var b = Constants.ALLONELONG
        for (set <- block.sets) {
          if (b != 0) {
            b = b & set.words(0)(offset)
          }
        }
        val bit = words(currentLevel)(offset)
        val w = words(currentLevel)(offset) & ~b
        if (bit != w) {
          words(currentLevel)(offset)=w
          Delta(offset)=bit & ~w
          change = true
        }
        if (words(currentLevel)(offset) == 0) {
          val j = limit(currentLevel)
          index(i) = index(j)
          index(j) = offset
          limit(currentLevel) -= 1
        }
      }
      i -= 1
    }
    return change
  }

  def addToMask(m: PWRSparseBitSet): Unit = { //重写addtoMask
    val currentLimit = limit(currentLevel)
    var i = 0
    while (i <= currentLimit) {
      val offset = index(i)
      mask(offset) = mask(offset) | m.words(0)(offset)
      i += 1
    }
  }


  def addBlockToMask(block: Block) = {

    block.commonIndices.getIndeces(offsetCon)
    for (offset <- offsetCon) {

        var b = block.sets(0).words(0)(offset)
        for (set <- block.sets) {
          if (b != 0)
            b = b & set.words(0)(offset)
        }
        mask(offset) = mask(offset) | b
    }
  }

  def computeDelta(tusToCheck: PWRSparseBitSet) = {
//    offsetCon.clear()
//    println(DenumSet())
    var i=0
    while (i < num_bit) {
      val offset = index(i)
      tusToCheck.words(0)(offset) = Delta(offset)
      i += 1
//      Constants.addValues(Delta(offset), offset * Constants.BITSIZE, offsetCon)
    }
//    for(i<-offsetCon)
//      print(i+" num  ")
//    println(tusToCheck.numSet())
  }

  def DenumSet(): Int = {
    var num = 0
    var i = 0
    while (i < num_bit) {
      val offset = index(i)
      num += java.lang.Long.bitCount(Delta(offset))
      i += 1
    }
    return num
  }

  def clearDelta() = {
    for (i <- 0 until num_bit) {
      val offset = index(i)
      Delta(offset) = 0L
    }
  }

  def getIndeces(offsetCon: ArrayBuffer[Int]): Unit = {
    offsetCon.clear()
    /*
    * for 0 to limit
    * indeces()
    * */
    var i = 0
    while (i < num_bit) {
      val offset = index(i)
      Constants.addValues(words(currentLevel)(offset), offset * Constants.BITSIZE, offsetCon)
      i += 1
    }
  }

  def numSet(): Int = {
    var num = 0
    var i = 0
    while (i < num_bit) {
      val offset = index(i)
      num += java.lang.Long.bitCount(words(currentLevel)(offset))
      i += 1
    }
    return num
  }

  def restore() = {
    limit(currentLevel)=savelimit
    var i=0
    while (i < num_bit) {
      val offset = index(i)
       words(currentLevel)(offset) = saveWord(offset)
      i += 1
    }
  }

  def save() = {
    savelimit=limit(currentLevel)
    var i=0
    while (i < num_bit) {
      val offset = index(i)
      saveWord(offset) = words(currentLevel)(offset)
      i += 1
    }
  }

  def toCheck(tocheck:PWRSBitSet):Unit={
    for(i<-0 until num_bit){
      val offset = index(i)
      tocheck.words(0)(offset)=words(currentLevel)(offset)
    }
  }
}
