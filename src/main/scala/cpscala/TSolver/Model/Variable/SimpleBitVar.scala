package cpscala.TSolver.Model.Variable

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil._

import scala.collection.mutable.ArrayBuffer

class SimpleBitVar(val name: String, val id: Int, num_vars: Int, vals: Array[Int], val helper: SearchHelper) extends Var {
  override val capacity = vals.length
  // 如果论域大于64则返回false

  val limit = capacity
  //  var level_size(helper.level): Int = capacity
  var last_size: Int = capacity
  // 总层数
  val num_level = num_vars + 3

  //只用于记录size的历史信息
  val level_size = Array.fill(num_level)(-1)
  level_size(0) = capacity

  var bit_tmp = Constants.ALLONELONG
  bit_tmp <<= (Constants.BITSIZE - limit)
  var bit_mark: Long = 0L
  var mark_size: Int = 0
  val bit_doms = new Array[Long](num_level)

  // 初始化第0级的bitDom
  bit_doms(0) = bit_tmp

  var ii = 0

  override def newLevel(): Int = {
    bit_doms(helper.level + 1) = bit_doms(helper.level)
    level_size(helper.level + 1) = level_size(helper.level)
    return helper.level
  }

  override def backLevel(): Int = {
    // 当前level_size置-1
    // 若变量在当前层赋值，则撤销赋值
    level_size(helper.level) = level_size(helper.level)
    if (bindLevel == helper.level) {
      bindLevel = Constants.kINTINF
    }
    return helper.level
  }

  //提交改动
  override def restrict(): Unit = {
    level_size(helper.level) = mark_size
    bit_doms(helper.level) = bit_mark
  }

  override def size(): Int = level_size(helper.level)

  override def bind(a: Int): Unit = {
    //    bitDoms(helper.level) = 0L
    //    bitDoms(helper.level) |= Constants.MASK1(a)
    bit_doms(helper.level) = Constants.MASK1(a)
    level_size(helper.level) = 1
    bindLevel = helper.level
  }

  override def isBind(): Boolean = {
    bindLevel != -1
  }

  override def remove(a: Int): Unit = {
    bit_doms(helper.level) &= Constants.MASK0(a)
    level_size(helper.level) -= 1
  }

  override def isEmpty(): Boolean = {
    level_size(helper.level) == 0
  }

  override def clearMark(): Unit = {
    bit_mark = 0L
    mark_size = 0
  }

  override def mark(a: Int): Unit = {
    // mark中没有并且没有被删掉才加入mark
    if ((bit_mark & Constants.MASK1(a) & bit_doms(helper.level)) == 0L) {
      bit_mark |= Constants.MASK1(a)
      mark_size += 1
    }
  }

  override def fullMark(): Boolean = {
    mark_size == level_size(helper.level)
  }

  override def contains(a: Int): Boolean = {
    if (a == Constants.INDEXOVERFLOW) {
      return false
    }
    return (bit_doms(helper.level) & Constants.MASK1(a)) != 0L
  }

  override def minValue(): Int = {
    if (bit_doms(helper.level) != 0L) {
      return Constants.FirstLeft(bit_doms(helper.level))
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

  override def lastValue(): Int = ???

  override def preValue(a: Int): Int = ???

  override def maxValue(a: Int): Int = ???

//  override def getLastRemovedValues(last: Long): Unit = ???
//
//  override def getValidValues(): Unit = {}
  override def get(index: Int): Int = ???

  override def mask(m: Array[Long]): Unit = ???
}
