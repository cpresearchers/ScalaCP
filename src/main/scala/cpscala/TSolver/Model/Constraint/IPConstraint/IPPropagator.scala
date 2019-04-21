package cpscala.TSolver.Model.Constraint.IPConstraint

import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean

import cpscala.TSolver.Model.Variable.PVar

abstract class IPPropagator extends Callable[Boolean] {
  val id: Int
  val arity: Int
  val scope: Array[PVar]
  var level = 0
  var assignedCount = 0
//  val helper: IPSearchHelper
  // 本线程继续
  var localContinue = new AtomicBoolean(false)
  // 由其它线程继续
  var loopContinueByOther = new AtomicBoolean(false)

  var blocking = new AtomicBoolean(false)

  def setup(): Unit = ???

  def propagate(): Boolean

  def newLevel(): Unit

  def backLevel(): Unit
}
