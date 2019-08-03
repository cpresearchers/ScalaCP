package cpscala.TSolver.Model.Solver.Others

class MultiLevel(var searchLevel: Int, var tmpLevel: Int, var tIdx: Int) {
  def set(m: MultiLevel) = {
    searchLevel = m.searchLevel
    tmpLevel = m.tmpLevel
    tIdx = m.tIdx
  }

  def set(sl: Int, tl: Int, ti: Int) = {
    searchLevel = sl
    tmpLevel = tl
    tIdx = ti
  }

  override def toString(): String = {
    return searchLevel + ", " + tmpLevel + ", " + tIdx
  }

}

object MultiLevel {
  def swap(ml0: MultiLevel, ml1: MultiLevel) = {
    val a = ml0.searchLevel
    val b = ml0.tmpLevel
    val c = ml0.tIdx
    ml0.set(ml1)
    ml1.set(a, b, c)
  }
}


