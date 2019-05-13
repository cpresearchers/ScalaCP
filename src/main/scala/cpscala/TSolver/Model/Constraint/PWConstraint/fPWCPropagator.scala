package cpscala.TSolver.Model.Constraint.PWConstraint

import cpscala.TSolver.CpUtil.{Block, PWRSparseBitSet}
import cpscala.TSolver.CpUtil.SearchHelper.PWSearchHelper
import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

abstract class fPWCPropagator extends Propagator[Var]{

  var living: PWRSparseBitSet = null
  val helper:PWSearchHelper

  override def setup(): Boolean = ???

  override def propagate(evt: ArrayBuffer[Var]): Boolean = ???

  def newLevel(): Unit

  def backLevel(): Unit

  override def stepUp(num_vars: Int): Unit = ???

  override def isEntailed(): Boolean = ???

  override def isSatisfied(): Unit = ???

  // input: 被改变人变量
  // return (是否相容，是否改变)
  def propagateGAC(evt: ArrayBuffer[Var]): (Boolean, Boolean)

  //
  def propagatePWC(xEvt: ArrayBuffer[Var],cEvt: ArrayBuffer[Int]):Boolean

  def clearLivingDelta()

  //返回living是否修改
  def removeBlock(vars: ArrayBuffer[Int], t: Array[Int],cscope:Array[Var]):Boolean

  def createBlock(vars: ArrayBuffer[Int], t: Array[Int]): Block

  def interesectIndex(vars: ArrayBuffer[Int], t: Array[Int],cscope:Array[Var]):Int

  def addBlockToMask(vars: ArrayBuffer[Int], t: Array[Int],cscope:Array[Var])

  def getTuple(i:Int):Array[Int]

  def getTupleLength():Int


}
