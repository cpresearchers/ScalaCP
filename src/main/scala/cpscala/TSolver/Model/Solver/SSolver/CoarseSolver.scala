package cpscala.TSolver.Model.Solver.SSolver

import cpscala.TSolver.Model.Variable.Var
import cpscala.XModel.XModel

/**
  *粗粒度求解器，适用于STR2。
  */

class CoarseSolver(xm: XModel, propagatorName: String, varType: String, heuName: String) extends Solver(xm, propagatorName, varType, heuName) {

  override def initialPropagate(): Boolean = {
    start_time = System.nanoTime
    prop_start_time = System.nanoTime
    return propagate(null)
  }

  override def checkConsistencyAfterAssignment(x: Var): Boolean = {
    return propagate(x)
  }

  override def checkConsistencyAfterRefutation(x: Var): Boolean = {
    return propagate(x)
  }

  def propagate(x: Var): Boolean = {
    Q.clear()
    if (x == null) {
      //初始化
      for (z <- vars) {
        insert(z)
      }
    } else {
      insert(x)
    }
    while (!Q.empty()) {
      val v = Q.pop()
      for (c <- subscription(v.id)) {
        if (helper.varStamp(v.id) > helper.tabStamp(c.id)) {
          //          println("str(" + c.name + ")")
          Y_evt.clear()
          val consistent = c.propagate(Y_evt)
          helper.c_sum += 1
          //          print(s"cid: ${c.id} yevt: ")
          //          Y_evt.foreach(p => print(p.id, " "))
          //          println()
          if (!consistent) {
            return false
          } else {
            for (y <- Y_evt) {
              insert(y)
            }
          }
          helper.globalStamp += 1
          helper.tabStamp(c.id) = helper.globalStamp
        }
      }
    }

    return true
  }

    def insert(x: Var): Unit = {
    Q.push(x)
    helper.globalStamp += 1
    helper.varStamp(x.id) = helper.globalStamp
  }

}
