package cpscala.TSolver.Model.Constraint.SConstraint

import cpscala.TSolver.CpUtil.SearchHelper.{LMaxRPCSearchHelper, SearchHelper}
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

class LMaxRPC_BitRM(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: LMaxRPCSearchHelper)extends Propagator {
  override def propagate(evt: ArrayBuffer[Var]): Boolean = ???

  override def newLevel(): Unit = ???

  override def backLevel(): Unit = ???

  override def stepUp(num_vars: Int): Unit = ???

  override def isEntailed(): Boolean = ???

  override def isSatisfied(): Unit = ???
}
