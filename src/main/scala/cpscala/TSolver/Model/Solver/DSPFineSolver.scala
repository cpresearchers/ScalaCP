package cpscala.TSolver.Model.Solver

import cpscala.TSolver.Model.Variable.PVar
import cpscala.XModel.XModel

class DSPFineSolver(xm: XModel, parallelism: Int, propagatorName: String, varType: String, heuName: String) extends DSPSolver(xm, parallelism, propagatorName, varType, heuName){

  def initialPropagate(): Boolean = {

    // 约束表初始化
    helper.varIsChange.set(false)
    for (c <- tabs){
      c.setup()
    }

    helper.isConsistent = true
    start_time = System.nanoTime
    prop_start_time = System.nanoTime

    // 初始删值
    for (c <- tabs){
      c.setup()
    }
    if (!helper.isConsistent) {
      return false
    }

    if(helper.varIsChange.get()){
      for (c <- tabs) {
        helper.submitToPool(c)
      }
      helper.poolAwait()
    }
    return helper.isConsistent
  }

  def checkConsistencyAfterAssignment(x: PVar): Boolean = {
    helper.isConsistent = true

    for (c <- helper.subscription(x.id)) {
      helper.submitToPool(c)
    }
    helper.poolAwait()
    return helper.isConsistent
  }

  def checkConsistencyAfterRefutation(x: PVar): Boolean = {
    helper.isConsistent = true

    for (c <- helper.subscription(x.id)) {
      helper.submitToPool(c)
    }

    helper.poolAwait()
    return helper.isConsistent
  }

}
