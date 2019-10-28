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

  val index_arr = tmp.keys.toArray
  val index_map = mutable.LongMap[Int]() ++ index_arr.zipWithIndex

  val words = tmp.values.toArray
  val limit = Array.fill(numLevel)(-1)
  // 获取长度
  val numBit = index_arr.length
  limit(0) = numBit - 1
  var currentLevel = 0

  def Size() = limit(currentLevel)

  def newLevel() = {
    currentLevel += 1
    limit(currentLevel) = limit(currentLevel - 1)
  }

  def backLevel(): Unit = {
    limit(currentLevel) = -1
    currentLevel -= 1
  }

  def swap(i: Int, j: Int): Unit = {
    val tmp = index_arr(i)
    index_arr(i) = index_arr(j)
    index_arr(j) = tmp

    index_map(index_arr(i)) = i
    index_map(index_arr(j)) = j
  }

  def remove(i: Int) = {
    val j = limit(currentLevel)
    index_arr(i) = index_arr(j)
    index_arr(j) = i

    index_map(index_arr(i)) = i
    index_map(index_arr(j)) = j
    limit(currentLevel) -= 1
  }

}
