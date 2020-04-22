package cpscala.TSolver.CpUtil

import java.util.concurrent.atomic.{AtomicIntegerArray, AtomicLongArray}

import cpscala.TSolver.Model.Constraint.SConstraint.BitSupport

import scala.collection.mutable.ArrayBuffer

class FDEBitSet(id: Int, numTuples: Int, numVars: Int) extends RSBitSet(id, numTuples, numVars) {

  val curWord = Array.fill(numBit)(0L)
  var in: Set[Int] = Set()

  if (lastLimits != 0) {
    curWord(numBit - 1) <<= 64 - lastLimits
  }

  def intersectWord(dom: Array[Long]): Boolean = {
    var changed = false
    var w = 0L
    var currentWords = 0L
    var i = limit(currentLevel)
    while (i >= 0) {
      val offset = index(i)
      currentWords = words(currentLevel)(offset)
      w = currentWords & dom(offset)
      if (w != currentWords) {
        words(currentLevel)(offset) = w
        //本表已修改
        changed = true
        if (w == 0L) {
          val j = limit(currentLevel)
          index(i) = index(j)
          index(j) = offset
          limit(currentLevel) -= 1
        }
      }
      i -= 1
    }
    //记录是否改变
    return changed
  }

  def intersectWord(dom: AtomicLongArray): Boolean = {
    var changed = false
    var w = 0L
    var currentWords = 0L
    var i = limit(currentLevel)
    while (i >= 0) {
      val offset = index(i)
      currentWords = words(currentLevel)(offset)
      w = currentWords & dom.get(offset)
      if (w != currentWords) {
        words(currentLevel)(offset) = w
        //本表已修改
        changed = true
        if (w == 0L) {
          val j = limit(currentLevel)
          index(i) = index(j)
          index(j) = offset
          limit(currentLevel) -= 1
        }
      }
      i -= 1
    }
    //记录是否改变
    return changed
  }

  def getWord(): Array[Long] = {
    clearcurWord()
    //本表默认未修改
    var w = 0L
    var i = limit(currentLevel)
    while (i >= 0) {
      val offset = index(i)
      curWord(offset) = words(currentLevel)(offset)
      i -= 1
    }
    //记录是否改变
    return curWord
  }

  def clearcurWord() = {
    for (i <- 0 until numBit) {
      val offset = index(i)
      curWord(offset) = 0L
    }
  }

  def intersectIndex(local: ArrayBuffer[Int], m: ArrayBuffer[Long]): Int = {
    val curlimit = limit(currentLevel)
    var i = 0
    while (i <= curlimit) {
      val offset = index(i)
      if (local.contains(offset))
        if ((words(currentLevel)(offset) & m(local.indexOf(offset))) != 0L) return offset
      i += 1
    }
    return -1
  }

  def addToMask(local: ArrayBuffer[Int], m: ArrayBuffer[Long]): Unit = {
    var i = 0
    for (offset <- local) {
      if (words(currentLevel)(offset) != 0)
        mask(offset) = mask(offset) | m(i)
      i += 1
    }
  }

  def getIndex(): Unit = {
    val curlimit = limit(currentLevel)
    var i = 0
    while (i <= curlimit) {
      in += index(i)
      i += 1
    }
  }

  def clearIndex() = {
    in = Set()
  }


  def intersectIndex(m: Array[BitSupport]): Int = {
    //    val curlimit=limit(currentLevel)
    //    var i = 0
    //    while (i <= curlimit) {
    //      var find=true
    //      val offset=index(i)
    ////      val support=m.find({x:BitSupport => x.ts==offset})
    //      var j=0
    //      while (j < m.length && find) {
    //        if (m(j).ts == offset){
    //          find=false
    //          if ((words(currentLevel)(offset) & m(j).mask) != 0L) return offset
    //        }
    //        j+=1
    //      }
    //      i+=1
    //    }
    var i = 0
    while (i < m.length) {
      if ((words(currentLevel)(m(i).ts) & m(i).mask) != 0L)
        if (in.contains(m(i).ts))return i
      i += 1
    }
    return -1
  }

  def addToMask(m: Array[BitSupport]): Unit = {
    for (support <- m) {
      if (words(currentLevel)(support.ts) != 0)
        mask(support.ts) = mask(support.ts) | support.mask
    }
  }

}
