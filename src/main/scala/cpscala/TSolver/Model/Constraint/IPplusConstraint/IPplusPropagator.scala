package cpscala.TSolver.Model.Constraint.IPplusConstraint

import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicLong

import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.PVar

import scala.collection.mutable

abstract class IPplusPropagator extends Propagator[PVar] with Callable[Unit] {

  // scope内变量时间戳
  val scopeStamp: Array[Long] = Array.fill[Long](arity)(-1)
  // scope映射，key为scope内变量的id，value为变量在scope内的index
  val scopeMap = new mutable.HashMap[Int, Int]

  for (i <- 0 until arity) {
    scopeMap(scope(i).id) = i
  }

  // 原子失败权重，搜索过程中该约束的传播失败次数，在一些启发式中会用到，比如dom/wdeg
  //  val atomFailWeight = new AtomicLong(0L)

  def propagate(): Boolean

}