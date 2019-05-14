package cpscala.TSolver.Model.Solver.IPplusSolver

import cpscala.TSolver.Model.Variable.PVar
import cpscala.XModel.XModel

// 根据Yevt内单个变量提交约束至线程池
class IPSingleCoarseSolver(xm: XModel, parallelism: Int, propagatorName: String, varType: String, heuName: String) extends IPplusSolver(xm, parallelism, propagatorName, varType, heuName) {

  //inCevt[i]表示第i个约束是否被提交
  val inCevt = Array.fill(numTabs)(false)

  def ClearInCevt() = {
    var i = 0
    while (i < numTabs) {
      inCevt(i) = false
      i += 1
    }
  }

  var maxSbrSize = 0
  for (v <- vars){
    if(subscription(v.id).size > maxSbrSize) {
      maxSbrSize = subscription(v.id).size
    }
  }

  override def initialPropagate(): Boolean = {
    start_time = System.nanoTime
    prop_start_time = System.nanoTime
    return propagate(null)
  }

  override def checkConsistencyAfterAssignment(v: PVar): Boolean = {
    return propagate(v)
  }

  override def checkConsistencyAfterRefutation(v: PVar): Boolean = {
    return propagate(v)
  }

  def propagate(v: PVar): Boolean = {
    helper.isConsistent = true
    Yevt.clear()

    if (v == null) {
      Yevt ++= vars
    } else {
      Yevt += v
      helper.pGlobalStamp.incrementAndGet()
      helper.pVarStamp.set(v.id, helper.pGlobalStamp.get())
    }

    while (Yevt.size != 0) {
      ClearInCevt()
      val oldpGlobalStamp = helper.pGlobalStamp.get()

      var j = 0
      while (j < maxSbrSize){
        for (v <- Yevt) {
          if (subscription(v.id).size > j){
            val c = subscription(v.id)(j)
            val index = c.scopeMap(v.id)
            if (!inCevt(c.id) && helper.pVarStamp.get(v.id) > c.scopeStamp(index)) {
              inCevt(c.id) = true
              helper.c_sum += 1
              helper.submitToPool(c)
            }
          }
        }
        j += 1
      }

//      for (v <- Yevt) {
//        for (c <- subscription(v.id)) {
//          val index = c.scopeMap(v.id)
//          if (!inCevt(c.id) && helper.pVarStamp.get(v.id) > c.scopeStamp(index)) {
//            inCevt(c.id) = true
//            helper.c_sum += 1
//            helper.submitToPool(c)
//          }
//        }
//      }
      helper.poolAwait()
      helper.p_sum += 1

      if (!helper.isConsistent) {
        return false
      }

      Yevt.clear()
      var i = helper.level
      while (i < numVars) {
        val vid = levelvdense(i)
        //若上轮改过了
        if (helper.pVarStamp.get(vid) > oldpGlobalStamp) {
          Yevt += vars(vid)
        }
        i += 1
      }
    }
    return true
  }

}

