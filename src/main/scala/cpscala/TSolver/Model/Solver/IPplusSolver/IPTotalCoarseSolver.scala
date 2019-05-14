package cpscala.TSolver.Model.Solver.IPplusSolver

import cpscala.TSolver.Model.Variable.PVar
import cpscala.XModel.XModel

// 根据Yevt整体提交约束至线程池
class IPTotalCoarseSolver(xm: XModel, parallelism: Int, propagatorName: String, varType: String, heuName: String) extends IPplusSolver(xm, parallelism, propagatorName, varType, heuName) {

  //inCevt[i]表示第i个约束是否被提交
  val inCevt = Array.fill(numTabs)(false)

  def ClearInCevt() = {
    var i = 0
    while (i < numTabs) {
      inCevt(i) = false
      i += 1
    }
  }

  def initialPropagate(): Boolean = {

    start_time = System.nanoTime
    prop_start_time = System.nanoTime

    helper.isConsistent = true
    Yevt.clear()
    Yevt ++= vars

    while (Yevt.nonEmpty) {

      ClearInCevt()
//      helper.c_sum = 0
      for (v <- Yevt) {
        for (c <- subscription(v.id)) {
          if (!inCevt(c.id)) {
            inCevt(c.id) = true
            helper.c_sum += 1
            helper.submitToPool(c)
            //            //println(s"${c.id} submit-----")
          }
        }
      }
      helper.poolAwait()
      helper.p_sum += 1

      //println(s"  ${helper.p_sum} p_sum's c_sum: ${helper.c_sum}")

      if (!helper.isConsistent) {
        return false
      }

      // 论域改动的变量stamp = gstamp+1
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
//      helper.c_sum = 0
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
      helper.p_sum += 1
      //println(s"  ${helper.p_sum} p_sum's c_sum: ${helper.c_sum}")

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

  def checkConsistencyAfterRefutation(ix: PVar): Boolean = {

    helper.isConsistent = true
    Yevt.clear()
    Yevt += ix

    while (Yevt.size != 0) {
      ClearInCevt()
//      helper.c_sum = 0

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
      helper.p_sum += 1
      //println(s"  ${helper.p_sum} p_sum's c_sum: ${helper.c_sum}")

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

}
