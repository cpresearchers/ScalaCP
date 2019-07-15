package cpscala.TSolver.CpUtil.SearchHelper

import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet

class LMaxRPCSearchHelper(override val numVars: Int, override val numTabs: Int) extends SearchHelper(numVars, numTabs) {
  val subscription = new Array[ArrayBuffer[Propagator]](numVars)(new ArrayBuffer[Propagator]())
  val commonVar = Array.ofDim[ArrayBuffer[Var]](numVars, numVars)
  // 为二元约束生成
  val commonNeibor = Array.ofDim[ArrayBuffer[Propagator]](numVars, numVars)
  val comVar = HashSet.empty[Var]



  //  val p = Promise[Int]
  //  val f = p.future

//  var ii = 0
//  while (ii < numVars) {
//    subscription(ii) = new ArrayBuffer[DSPPropagator]()
//    ii += 1
//  }
}

