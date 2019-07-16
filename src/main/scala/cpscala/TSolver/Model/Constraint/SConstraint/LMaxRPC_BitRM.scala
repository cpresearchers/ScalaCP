package cpscala.TSolver.Model.Constraint.SConstraint

import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.CpUtil.SearchHelper.{LMaxRPCSearchHelper, SearchHelper}
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

class LMaxRPC_BitRM(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: LMaxRPCSearchHelper) extends Propagator {

  // 获取所有变量的numbit
  val numBits = Array.tabulate(arity)(i => Constants.getNumBit(scope(i).size()))
  val maxNumBits = numBits.max
  val bitSup = Array.tabulate(arity)(i => Array.ofDim(scope(i).size(), maxNumBits))
  val lastPc = Array.tabulate(arity)(i => Array.fill(scope(i).size())(INDEX.kOVERFLOW))
  val lastAc = Array.tabulate(arity)(i => Array.fill(scope(i).size())(INDEX.kOVERFLOW)) \

  for (t <- tuples) {
    val bIdx = (INDEX.getBitIndex(t(0)), INDEX.getBitIndex(t(1)))

    bitSup(0)(t(0))(bIdx._1)

  }


  override def propagate(evt: ArrayBuffer[Var]): Boolean = ???

  override def newLevel(): Unit = ???

  override def backLevel(): Unit = ???

  override def stepUp(num_vars: Int): Unit = ???

  override def isEntailed(): Boolean = ???

  override def isSatisfied(): Unit = ???
}
