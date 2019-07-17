package cpscala.TSolver.CpUtil.SearchHelper

import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Solver.Others.LMaxRPC_BitRM
import cpscala.TSolver.Model.Variable.{BitSetVar, Var}
import cpscala.XModel.XModel

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet

class LMaxRPCSearchHelper(override val numVars: Int, override val numTabs: Int, xm: XModel) extends SearchHelper(numVars, numTabs) {
  val subscription = new Array[ArrayBuffer[LMaxRPC_BitRM]](numVars)(new ArrayBuffer[LMaxRPC_BitRM]())
  // 为两两变量间生成中间变量的矩阵
  val commonVar = Array.ofDim[ArrayBuffer[BitSetVar]](numVars, numVars)
  // 为两两变量间生成共同约束的矩阵
  val commonCon = Array.ofDim[ArrayBuffer[LMaxRPC_BitRM]](numVars, numVars)
  // 存储一个变量所有的临域
  val neiVar = Array.fill(numVars)(new ArrayBuffer[BitSetVar])


  val maxNumBit = Constants.getNumBit(xm.max_domain_size)


}

