package cpscala.TSolver.Model.Heuristic
import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

class HeuDomDdeg[VT <: Var :ClassTag, PT <: Propagator[VT]](numVars: Int, vars: Array[VT], subscription: Array[ArrayBuffer[PT]]) extends Heuristic[VT] {

  override def selectLiteral(level: Int, levelvdense: Array[Int]): (VT, Int) = {
    var mindmdd = Double.MaxValue
    var minvid = levelvdense(level)

    var i = level

    while (i < numVars) {
      val vid = levelvdense(i)
      val v = vars(vid)
      var ddeg: Double = 0L

      for (c <- subscription(vid)) {
        if (c.assignedCount + 1 < c.arity) {
          ddeg += 1
        }
      }

      if (ddeg == 0 && mindmdd == Double.MaxValue) {
        minvid = vid
      } else {
        val sizeD: Double = v.size.toDouble
        val dmdd = sizeD / ddeg

        // 变量论域若恰好为1，但又未赋值，则应先考虑论域大小大于1的变量，以尽早传播
        if (sizeD > 1 && dmdd < mindmdd) {
          minvid = vid
          mindmdd = dmdd
        }
      }

      i += 1
    }

    return (vars(minvid), vars(minvid).minValue())
  }

}
