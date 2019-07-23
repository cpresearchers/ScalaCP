package cpscala.TSolver.Model.Solver.Others

class MultiLevel(var searchLevel: Int, var tmpLevel: Int) {
  def set(m: MultiLevel) = {
    searchLevel = m.searchLevel
    tmpLevel = m.tmpLevel
  }

  def set(sl: Int, tl: Int) = {
    searchLevel = sl
    tmpLevel = tl
  }

//  def ==(m:MultiLevel): Unit ={
//    if(m.searchLevel == m.)
//  }
}

object MultiLevel {
  def swap(ml0: MultiLevel, ml1: MultiLevel) = {
    val a = ml0.searchLevel
    val b = ml0.tmpLevel
    ml0.set(ml1)
    ml1.set(a,b)
//    ml0.searchLevel = ml1.searchLevel
//    ml0.tmpLevel = ml1.tmpLevel
//    ml1.searchLevel = a
//    ml1.tmpLevel = b
  }
}


