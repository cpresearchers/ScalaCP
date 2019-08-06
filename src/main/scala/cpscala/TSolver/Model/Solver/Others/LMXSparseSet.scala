package cpscala.TSolver.Model.Solver.Others

import cpscala.TSolver.CpUtil.INDEX
import org.glassfish.json.MapUtil

//class LMXSparseSet(val capacity: Int, val baseLevel: Int) {
//  //  val data = Array.fill[T](capacity)(new MultiLevel(-1,-1))
//  val dense = Array.tabulate[MultiLevel](capacity)(i => new MultiLevel(INDEX.kOVERFLOW, INDEX.kOVERFLOW, INDEX.kOVERFLOW))
//  //  val dense = Array.range(0, capacity)
//  val sparse = Array.range(0, capacity)
//  // 当前顶层
//  var top = 0
//
//  def getIndex(a: Int) = a - baseLevel
//
//  def add(searchLevel: Int): MultiLevel = {
//    // 相对层数
//    // 拿到相对层数
//    // 获取tmplevel
//    val tmplevel = sparse(top)
//    dense(top).set(searchLevel, tmplevel + baseLevel, tmplevel)
//    top += 1
//    return dense(top - 1)
//  }
//
//  def remove(m: MultiLevel) = {
//    val a = m.tmpLevel - baseLevel
//    if (sparse(a) < top) {
//      swap(sparse(a), top - 1)
//      top -= 1
//    }
//  }
//
//  def swap(i: Int, j: Int): Unit = {
//    MultiLevel.swap(dense(i), dense(j))
//    sparse(dense(i).tmpLevel - baseLevel) = i
//    sparse(dense(j).tmpLevel - baseLevel) = j
//  }
//
//  def size() = top
//
//  def isFull() = top >= capacity
//
//  def nonFull() = top < capacity
//
//}

class LMXSparseSet(val capacity: Int, val baseLevel: Int) {
  // 临时层位置
  val tmpLevels = Array.range(baseLevel, baseLevel + capacity)
//  println(tmpLevels.mkString(","))
  // 对应的搜索层，初始为-1
  val searchLevels = Array.fill(capacity)(INDEX.kOVERFLOW)
  //  val tmpMultiLevels = Array.fill(capacity)(new MultiLevel(INDEX.kOVERFLOW, INDEX.kOVERFLOW, INDEX.kOVERFLOW))

  var sizeL = 0

  def add(searchLevel: Int): MultiLevel = {
    if (nonFull()) {
      var ii = 0
      while (ii < capacity) {
        if (searchLevels(ii) == INDEX.kOVERFLOW) {
          sizeL += 1
          searchLevels(ii) = searchLevel
          val m =new MultiLevel(searchLevel, tmpLevels(ii), sizeL - 1)
//          println("return: "+m.toString())
          return m
        }
        ii += 1
      }
    }
    else {
      println("set full!!")
    }

    return null
  }

  def getIndex(tmp: Int) = tmp - baseLevel

  def remove(m: MultiLevel) = {
    if (nonEmpty()) {
      val idx = m.tmpLevel - baseLevel
      searchLevels(idx) = INDEX.kOVERFLOW
      sizeL -= 1
    }else{
      println("set empty!!")
    }
  }

  def size() = sizeL

  def nonFull() = sizeL < capacity

  def isFull() = sizeL == capacity

  def isEmpty() = sizeL == 0

  def nonEmpty() = sizeL != 0
}