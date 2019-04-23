package cpscala.TSolver.CpUtil.SearchHelper

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong, AtomicLongArray}

import cpscala.TSolver.Model.Constraint.IPbitConstraint.IPbitPropagator
import cpscala.TSolver.CpUtil.Constants

import scala.collection.mutable.ArrayBuffer


class IPbitSearchHelper(override val numVars: Int, override val numTabs: Int, val parallelism: Int) extends SearchHelper(numVars, numTabs) {

  val pool = if (parallelism == -1) new ForkJoinPool() else new ForkJoinPool(parallelism)

  // 比特约束组个数
  val numBitCons = Math.ceil(numTabs.toDouble / Constants.BITSIZE.toDouble).toInt
  val bitTmp = Array.fill[Long](numBitCons)(0L)
//  bitTmp(numBitCons - 1) <<= (Constants.BITSIZE - numTabs % Constants.BITSIZE)
  //subMask[i]表示第i个约束是否需要被提交
  val subMask = new AtomicLongArray(bitTmp)

//  val subscription = new Array[ArrayBuffer[IPbitPropagator]](numVars)

  // 被提交运行的约束个数
  val numSubCons = new AtomicLong(0L)
  // 本次传播是否有变量的论域发生改变
  val varIsChange = new AtomicBoolean(false)

  def submitToPool(c: IPbitPropagator): Unit = {
      numSubCons.incrementAndGet()
      pool.submit(c)
      //      println(s"   cur_cid: ${c.id} submit")
  }

  def poolAwait() = {
    while (numSubCons.get != 0) {}
  }
}

