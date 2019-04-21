package cpscala.TSolver.Model.Constraint.SConstraint

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

abstract class Propagator {
  val id: Int
  val arity: Int
  val scope: Array[Var]
  var level = 0
  var assignedCount = 0
  val helper: SearchHelper

  def setup(): Boolean = ???

  def propagate(evt: ArrayBuffer[Var]): Boolean

  def newLevel()

  def backLevel()

  def stepUp(num_vars: Int)

  def isEntailed(): Boolean

  def isSatisfied()

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
