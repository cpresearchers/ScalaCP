package cpscala.TSolver.Model.Solver.IPSolver

import cpscala.TSolver.Model.Variable.PVar
import cpscala.XModel.XModel

/**
  * 细粒度并行求解器，适用于PSTR3和PSTRbit。
  */

class IPFineSolver(xm: XModel, parallelism: Int, propagator_name: String, var_type: String, heu_name: String) extends IPSolver(xm, parallelism, propagator_name, var_type, heu_name) {

  def initialPropagate(): Boolean = {

    // 约束表初始化
    for (c <- tabs) {
      c.setup()
    }

    start_time = System.nanoTime
    prop_start_time = System.nanoTime

    // 初始删值
    for (c <- tabs) {
      c.setup()
    }
    if (!helper.isConsistent) {
      return false
    }
    helper.globalStamp += 1

    // 初始传播
    helper.isConsistent = true
    Yevt.clear()
    var i = 0
    for (i <- 0 until numVars) {
      if (helper.varStamp(i) == helper.globalStamp) {
        Yevt += vars(i)
      }
    }

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

      // 论域改动的变量stamp = gstamp+1
      helper.pool.invokeAll(Cevt)
      helper.c_sum += Cevt.size()
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


  def checkConsistencyAfterAssignment(ix: PVar): Boolean = {

    helper.isConsistent = true
    Yevt.clear()
    Yevt += ix

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

      // 论域改动的变量stamp = gstamp+1
      helper.pool.invokeAll(Cevt)
      helper.c_sum += Cevt.size()
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

      // 论域改动的变量stamp = gstamp+1
      helper.pool.invokeAll(Cevt)
      helper.c_sum += Cevt.size()
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
