package cpscala.TSolver.Model.Solver.DSPFDESolver

import cpscala.TSolver.Model.Solver.FDESolver.FDESolver
import cpscala.TSolver.Model.Variable.{PVar, Var}
import cpscala.XModel.FDEModel1

class DSPFDECoarseSolver(fdeM: FDEModel1, parallelism: Int, propagatorName: String, varType: String, heuName: String) extends DSPFDESolver(fdeM, parallelism, propagatorName, varType, heuName) {

  override def initialPropagate(): Boolean = {
    for (c <- 0 until fdeM.num_OriTabs) {
      tabs(c).setup()
    }

    helper.globalStamp += 1
    // 初始删值
    for (c <- 0 until fdeM.num_OriTabs) {
      if (!tabs(c).setup()) {
        return false
      }
    }

    helper.isConsistent = true
    start_time = System.nanoTime
    prop_start_time = System.nanoTime

    for (c <- tabs) {
      helper.submitToPool(c)
    }
    helper.poolAwait()
    return helper.isConsistent


  }

  override def checkConsistencyAfterAssignment(x: PVar): Boolean = {
    helper.isConsistent = true

    for (c <- helper.subscription(x.id)) {
      helper.submitToPool(c)
    }
    helper.poolAwait()
    return helper.isConsistent
  }

  override def checkConsistencyAfterRefutation(x: PVar): Boolean = {
    helper.isConsistent = true

    for (c <- helper.subscription(x.id)) {
      helper.submitToPool(c)
    }
    helper.poolAwait()
    return helper.isConsistent
  }

}


