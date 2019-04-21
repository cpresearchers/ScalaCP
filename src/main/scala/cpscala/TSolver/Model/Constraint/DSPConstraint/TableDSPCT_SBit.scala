package cpscala.TSolver.Model.Constraint.DSPConstraint

import cpscala.TSolver.CpUtil.SearchHelper.DSPSearchHelper
import cpscala.TSolver.CpUtil.{Constants, INDEX, RSBitSet}
import cpscala.TSolver.Model.Variable.PVar

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class TableDSPCT_SBit(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[PVar], val tuples: Array[Array[Int]], val helper: DSPSearchHelper) extends DSPPropagator {
  val currTab = new RSBitSet(id, tuples.length, num_vars)
  val supports = new Array[Array[Array[Long]]](arity)
  val num_bit = currTab.num_bit
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

  val scopeMap = new mutable.HashMap[PVar, Int]()
  for (i <- 0 until arity) {
    scopeMap.put(scope(i), i)
  }

  //存变量Index
  val Ssup = new ArrayBuffer[Int](arity)
  val Sval = new ArrayBuffer[Int](arity)
  // 是否首次传播
  var firstPropagate = true

  // 获取最大论域大小
  var maxDomainSize = Int.MinValue
  scope.foreach(x => {
    maxDomainSize = math.max(maxDomainSize, x.size())
  })

  val var_num_bit = Math.ceil(maxDomainSize.toDouble / Constants.BITSIZE.toDouble).toInt

  // 局部变量标记
  // 1. newMask: 原子提交后获得的论域
  // 1. oldMask: 原子提交前获得的论域
  // 2. localMask：当前调用时不断修改的论域的mask
  // 3. lastMask：上一次调用后的mask
  //  val localMask = Array.fill[Long](arity)(0L)
  //  val lastMask = Array.fill[Long](arity)(Constants.ALLONELONG)
  val localMask = Array.fill[Long](arity, var_num_bit)(0L)
  //  val localMask = Array.tabulate(arity)(i => Array.fill[Long](scope(i)))
  val lastMask = Array.fill[Long](arity, var_num_bit)(0L)

  //  var ii = 0
  //  while (ii < arity) {
  //    scope(ii).mask(lastMask(ii))
  //    ii += 1
  //  }

  val globalMask = Array.fill[Long](var_num_bit)(0L)
  val tmpMask = Array.fill[Long](var_num_bit)(0L)

  val values = new ArrayBuffer[Int](maxDomainSize)
  values.clear()

  //检查变量
  def initial(): Boolean = {
    Ssup.clear()
    Sval.clear()
    // 标记SVal是否为空，为空则跳出propagate
    var snapshotChanged = false

    var i = 0
    while (i < arity) {
      var diff = false
      val v = scope(i)
      v.mask(localMask(i))

      // 本地论域快照与全局论域不同
      // 更新本地论域快照
      // snapshotChanged 即为需要propagate，否则不用propagate

      var j = 0
      while (j < var_num_bit) {
        if (lastMask(i)(j) != localMask(i)(j)) {
          diff = true
        }
        j += 1
      }

      if (diff) {
        Sval += i
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
    //    //println(s"      ut cid: ${id}===========>")
    var i = 0
    while (i < Sval.length && helper.isConsistent) {
      val vv: Int = Sval(i)
      val v: PVar = scope(vv)
      //      v.mask(localMask(vv))
      //println(s"cid: ${id}, vid: ${v.id}: localMask ${Constants.toFormatBinaryString(localMask(vv)(0))}")

      // 获得delta更新数据
      var numValid = 0
      var numRemoved = 0
      var j = 0
      while (j < var_num_bit) {
        tmpMask(j) = (~localMask(vv)(j)) & lastMask(vv)(j)
        numRemoved += java.lang.Long.bitCount(tmpMask(j))
        numValid += java.lang.Long.bitCount(localMask(vv)(j))
        lastMask(vv)(j) = localMask(vv)(j)
        j += 1
      }

      currTab.clearMask()
      if (numRemoved >= numValid || firstPropagate) {
        Constants.getValues(localMask(vv), values)
        for (a <- values) {
          currTab.addToMask(supports(vv)(a))
        }
      } else {
        Constants.getValues(tmpMask, values)
        // 重头重新
        for (a <- values) {
          currTab.addToMask(supports(vv)(a))
        }
        currTab.reverseMask()
      }

      val changed = currTab.intersectWithMask()

      //传播失败
      if (currTab.isEmpty()) {
        helper.isConsistent = false
        //println(s"update faild!!: ${Thread.currentThread().getName}, cid: ${id}")
        return false
      }

      i += 1
    }

    firstPropagate = false
    return true
  }

  def filterDomains(): Boolean = {
    //println(s"      fd cid: ${id}===========>")
    Xevt.clear()
    var i = 0
    while (i < Ssup.length && helper.isConsistent) {
      var deleted: Boolean = false
      val vv: Int = Ssup(i)
      val v = scope(vv)

      Constants.getValues(localMask(vv), values)

      for (a <- values) {
        if (id == 7) {
          //println(s"      cid: ${id} var: ${v.id} value: ${a} support: ${Constants.toFormatBinaryString(supports(vv)(a)(0))}")
        }
        var index = residues(vv)(a)
        if (index == -1 || (currTab.words(helper.level)(index) & supports(vv)(a)(index)) == 0L) { //res失效
          index = currTab.intersectIndex(supports(vv)(a))
          if (index != -1) { //重新找到支持
            residues(vv)(a) = index
          }
          else {
            deleted = true
            //无法找到支持, 删除(v, a)
            //println(s"      cons:${id} var:${v.id} remove new value:${a}")
            val (x, y) = INDEX.getXY(a)
            localMask(vv)(x) &= Constants.MASK0(y)
          }
        }
      }

      if (deleted) {
        val changed = v.submitMask(localMask(vv))
        // 本地线程删值
        // 提交更改，并获取新值
        if (v.isEmpty()) {
          helper.isConsistent = false
          //println(s"filter faild!!: ${Thread.currentThread().getName}, cid: ${id}, vid: ${v.id}")
          return false
        }

        if (changed) {
          Xevt += v
        }
        var j = 0
        while (j < var_num_bit) {
          lastMask(vv)(j) = localMask(vv)(j)
          j += 1
        }
      }
      i += 1
    }

    return true
  }

  def submitPropagtors(): Boolean = {
    //println(s"   cur_ID: ${Thread.currentThread().getId()} cur_cid: ${id}  submitPropa")

    // 提交其它约束
    for (x <- Xevt) {
      if (helper.isConsistent) {
        for (c <- helper.subscription(x.id)) {
          // !!这里可以加限制条件c.v.simpleMask!=x.simpleMask
          if (c.id != id) {
            helper.submitToPool(c)
          }
        }
      }
    }

    // 提交其它约束
    //
    //    for (vv <- Xevt) {
    //      val x = scope(vv)
    //      if (helper.isConsistent) {
    //        for (c <- helper.subscription(x.id)) {
    //          // !!这里可以加限制条件c.v.simpleMask!=x.simpleMask
    //          //          if (c.domainChanged(x)) {
    //          //          if (c.domainChanged(x, localMask(vv))) {
    //          //          if (c.id != id && c.domainChanged(x)) {
    //          if (c.id != id) {
    //            //            println(s"cid:${id}, vid: %${x.id}, changed: ${c.domainChanged(x)}")
    //            helper.submitToCevt(c)
    //            //          if (c.runningStatus.getAndIncrement() == 0) {
    //            ////            c_sub.incrementAndGet()
    //            //            c.reinitialize()
    //            //            c.fork()
    //          }
    //          //          }
    //          //          }
    //          //          }
    //        }
    //      }
    //    }
    return false
  }

  override def run(): Unit = {
    //    println(s"start: cur_ID: ${Thread.currentThread().getId()},cur_name: ${Thread.currentThread().getName()},cur_cid: ${id}")
    do {
      helper.c_prop.incrementAndGet()
      runningStatus.set(1)
      if (propagate()) {
        submitPropagtors()
      }
    } while (!runningStatus.compareAndSet(1, 0))
    helper.c_sub.decrementAndGet()
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
      scope(i).mask(lastMask(i))
      i += 1
    }
    // 重置runningStatus
    runningStatus.set(0)
  }

  override def domainChanged(v: PVar): Boolean = v.isChanged(localMask(scopeMap(v)))

  override def domainChanged(v: PVar, mask: Array[Long]): Boolean = {
    val index = scopeMap(v)
    var ii = 0
    while (ii < v.getNumBit()) {
      if (localMask(index)(ii) != mask(ii)) {
        return true
      }
      ii += 1
    }
    return false
  }
}
