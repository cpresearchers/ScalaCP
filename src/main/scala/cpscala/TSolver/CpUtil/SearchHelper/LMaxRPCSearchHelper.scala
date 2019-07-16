package cpscala.TSolver.CpUtil.SearchHelper

import cpscala.TSolver.Model.Constraint.SConstraint.{LMaxRPC_BitRM, Propagator}
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet

class LMaxRPCSearchHelper(override val numVars: Int, override val numTabs: Int) extends SearchHelper(numVars, numTabs) {
  val subscription = new Array[ArrayBuffer[LMaxRPC_BitRM]](numVars)(new ArrayBuffer[LMaxRPC_BitRM]())
  // 为两两变量间生成中间变量的矩阵
  val commonVar = Array.ofDim[ArrayBuffer[Var]](numVars, numVars)
  // 为两两变量间生成共同约束的矩阵
  val commonCon = Array.ofDim[ArrayBuffer[LMaxRPC_BitRM]](numVars, numVars)
  // 存储一个变量所有的临域
  val neiborVar = new Array[ArrayBuffer[Var]](numVars)(new ArrayBuffer[Var]())

  val comVar = HashSet.empty[Var]



  //  val p = Promise[Int]
  //  val f = p.future

//  var ii = 0
//  while (ii < numVars) {
//    subscription(ii) = new ArrayBuffer[DSPPropagator]()
//    ii += 1
//  }
}

