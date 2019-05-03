package cpscala.TSolver.CpUtil.SearchHelper

import java.util.concurrent.{ForkJoinPool, TimeUnit}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong, AtomicLongArray}

import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.Model.Constraint.IPbitConstraint.IPbitPropagator

class IPbit2SearchHelper(override val numVars: Int, override val numTabs: Int, val numBitCons: Int, val parallelism: Int) extends SearchHelper(numVars, numTabs) {

  // tableMask[i]表示第i个约束是否需要被提交
  val tableMask = Array.fill[Long](numBitCons)(-1L)
  tableMask(numBitCons - 1) <<= (Constants.BITSIZE - numTabs % Constants.BITSIZE)
  // 变量所对应的比特约束组，第n个bit位为1说明第n个约束的scope包含该变量
  val bitSrb = Array.fill[Long](numVars, numBitCons)(0L)

  def clearTableMask(): Unit = {
    var i = 0
    while (i < numBitCons) {
      tableMask(i) = 0L
      i += 1
    }
  }

  // 在第cid个约束内，第vid个变量的论域被缩减，则将该变量对应的比特约束组提交至subMask（或运算）
  def addToTableMask(cid: Int, vid: Int): Unit = this.synchronized {
    var i = 0
    val (x, y) = INDEX.getXY(cid)
    while (i < numBitCons) {
      if (i == x) {
        // 因为在第cid个约束内，第vid个变量的论域被缩减，所以无需提交第cid个约束
        tableMask(i) |= (bitSrb(vid)(i) & Constants.MASK0(y))
      } else {
        tableMask(i) |= bitSrb(vid)(i)
      }
      i += 1
    }
  }

  def getTableMask(mask: Array[Long]): Unit = {
    var i = 0
    while (i < numBitCons) {
      mask(i) = tableMask(i)
      i += 1
    }
  }

  // 初始化比特subscription
  def setSrb(cid: Int, vid: Int): Unit = {
    val (x, y) = INDEX.getXY(cid)
    bitSrb(vid)(x) |= Constants.MASK1(y)
  }

  def getSrb(vid: Int, mask: Array[Long]): Unit = {
    var i = 0
    while (i < numBitCons) {
      mask(i) = bitSrb(vid)(i)
      i += 1
    }
  }

  def showSrb(): Unit = {
    for (i <- 0 until numVars) {
      println(s"var: ${i} subs is ======>")
      for (j <- 0 until numBitCons) {
        println(Constants.toFormatBinaryString(bitSrb(i)(j)))
      }
    }
  }

  // 线程池
  val pool = if (parallelism == -1) new ForkJoinPool() else new ForkJoinPool(parallelism)
  // 被提交运行的约束个数
  val numSubCons = new AtomicLong(0L)
  // 本次传播是否有变量的论域发生改变
  val varIsChange = new AtomicBoolean(true)

  def submitToPool(c: IPbitPropagator): Unit = {
    numSubCons.incrementAndGet()
    pool.submit(c)
//    println(s"   cur_cid: ${c.id} submit")
  }

  def poolAwait(): Unit = {
    //    pool.awaitQuiescence(1, TimeUnit.DAYS)
    while (numSubCons.get != 0) {
      pool.awaitQuiescence(1, TimeUnit.DAYS)
    }
  }
}

