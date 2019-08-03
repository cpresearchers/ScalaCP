package cpscala.TSolver.Model.Solver.Others

import cpscala.TSolver.CpUtil.INDEX
import org.glassfish.json.MapUtil

class LMXSparseSet(val capacity: Int, val baseLevel: Int) {
  //  val data = Array.fill[T](capacity)(new MultiLevel(-1,-1))
  val dense = Array.tabulate[MultiLevel](capacity)(i => new MultiLevel(INDEX.kOVERFLOW, INDEX.kOVERFLOW, INDEX.kOVERFLOW))
  //  val dense = Array.range(0, capacity)
  val sparse = Array.range(0, capacity * 2)
  // 当前顶层
  var top = 0

  def getIndex(a: Int) = a - baseLevel

  def add(searchLevel: Int): MultiLevel = {
    // 相对层数
    // 拿到相对层数
    // 获取tmplevel
    val tmplevel = sparse(top)
    dense(top).set(searchLevel, tmplevel + baseLevel, tmplevel)
    top += 1
    return dense(top - 1)
  }

  def remove(m: MultiLevel) = {
    val a = m.tmpLevel - baseLevel
    if (sparse(a) < top) {
      swap(sparse(a), top - 1)
      top -= 1
    }
  }

  def swap(i: Int, j: Int): Unit = {
    MultiLevel.swap(dense(i), dense(j))
    sparse(dense(i).tmpLevel - baseLevel) = i
    sparse(dense(j).tmpLevel - baseLevel) = j
  }

  def size() = top

  def isFull() = top >= capacity

  def nonFull() = top < capacity

}
