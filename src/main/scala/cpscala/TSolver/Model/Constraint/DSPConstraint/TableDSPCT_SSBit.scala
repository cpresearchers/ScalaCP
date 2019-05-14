package cpscala.TSolver.Model.Constraint.DSPConstraint

import cpscala.TSolver.CpUtil.SearchHelper.DSPSearchHelper
import cpscala.TSolver.CpUtil.{Constants, INDEX, RSBitSet}
import cpscala.TSolver.Model.Variable.PVar

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class TableDSPCT_SSBit(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[PVar], val tuples: Array[Array[Int]], val helper: DSPSearchHelper) extends DSPPropagator {
  val currTab = new RSBitSet(id, tuples.length, num_vars)
  val supports = new Array[Array[Array[Long]]](arity)
  val num_bit = currTab.numBit
  val residues = new Array[Array[Int]](arity)
  // 活动变量
  val Xevt = new ArrayBuffer[PVar](arity)
  Xevt.clear()

  for (vv <- 0 until arity) {
    supports(vv) = Array.ofDim[Long](scope(vv).size, num_bit)
    residues(vv) = Array.fill(scope(vv).size)(-1)
  }

  for (i <- 0 until tuples.length) {
    val (x, y) = INDEX.getXY(i)
    val t = tuples(i)

    for (j <- 0 until t.length) {
      supports(j)(t(j))(x) |= Constants.MASK1(y)
    }
  }

//  val scopeMap = new mutable.HashMap[PVar, Int]()
//  for (i <- 0 until arity) {
//    scopeMap.put(scope(i), i)
//  }

  //存变量Index
  val Ssup = new ArrayBuffer[Int](arity)
  val Sval = new ArrayBuffer[Int](arity)

  // 获取最大论域大小
  var maxDomainSize = Int.MinValue
  scope.foreach(x => {
    maxDomainSize = math.max(maxDomainSize, x.size())
  })

  // 局部变量标记
  // 1. newMask: 原子提交后获得的论域
  // 1. oldMask: 原子提交前获得的论域
  // 2. localMask：当前调用时不断修改的论域的mask
  // 3. lastMask：上一次调用后的mask
  val localMask = Array.fill[Long](arity)(0L)
  val lastMask = Array.fill[Long](arity)(Constants.ALLONELONG)

  val lastRemovedValues = new ArrayBuffer[Int](maxDomainSize)
  lastRemovedValues.clear()
  val validValues = new ArrayBuffer[Int](maxDomainSize)
  validValues.clear()

  //判断domain是否改变
  override def domainChanged(v: PVar, mask: Array[Long]): Boolean = {
    //    mask == lastMask(scopeMap(v))
    return true
  }

  //检查变量
  def initial(): Boolean = {

    Ssup.clear()
    Sval.clear()
    // 标记SVal是否为空，为空则跳出propagate
    var snapshotChanged = false

    var i = 0
    while (i < arity) {
      val v = scope(i)
      val globalMask = v.simpleMask()

      // 本地论域快照与全局论域不同
      // 更新本地论域快照
      // snapshotChanged 即为需要propagate，否则不用propagate
      if (lastMask(i) != globalMask) {
        Sval += i
        //        localMask(i) = globalMask
        snapshotChanged = true
      }

      if (v.unBind()) {
        Ssup += i
      }
      i += 1
    }
    return snapshotChanged
  }

  def updateTable(): Boolean = {
    var i = 0
    val SvalN = Sval.length
    while (i < SvalN && helper.isConsistent) {
      val vv: Int = Sval(i)
      val v: PVar = scope(vv)
      localMask(vv) = v.simpleMask()
      currTab.clearMask()

      val lastRemovedMask: Long = (~localMask(vv)) & lastMask(vv)
      //      val valid = java.lang.Long.bitCount(lastMask(vv))
      val valid = java.lang.Long.bitCount(localMask(vv))
      val remove = java.lang.Long.bitCount(lastRemovedMask)
      lastMask(vv) = localMask(vv)

      // !!标记是否需要delta更新
      //      if ((old - last) < last) {
      if (remove < valid) {
        // delta更新
        lastRemovedValues.clear()
        var j = 0
        while (j < v.capacity) {
          if ((lastRemovedMask & Constants.MASK1(j)) != 0L) {
            lastRemovedValues += j
          }
          j += 1
        }

        for (a <- lastRemovedValues) {
          currTab.addToMask(supports(vv)(a))
        }
        currTab.reverseMask()
      }
      else {
        // 从头计算
        validValues.clear()
        var j = 0
        while (j < v.capacity) {
          if ((localMask(vv) & Constants.MASK1(j)) != 0L) {
            validValues += j
          }
          j += 1
        }
        // 重头重新
        for (a <- validValues) {
          currTab.addToMask(supports(vv)(a))
        }
      }

      val changed = currTab.intersectWithMask()

      //传播失败
      if (currTab.isEmpty()) {
        helper.isConsistent = false
        failWeight += 1
        return false
      }
      i += 1
    }

    return true
  }

  // 这里重新获得Xevt
  def filterDomains(): Boolean = {
    Xevt.clear()
    val SsupN = Ssup.length
    var i = 0
    while (i < SsupN && helper.isConsistent) {
      var deleted: Boolean = false
      val vv: Int = Ssup(i)
      val v = scope(vv)

      validValues.clear()
      var j = 0
      while (j < v.capacity) {
        if ((localMask(vv) & Constants.MASK1(j)) != 0L) {
          validValues += j
        }
        j += 1
      }

      for (a <- validValues) {
        var index = residues(vv)(a)
        if (index == -1 || (currTab.words(helper.level)(index) & supports(vv)(a)(index)) == 0L) { //res失效
          index = currTab.intersectIndex(supports(vv)(a))
          if (index != -1) { //重新找到支持
            residues(vv)(a) = index
          }
          else {
            deleted = true
            //无法找到支持, 删除(v, a)
            //            println("name: " + id + ", delete: " + v.id + "," + a + ", level: " + helper.level)
            localMask(vv) &= Constants.MASK0(a)
          }
        }
      }

      if (deleted) {
        val newMask = v.submitMaskAndGet(localMask(vv))
        // 本地线程删值
        // 提交更改，并获取新值
        if (newMask == 0L) {
          helper.isConsistent = false
          failWeight += 1
          return false
        }

        lastMask(vv) = localMask(vv)
        Xevt += v
      }
      i += 1
    }
    return true
  }

  def submitPropagtors(): Boolean = {
    // 提交其它约束
    for (x <- Xevt) {
      if (helper.isConsistent) {
        for (c <- helper.subscription(x.id)) {
          // !!这里可以加限制条件c.v.simpleMask!=x.simpleMask
          //          if (c.id != id && c.domainChanged(x, localMask(scopeMap(x)))) {
          if (c.id != id) {
            helper.submitToPool(c)
          }
        }
      }
    }
    return false
  }

  override def run(): Unit = {
    //    println(s"start: cur_ID: ${Thread.currentThread().getId()},cur_name: ${Thread.currentThread().getName()},cur_cid: ${id}")
    do {
      helper.c_prop.incrementAndGet()
      // 在执行过滤之前重置其为1

      //      if (runningStatus.get() == 0) {
      //        return
      //      }

      runningStatus.set(1)

      if (propagate()) {
        submitPropagtors()
      }

      // !!原子计算或临界区
      // 用锁机制获取最终状态
      //      lock.lock()
      //      try {
      //        if (runningStatus.get() > 1 && helper.isConsistent) {
      //          // 如果要运行的请求大于1， 循环需继续
      //          loopContinue = true
      //          runningStatus.set(1)
      //        } else {
      //          // 如果要运行的请求等于1，即说明只有当前任务，由于当前任务已经完成循环应退出
      //          loopContinue = false
      //          runningStatus.set(0)
      //        }
      //      }
      //      finally {
      //        lock.unlock()
      //      }

    } while (!runningStatus.compareAndSet(1, 0))
    helper.counter.decrementAndGet()
  }

  override def propagate(): Boolean = {
    if (initial()) {
      if (updateTable()) {
        if (filterDomains()) {
          return true
        }
      }
    }
    return false
  }

  override def newLevel(): Unit = {
    level += 1
    currTab.newLevel(level)
    // 重置runningStatus
    runningStatus.set(0)
  }

  override def backLevel(): Unit = {
    currTab.deleteLevel(level)
    level -= 1
    var i = 0
    while (i < arity) {
      localMask(i) = scope(i).simpleMask()
      lastMask(i) = localMask(i)
      i += 1
    }
    // 重置runningStatus
    runningStatus.set(0)
  }

}
