package cpscala.TSolver.CpUtil

import scala.collection.immutable.IntMap
import scala.collection.{immutable, mutable}
import scala.collection.mutable.ArrayBuffer
import scala.util.Sorting

class RSIndexedBitSet(numVars: Int, indices: ArrayBuffer[Int]) {
  private[this] val numLevel = numVars + 1
  private[this] val m = new mutable.HashMap[Int, Long]()

  // 索引转成
  for (a <- indices) {
    (x, y) = INDEX.getXY(a)

    if (m.contains(x)) {
      m(x) |= Constants.MASK1(y)
    } else {
      m.put(x, Constants.MASK1(y))
    }

  }

  val k = m.keys.toArray
  Sorting.quickSort(k)
  val v = m.values.toArray
  Sorting.quickSort(v)

  val index_map: IntMap[Int] = a.zipWithIndex.to(IntMap)

  private[this] val words = Array[Array[Long]]()
  private[this] val limit = Array.fill(numLevel)(-1)
  private[this] val index = IntMap[Int]()
  private[this] var currentLevel = 0
  private[this] var isFinished = false

  private[this] val tmp_idx_dict = new mutable.HashMap[Int, Int]()
  private[this] val tmp_idx_set = mutable.SortedSet[Int]()
  private[this] val tmp_idx_arr = new mutable.ArrayBuffer[Int]()


  def add(idx: Int): Unit = {
    //    if (!isFinished) {
    //      tmp_idx_arr += idx
    //    } else {
    //      println("add after finish")
    //    }

    tmp_idx_arr += idx
  }

  def finish(): Unit = {
    //    tmp_idx_arr.sortBy()sortBy

  }


}
