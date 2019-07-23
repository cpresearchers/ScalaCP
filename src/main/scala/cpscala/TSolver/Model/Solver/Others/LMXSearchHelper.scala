package cpscala.TSolver.Model.Solver.Others

import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.Model.Variable.BitSetVar
import cpscala.XModel.XModel

import scala.collection.mutable.ArrayBuffer

class LMXSearchHelper(override val numVars: Int, override val numTabs: Int, xm: XModel) extends SearchHelper(numVars, numTabs) {
  //  val subscription = new Array[ArrayBuffer[LMaxRPC_BitRM]](numVars)(new ArrayBuffer[LMaxRPC_BitRM]())
  val subscription = Array.fill(numTabs)(new ArrayBuffer[LMX_BitRM])
  // 为两两变量间生成中间变量的矩阵
  val commonVar = Array.fill(numVars)(Array.fill(numVars)(new ArrayBuffer[BitSetVar_LMRPC]))
  // 为两两变量间生成共同约束的矩阵
  val commonCon: Array[Array[ArrayBuffer[LMX_BitRM]]] = Array.fill(numVars)(Array.fill(numVars)(new ArrayBuffer[LMX_BitRM]))
  // 存储一个变量所有的临域
  val neiVar = Array.fill(numVars)(new ArrayBuffer[BitSetVar_LMRPC])


  val maxNumBit = Constants.getNumBit(xm.max_domain_size)


}

