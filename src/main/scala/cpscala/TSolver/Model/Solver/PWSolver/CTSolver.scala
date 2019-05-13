package cpscala.TSolver.Model.Solver.PWSolver

import cpscala.TSolver.Model.Constraint.PWConstraint.fPWCPropagator
import cpscala.TSolver.Model.Variable.Var
import cpscala.XModel.XModel

import scala.collection.mutable.ArrayBuffer

class CTSolver(xm: XModel, propagatorName: String, varType: String, heuName: String) extends PWSolver(xm, propagatorName, varType, heuName) {
  override def initialPropagate(): Boolean = {
    start_time = System.nanoTime
    prop_start_time = System.nanoTime
    return propagate(null)
  }

  override def checkConsistencyAfterAssignment(ix: Var): Boolean = {
    return propagate(subscription(ix.id))
  }

  override def checkConsistencyAfterRefutation(ix: Var): Boolean = {
    return propagate(subscription(ix.id))
  }

  def propagate(x: ArrayBuffer[fPWCPropagator]): Boolean = {
    CT_Q.clear()
    if (x == null) {
      //初始化
      for (z <- tabs) {
        insert(z)
      }
    } else {
      for (z <- x) {
        insert(z)
      }
    }
    while (!CT_Q.empty()) {
      val c = CT_Q.pop()
      Y_evt.clear()
      val consistent = c.propagateGAC(Y_evt)
      helper.c_sum += 1
      if (!consistent._1) {
        return false
      } else {
        for (y <- Y_evt) {
          for(tab<-subscription(y.id))
            insert(tab)
        }
      }
    }
    return true
  }

  def insert(x: fPWCPropagator): Unit = {
    CT_Q.push(x)
    PWC_Q.push(x)
  }
}
