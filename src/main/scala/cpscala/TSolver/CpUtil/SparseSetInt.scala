package cpscala.TSolver.CpUtil

import scala.collection.mutable.ArrayBuffer

class SparseSetInt(val capacity: Int, val num_level: Int) {
  val dense = Array.range(0, capacity)
  val sparse = Array.range(0, capacity)
  val levelSize: Array[Int] = Array.fill(num_level)(-1)
  var level: Int = 0
  var cur_size: Int = capacity - 1

  def clear(): Unit = {
    cur_size = -1
  }

  def empty(): Boolean = {
    cur_size == -1
  }

  def fill(): Unit = {
    cur_size = capacity - 1
  }

  //
  def add(a: Int): Unit = {
    if (!has(a)) {
      swap(cur_size + 1, sparse(a))
      cur_size += 1
    }
  }

  def get(i: Int): Int = {
    dense(i)
  }

  def size(): Int = {
    return cur_size + 1
  }

  def has(a: Int): Boolean = {
    sparse(a) <= cur_size
  }

  def remove(a: Int): Unit = {
    if (has(a)) {
      swap(sparse(a), cur_size)
      cur_size -= 1
    }
  }

  def swap(i: Int, j: Int): Unit = {
    val tmp = dense(i)
    dense(i) = dense(j)
    dense(j) = tmp
    sparse(dense(i)) = i
    sparse(dense(j)) = j
  }

  def newLevel(): Int = {
    levelSize(level) = cur_size
    level += 1
    cur_size
  }

  def backLevel(): Int = {
    level -= 1
    cur_size = levelSize(level)
    cur_size
  }

}
