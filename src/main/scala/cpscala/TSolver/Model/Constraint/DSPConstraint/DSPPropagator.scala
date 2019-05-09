package cpscala.TSolver.Model.Constraint.DSPConstraint

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.concurrent.locks.ReentrantLock

import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.PVar

abstract class DSPPropagator extends Propagator[PVar] with Runnable {

  //  val loopContinue = new AtomicBoolean(false)
  var loopContinue: Boolean = false
  // 运行状态
  // runningStatus = 0 未运行
  // runningStatus = 1 提交任务
  // runningStatus = 2 运行
  // runningStatus = 3 循环中，循环完成退出
  // runningStatus = 4 循环中，继续下一次循环
  // runningStatus = 5 出循环，仍在运行中
  val runningStatus = new AtomicInteger(0)
  val lock = new ReentrantLock()
  val isLock = new AtomicBoolean(false)

  def domainChanged(v: PVar, mask: Array[Long]): Boolean

  def domainChanged(v: PVar): Boolean

  def propagate(): Boolean

}
