package cpscala.TSolver.CpUtil

import scala.collection.immutable.IntMap
import scala.collection.{immutable, mutable}
import scala.collection.mutable.ArrayBuffer

// 用于support
class RSIndexedBitSet(numVars: Int, indices: ArrayBuffer[Int]) {
  private[this] val numLevel = numVars + 1

  // 临时存储变量
  private[this] val tmp = new mutable.TreeMap[Int, Long]()

  // 索引转成sparsebitset
  for (a <- indices) {
    val (x, y) = INDEX.getXY(a)

    if (tmp.contains(x)) {
      tmp(x) |= Constants.MASK1(y)
    } else {
      tmp.put(x, Constants.MASK1(y))
    }
  }
  // 清楚临时变量
  tmp.clear()

  private[this] val index_arr: Array[Int] = tmp.keys.toArray
  private[this] val index_map = IntMap[Int]() ++ index_arr.zipWithIndex
  private[this] val words = tmp.values.toArray
  val limit = Array.fill(numLevel)(-1)
  // 获取长度
  val numBit = index_arr.length
  limit(0) = numBit - 1
  var currentLevel = 0

  def Size() = limit(currentLevel)

  def newLevel() = {
    currentLevel += 1
    levelLimits(currentLevel) = levelLimits(currentLevel - 1)
  }

  def backLevel(): Unit = {
    levelLimits(currentLevel) = -1
    currentLevel -= 1
  }

}
