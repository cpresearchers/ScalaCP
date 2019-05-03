package cpscala.TSolver.Model.Solver.IPbitSolver

import cpscala.TSolver.Model.Variable.PVar
import cpscala.XModel.XModel

class IPtmpCoarseSolver(xm: XModel, parallelism: Int, propagatorName: String, varType: String, heuName: String) extends IPbitSolver(xm, parallelism, propagatorName, varType, heuName) {

  def initialPropagate(): Boolean = {

    start_time = System.nanoTime
    prop_start_time = System.nanoTime

    helper.isConsistent = true
    Yevt.clear()
    Yevt ++= vars

    while (Yevt.nonEmpty) {

      ClearInCevt()
      for (v <- Yevt) {
        for (c <- subscription(v.id)) {
          if (!inCevt(c.id)) {
            inCevt(c.id) = true
            helper.c_sum += 1
            helper.submitToPool(c)
            //            println(s"${c.id} submit-----")
          }
        }
      }
      helper.poolAwait()
      helper.p_sum += 1

      // 论域改动的变量stamp = gstamp+1
      if (!helper.isConsistent) {
        return false
      }

      helper.globalStamp += 1
      Yevt.clear()

      var i = helper.level
      while (i < numVars) {
        val vid = levelvdense(i)
        val v = vars(vid)
        //若上轮改过了
        if (helper.varStamp(vid) == helper.globalStamp) {
          Yevt += v
        }
        i += 1
      }
    }
    return true
  }

  def checkConsistencyAfterAssignment(ix: PVar): Boolean = {

    helper.isConsistent = true
    Yevt.clear()
    Yevt += ix

    while (Yevt.size != 0) {

      ClearInCevt()
      for (v <- Yevt) {
        for (c <- subscription(v.id)) {
          if (!inCevt(c.id)) {
            inCevt(c.id) = true
            helper.c_sum += 1
            helper.submitToPool(c)
          }
        }
      }
      helper.poolAwait()

      // 论域改动的变量stamp = gstamp+1
      helper.p_sum += 1
      if (!helper.isConsistent) {
        return false
      }

      helper.globalStamp += 1
      Yevt.clear()

      var i = helper.level
      while (i < numVars) {
        val vid = levelvdense(i)
        val v = vars(vid)
        //若上轮改过了
        if (helper.varStamp(vid) == helper.globalStamp) {
          Yevt += v
        }
        i += 1
      }
    }
    return true
  }

  def checkConsistencyAfterRefutation(ix: PVar): Boolean = {

    helper.isConsistent = true
    Yevt.clear()
    Yevt += ix

    while (Yevt.size != 0) {
      ClearInCevt()
      for (v <- Yevt) {
        for (c <- subscription(v.id)) {
          if (!inCevt(c.id)) {
            inCevt(c.id) = true
            helper.c_sum += 1
            helper.submitToPool(c)
          }
        }
      }
      helper.poolAwait()

      // 论域改动的变量stamp = gstamp+1
      helper.p_sum += 1
      if (!helper.isConsistent) {
        return false
      }

      helper.globalStamp += 1
      Yevt.clear()

      var i = helper.level
      while (i < numVars) {
        val vid = levelvdense(i)
        val v = vars(vid)
        //若上轮改过了
        if (helper.varStamp(vid) == helper.globalStamp) {
          Yevt += v
        }
        i += 1
      }
    }
    return true
  }

}
