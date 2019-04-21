package cpscala.TSolver.Model.Variable

import java.util.concurrent.atomic.AtomicIntegerArray

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil.{Constants, SearchHelper}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class SparseSetVar(val name: String, val id: Int, num_vars: Int, vals: Array[Int], val helper: SearchHelper) extends PVar {
  override val capacity = vals.length
  val numLevel: Int = num_vars + 1
  val dense: Array[Int] = vals.clone()
  val sparse: Array[Int] = vals.clone()
  val sizeLevel: Array[Int] = Array.fill(numLevel)(-1)
  sizeLevel(0) = vals.length
  // mark属性及其相关函数是代替gacValue的机制
  var mark = 0
  //并行str2需要用到counter计数
  val counter = new AtomicIntegerArray(vals.length)
  val initialPos: mutable.HashMap[Int, Int] = mutable.HashMap[Int, Int]()

  var i = 0
  while (i < vals.length) {
    initialPos.put(vals(i), i)
    i += 1
  }

  override def get(index: Int): Int = dense(index)

  override def clearMark(): Unit = {
    mark = 0
  }

  override def mark(a: Int): Unit = {
    if (sparse(a) < sizeLevel(level) && sparse(a) >= mark) {
      swap(sparse(a), mark)
      mark += 1
    }
  }

  override def fullMark(): Boolean = {
    mark == sizeLevel(level)
  }

  override def restrict(): Unit = {
    for (i <- mark until sizeLevel(level)) {
      //println(s"       var:${id} remove values:${dense(i)}")
    }
    sizeLevel(level) = mark
  }

  override def newLevel(): Int = {
    // level在Solver中已经先加了，所以level是空白层
    level += 1
    sizeLevel(level) = sizeLevel(level - 1)
    return level
  }

  override def backLevel(): Int = {
    // level在Solver中已经先减了，所以level是旧层
    sizeLevel(level) = -1
    if (bindLevel == level) {
      bindLevel = Constants.kINTINF
    }

    level -= 1
    return level
  }

  //
  @inline override def size(): Int = sizeLevel(level)

  @inline override def contains(a: Int): Boolean = sparse(a) < sizeLevel(level)

  def safeContains(a: Int): Boolean = this.synchronized {
    sparse(a) < sizeLevel(level)
  }

  @inline def swap(i: Int, j: Int): Unit = {
    val tmp = dense(i)
    dense(i) = dense(j)
    dense(j) = tmp
    sparse(dense(i)) = i
    sparse(dense(j)) = j
  }

  override def bind(a: Int): Unit = {
    bindLevel = level
    if (sparse(a) >= sizeLevel(level)) {
      sizeLevel(level) = 0
    }
    else {
      swap(sparse(a), 0)
      sizeLevel(level) = 1
    }
  }

  override def remove(a: Int): Unit = {
    if (sparse(a) < sizeLevel(level)) {
      swap(sparse(a), sizeLevel(level) - 1)
      sizeLevel(level) -= 1
    }
  }

  override def safeRemove(a: Int): Unit = this.synchronized {
    if (sparse(a) < sizeLevel(level)) {
      //      //println("tid:" + Thread.currentThread.getId() + ", cid: " + name + ", delete: " + name + "," + a + ", level: " + level)
      swap(sparse(a), sizeLevel(level) - 1)
      sizeLevel(level) -= 1
    }
  }

  override def show(): Unit = {
    //println("name: " + id + " name: " + name + " size: " + size + " numVars: " + numVars)
    //println(sparse.mkString(","))
    //    //println(sizeLevel.mkString(","))
  }

  override def minValue(): Int = {
    var a = 0
    while (a < capacity) {
      if (contains(a)) {
        return a
      }
      a += 1
    }
    Constants.INDEXOVERFLOW
  }

  def safeMinValue: Int = this.synchronized {
    var a = 0
    while (a < capacity) {
      if (contains(a)) {
        return a
      }
      a += 1
    }
    Constants.INDEXOVERFLOW
  }

  override def isBind(): Boolean = ???

  override def isEmpty(): Boolean = {
    sizeLevel(level) == 0
  }

  // !!待改进
  override def nextValue(a: Int): Int = {
    var b = a + 1
    while (b < capacity) {
      if (contains(b)) {
        return b
      }
      b += 1
    }
    Constants.INDEXOVERFLOW
  }

  override def lastValue(): Int = {
    dense(sizeLevel(level))
  }

  override def preValue(a: Int): Int = ???

  override def maxValue(a: Int): Int = ???

  override def getLastRemovedValues(oldSize: Long, vals: ArrayBuffer[Int]): Int = {
    vals.clear()
    val tempSize = size()
    var a = tempSize
    while (a < oldSize) {
      vals += dense(a)
      a += 1
    }
   tempSize
    //    //println("id: " + id + ", lastRemoveValues: " + lastRemovedValues.mkString(","))
  }

  override def getValidValues(vals: ArrayBuffer[Int]): Int = {
    vals.clear()
    val tempSize = size()
    var a = 0
    while (a < tempSize) {
      vals += dense(a)
      a += 1
    }
    tempSize
    //    //println("id: " + id + ", validValues: " + validValues.mkString(","))
  }

  override def isChanged(mask: Array[Long]): Boolean = ???
}
