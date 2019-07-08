package cpscala.TSolver.Model.Solver.DSPSolver

import cpscala.TSolver.Model.Variable.PVar
import cpscala.XModel.XModel

class DSPCoarseSolver(xm: XModel, parallelism: Int, propagator_name: String, var_type: String, heu_name: String) extends DSPSolver(xm, parallelism, propagator_name, var_type, heu_name) {

  def initialPropagate(): Boolean = {
    helper.isConsistent = true
    start_time = System.nanoTime
    prop_start_time = System.nanoTime

    for (c <- tabs) {
      helper.submitToPool(c)
    }
    helper.poolAwait()
    if (!helper.isConsistent) {
      helper.stopTime += System.nanoTime - helper.inconsistentTime
    }
    return helper.isConsistent
  }

  def checkConsistencyAfterAssignment(x: PVar): Boolean = {
    helper.isConsistent = true

    for (c <- helper.subscription(x.id)) {
      helper.submitToPool(c)
    }
    helper.poolAwait()
    if (!helper.isConsistent) {
      helper.stopTime += System.nanoTime - helper.inconsistentTime
    }
    return helper.isConsistent
  }

  def checkConsistencyAfterRefutation(x: PVar): Boolean = {
    helper.isConsistent = true
    for (c <- helper.subscription(x.id)) {
      helper.submitToPool(c)
    }
    helper.poolAwait()
    if (!helper.isConsistent) {
      helper.stopTime += System.nanoTime - helper.inconsistentTime
    }
    return helper.isConsistent
  }

}
