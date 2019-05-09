package cpscala.TSolver.Model.Constraint.SConstraint

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

abstract class Propagator[VT <: Var] {

  val id: Int
  val arity: Int
  val scope: Array[VT]
  var level = 0
  // 约束scope中被赋值的变量个数
  var assignedCount = 0
  // 失败权重，搜索过程中该约束的传播失败次数，在一些启发式中会用到，比如dom/wdeg
  var failWeight = 0
  val helper: SearchHelper

  def setup(): Boolean = ???

  def propagate(evt: ArrayBuffer[Var]): Boolean = ???

  def newLevel(): Unit

  def backLevel(): Unit

  def stepUp(num_vars: Int): Unit = ???

  def isEntailed(): Boolean = ???

  def isSatisfied(): Unit = ???

  //  def assignedCount(): Int = {
  //    var i = 0
  //
  //    for (v <- scope) {
  //      if (v.isBind()) {
  //        i += 1
  //      }
  //    }
  //
  //    return i
  //
  ////    return scope.iterator.count(p => p.isBind())
  //  }


}
