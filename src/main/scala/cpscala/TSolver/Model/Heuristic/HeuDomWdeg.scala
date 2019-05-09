package cpscala.TSolver.Model.Heuristic
import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

class HeuDomWdeg[VT <: Var :ClassTag, PT <: Propagator[VT]](numVars: Int, vars: Array[VT], subscription: Array[ArrayBuffer[PT]]) extends Heuristic[VT] {

  override def selectLiteral(level: Int, levelvdense: Array[Int]): (VT, Int) = {
    var mindmwd = Double.MaxValue
    var minvid = levelvdense(level)

    var i = level
    while (i < numVars) {
      val vid = levelvdense(i)
      val v = vars(vid)
      var wdeg: Double = 0L

      for (c <- subscription(vid)) {
        if (c.assignedCount + 1 < c.arity) {
          wdeg += c.failWeight
        }
      }

      if (wdeg == 0 && mindmwd == Double.MaxValue) {
        minvid = vid
      } else {
        val sizeD: Double = v.size.toDouble
        val dmwd = sizeD / wdeg

        // 变量论域若恰好为1，但又未赋值，则应先考虑论域大小大于1的变量，以尽早传播
        if (sizeD > 1 && dmwd < mindmwd) {
          minvid = vid
          mindmwd = dmwd
        }
      }

      i += 1
    }

    return (vars(minvid), vars(minvid).minValue())
  }

}
