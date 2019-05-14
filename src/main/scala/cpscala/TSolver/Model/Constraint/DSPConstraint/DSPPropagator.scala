package cpscala.TSolver.Model.Constraint.DSPConstraint

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.concurrent.locks.ReentrantLock

import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.PVar

import scala.collection.mutable

abstract class DSPPropagator extends Propagator[PVar] with Runnable {

  // scope内变量时间戳
  val scopeStamp: Array[Long] = Array.fill[Long](arity)(-1)
  // scope映射，key为scope内变量的id，value为变量在scope内的index
  val scopeMap = new mutable.HashMap[Int, Int]

  for (i <- 0 until arity) {
    scopeMap(scope(i).id) = i
  }

  var loopContinue: Boolean = false
  // 运行状态
  // runningStatus = 0 未运行
  // runningStatus = 1 运行
  val runningStatus = new AtomicInteger(0)
  val lock = new ReentrantLock()
  val isLock = new AtomicBoolean(false)

  def domainChanged(v: PVar, mask: Array[Long]): Boolean = ???

  def domainChanged(v: PVar): Boolean = ???

  def propagate(): Boolean

}
