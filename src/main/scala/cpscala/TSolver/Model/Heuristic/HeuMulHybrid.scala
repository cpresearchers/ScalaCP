package cpscala.TSolver.Model.Heuristic

import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

class HeuMulHybrid[VT <: Var :ClassTag, PT <: Propagator[VT]](numVars: Int, vars: Array[VT], subscription: Array[ArrayBuffer[PT]]) extends Heuristic[VT] {

  override def selectLiteral(level: Int, levelvdense: Array[Int]): (VT, Int) = {
    var dmddSum: Double = 0
    var dmwdSum: Double = 0
    val dmddVars: Array[Double] = Array.fill(numVars)(0)
    val dmwdVars: Array[Double] = Array.fill(numVars)(0)
    var minHybrid = Double.MaxValue
    var minvid = levelvdense(level)

    var i = level
    var ddeg: Double = 0
    var wdeg: Double = 0
    while (i < numVars) {
      val vid = levelvdense(i)
      val v = vars(vid)
      ddeg = 0
      wdeg = 0

      for (c <- subscription(vid)) {
        if (c.assignedCount + 1 < c.arity) {
          ddeg += 1
          wdeg += c.failWeight
        }
      }

      val sizeD: Double = v.size.toDouble
      if (ddeg != 0) {
        val dmdd = sizeD / ddeg
        dmddVars(vid) = dmdd
        dmddSum += dmdd
      }

      if (wdeg != 0) {
        val dmwd = sizeD / wdeg
        dmwdVars(vid) = dmwd
        dmwdSum += dmwd
      }

      i += 1
    }

    i = level
    while (i < numVars) {
      val vid = levelvdense(i)

      if (dmddVars(vid) == 0) {
        dmddVars(vid) = 1
      } else {
        dmddVars(vid) = dmddVars(vid) / dmddSum
      }

      if (dmwdVars(vid) == 0) {
        dmwdVars(vid) = 1
      } else {
        dmwdVars(vid) = dmwdVars(vid) / dmwdSum
      }

      val hybrid = dmddVars(vid) * dmwdVars(vid)
      if (hybrid < minHybrid) {
        minHybrid = hybrid
        minvid = vid
      }

      i += 1
    }

    return (vars(minvid), vars(minvid).minValue())
  }

}

