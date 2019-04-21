package cpscala.TSolver.Model.Solver

import cpscala.TSolver.Model.Variable.PVar
import cpscala.XModel.XModel

class IPCoarseSolver(xm: XModel, parallelism: Int, propagator_name: String, var_type: String, heu_name: String) extends IPSolver(xm, parallelism, propagator_name, var_type, heu_name){

  def initialPropagate(): Boolean = {

    start_time = System.nanoTime
    prop_start_time = System.nanoTime

    helper.isConsistent = true
    Yevt.clear()
    Yevt ++= vars

    var otherStartTime = System.nanoTime()
    var otherEndTime = 0L

    while (Yevt.size != 0) {

      Cevt.clear()
      ClearInCevt()

      for (v <- Yevt) {
        for (c <- subscription(v.id)) {
          if (!inCevt(c.id)) {
            Cevt.add(c)
            inCevt(c.id) = true
          }
        }
      }

      otherEndTime = System.nanoTime()
      helper.lockTime += otherEndTime - otherStartTime

      helper.searchState = 2 //"propagate"
      // 论域改动的变量stamp = gstamp+1
      helper.pool.invokeAll(Cevt)
      helper.c_sum += Cevt.size()
      helper.p_sum += 1
      if (!helper.isConsistent) {
        return false
      }

      otherStartTime = System.nanoTime()

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

    var otherStartTime = System.nanoTime()
    var otherEndTime = 0L

    while (Yevt.size != 0) {

      Cevt.clear()
      ClearInCevt()

      for (v <- Yevt) {
        for (c <- subscription(v.id)) {
          if (!inCevt(c.id)) {
            Cevt.add(c)
            inCevt(c.id) = true
          }
        }
      }

      otherEndTime = System.nanoTime()
      helper.lockTime += otherEndTime - otherStartTime

      helper.searchState = 2 //"propagate"
      // 论域改动的变量stamp = gstamp+1
      helper.pool.invokeAll(Cevt)
      helper.c_sum += Cevt.size()
      helper.p_sum += 1
      if (!helper.isConsistent) {
        return false
      }

      otherStartTime = System.nanoTime()

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

    var otherStartTime = System.nanoTime()
    var otherEndTime = 0L

    while (Yevt.size != 0) {

      Cevt.clear()
      ClearInCevt()

      for (v <- Yevt) {
        for (c <- subscription(v.id)) {
          if (!inCevt(c.id)) {
            Cevt.add(c)
            inCevt(c.id) = true
          }
        }
      }

      otherEndTime = System.nanoTime()
      helper.lockTime += otherEndTime - otherStartTime

      helper.searchState = 2 //"propagate"
      // 论域改动的变量stamp = gstamp+1
      helper.pool.invokeAll(Cevt)
      helper.c_sum += Cevt.size()
      helper.p_sum += 1
      if (!helper.isConsistent) {
        return false
      }

      otherStartTime = System.nanoTime()

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
