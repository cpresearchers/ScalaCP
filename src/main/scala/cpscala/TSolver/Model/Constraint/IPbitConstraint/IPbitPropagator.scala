package cpscala.TSolver.Model.Constraint.IPbitConstraint

import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicLong

import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.PVar

abstract class IPbitPropagator extends Propagator[PVar] with Callable[Unit] {

  // 原子失败权重，搜索过程中该约束的传播失败次数，在一些启发式中会用到，比如dom/wdeg
//  val atomFailWeight = new AtomicLong(0L)

  def propagate(): Boolean

}