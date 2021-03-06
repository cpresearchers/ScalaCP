package cpscala.TSolver.CpUtil.SearchHelper

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong, AtomicLongArray}
import java.util.concurrent.{ForkJoinPool, TimeUnit}

import cpscala.TSolver.Model.Constraint.DSPConstraint.DSPPropagator

import scala.collection.mutable.ArrayBuffer

class DSPSearchHelper(override val numVars: Int, override val numTabs: Int, val parallelism: Int) extends SearchHelper(numVars, numTabs) {
  val pool = if (parallelism == -1) new ForkJoinPool() else new ForkJoinPool(parallelism)

  //  import java.util.concurrent.ExecutorService
  //  import java.util.concurrent.Executors

  //  val pool = Executors.newWorkStealingPool
  val subscription = new Array[ArrayBuffer[DSPPropagator]](numVars)
  // 运行次数
  val c_prop = new AtomicLong(0L)
  // 动态提交次数
  val c_sub = new AtomicLong(0L)
  val counter = new AtomicLong(0L)
  // 本次传播是否有变量的论域发生改变
  val varIsChange = new AtomicBoolean(false)

  //  val p = Promise[Int]
  //  val f = p.future

  var ii = 0
  while (ii < numVars) {
    subscription(ii) = new ArrayBuffer[DSPPropagator]()
    ii += 1
  }

  var pGlobalStamp = new AtomicLong(0L)
  val pVarStamp = new AtomicLongArray(varStamp)

  @inline def submitToPool(c: DSPPropagator): Unit = {
    if (c.runningStatus.getAndIncrement() == 0) {
      counter.incrementAndGet()
      c_sub.incrementAndGet()
      //      c.reinitialize()
      pool.execute(c)
      //      println(s"   cur_cid: ${c.id} submit")
    }
  }

  //
  //  @inline def fork(c: DSPPropagator): Unit = {
  //    //    while (c.lock.isLocked()) {}
  //    if (c.runningStatus.getAndIncrement() == 0) {
  //      c_sub.incrementAndGet()
  ////      c.reinitialize()
  //      c.fork()
  //    }
  //  }

  //  @inline def invoke(c: DSPPropagator): Unit = {
  //    //    while (c.lock.isLocked()) {}
  //    if (c.runningStatus.getAndIncrement() == 0) {
  //      c_sub.incrementAndGet()
  ////      c.reinitialize()
  //      c.quietlyInvoke()
  //    }
  //  }

  @inline def poolAwait() = {
    while (counter.get != 0) {
      pool.awaitQuiescence(1, TimeUnit.DAYS)
    }
    //    Await(f.result())
    //    Await.ready(f, Duration.Inf)
  }


}
