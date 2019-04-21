package cpscala.TSolver.CpUtil.SearchHelper

import java.util.ArrayList
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicLongArray

import cpscala.TSolver.Model.Constraint.IPConstraint.IPPropagator
import cpscala.TSolver.CpUtil.Constants

import scala.collection.mutable.ArrayBuffer


class IPbitSearchHelper(override val numVars: Int, override val numTabs: Int, val parallelism: Int) extends SearchHelper(numVars, numTabs) {

  // 并行时，根据搜索状态执行相应的代码
  // searchState = 0 setup
  // searchState = 1 newLevel
  // searchState = 2 propagate
  // searchState = 3 backLevel
  var searchState = 0
  val pool = if (parallelism == -1) new ForkJoinPool() else new ForkJoinPool(parallelism)

  // 比特约束组个数
  val numBit = Math.ceil(numTabs.toDouble / Constants.BITSIZE.toDouble).toInt
  val bitTmp = Array.fill[Long](numBit)(0L)
  bitTmp(numBit - 1) <<= (Constants.BITSIZE - numTabs % Constants.BITSIZE)
  //inCevt[i]表示第i个约束是否在Cevt中
  val inCevt = new AtomicLongArray(numTabs)

  val subscription = new Array[ArrayBuffer[IPPropagator]](numVars)
  val Cevt = new ArrayList[IPPropagator](numTabs)
  Cevt.clear()

  var ii = 0
  while (ii < numTabs) {
    inCevt.set(ii, 0)
    ii += 1
  }

  ii = 0
  while (ii < numVars) {
    subscription(ii) = new ArrayBuffer[IPPropagator]()
    ii += 1
  }


  @inline def clearCevt() = {
    Cevt.clear()

    ii = 0
    while (ii < numTabs) {
      inCevt.set(ii, 0)
      ii += 1
    }
  }

  @inline def addToCevt(c: IPPropagator): Unit = {
    if (inCevt.get(c.id) == 0) {
      Cevt.add(c)
      inCevt.set(c.id, 1)
    }
  }

  @inline def invokeCevt(): Any = {
    val c = pool.invokeAll(Cevt)

    return
  }
}

