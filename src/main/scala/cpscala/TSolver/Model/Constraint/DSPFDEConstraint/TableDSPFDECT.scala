package cpscala.TSolver.Model.Constraint.DSPFDEConstraint

import cpscala.TSolver.CpUtil.{Constants, FDEBitSet, INDEX}
import cpscala.TSolver.CpUtil.SearchHelper.{DSPFDESearchHelper, FDESearchHelper}
import cpscala.TSolver.Model.Constraint.DSPConstraint.DSPPropagator
import cpscala.TSolver.Model.Constraint.FDEConstrain.FDEPropagator
import cpscala.TSolver.Model.Variable.{PVar, Var}

import scala.collection.mutable.ArrayBuffer

class TableDSPFDECT(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[PVar], val tuples: Array[Array[Int]], val helper: DSPFDESearchHelper) extends DSPPropagator {

  val currTab = new FDEBitSet(id, tuples.length, num_vars)
  val supports = new Array[Array[Array[Long]]](arity - 1)
  val num_bit = currTab.numBit
  val residues = new Array[Array[Int]](arity)
  level = 0

  // 活动变量
  val Xevt = new ArrayBuffer[PVar](arity)
  Xevt.clear()

  for (i <- 0 until arity - 1) {
    supports(i) = Array.ofDim[Long](scope(i).size, num_bit)
    residues(i) = Array.fill(scope(i).size)(-1)
  }
  var ii = 0
  while (ii < tuples.length) {
    //  for (i <- 0 until tuples.length) {
    val (x, y) = INDEX.getXY(ii)
    val t = tuples(ii)
    for (j <- 0 until t.length - 1) {
      supports(j)(t(j))(x) |= Constants.MASK1(y)
    }
    ii += 1
  }

  //存变量Index
  val Ssup = new ArrayBuffer[Int](arity)
  val Sval = new ArrayBuffer[Int](arity)

  // 变量的比特组个数
  private[this] val varNumBit: Array[Int] = Array.tabulate[Int](arity)(i => scope(i).getNumBit())
  // lastMask与变量Mask不同的值是该约束两次传播之间被过滤的值（delta）
  private[this] val lastMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  // 在约束传播开始时localMask获取变量最新的mask
  private[this] val localMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  // 记录该约束两次传播之间删值的mask
  private[this] val removeMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  // 保存delta或者变量的剩余有效值
  private[this] val values = new ArrayBuffer[Int]()

  // 是否首次传播
  var firstPropagate = true

  //检查变量
  def initial(): Boolean = {
    Ssup.clear()
    Sval.clear()
    // 标记SVal是否为空，为空则跳出propagate
    var snapshotChanged = false

    var i = 0
    while (i < arity - 1) {
      var diff = false
      val v = scope(i)
      scopeStamp(i) = helper.pVarStamp.get(v.id)
      v.mask(localMask(i))

      // 本地论域快照与全局论域不同
      // 更新本地论域快照
      // snapshotChanged 即为需要propagate，否则不用propagate
      var j = 0
      while (j < varNumBit(i)) {
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
    currTab.intersectWord(helper.vcMap(id).getAtomicBitDom())

    var i = 0
    while (i < Sval.length && helper.isConsistent) {
      val vv: Int = Sval(i)
      //      val v: PVar = scope(vv)
      //      v.mask(localMask(vv))
      //println(s"cid: ${id}, vid: ${v.id}: localMask ${Constants.toFormatBinaryString(localMask(vv)(0))}")

      // 获得delta更新数据
      var numValid = 0
      var numRemoved = 0
      var j = 0
      while (j < varNumBit(vv)) {
        removeMask(vv)(j) = 0L
        removeMask(vv)(j) = (~localMask(vv)(j)) & lastMask(vv)(j)
        numRemoved += java.lang.Long.bitCount(removeMask(vv)(j))
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
        Constants.getValues(removeMask(vv), values)
        // 重头重新
        for (a <- values) {
          currTab.addToMask(supports(vv)(a))
        }
        currTab.reverseMask()
      }

      val changed = currTab.intersectWithMask()

      //传播失败
      if (currTab.isEmpty()) {
        //println(s"update faild!!: ${Thread.currentThread().getName}, cid: ${id}")
        helper.isConsistent = false
        failWeight += 1
        return false
      }

      i += 1
    }

    firstPropagate = false
    return true
  }

  def filterDomains(y: ArrayBuffer[PVar]): Boolean = {
    y.clear()
    for (vv <- Ssup) {
      var deleted: Boolean = false
      val v = scope(vv)
      v.getValidValues(values)

      for (a <- values) {
        var index = residues(vv)(a)
        if (index == Constants.kINDEXOVERFLOW || (currTab.words(helper.level)(index) & supports(vv)(a)(index)) == 0L) { //res失效
          index = currTab.intersectIndex(supports(vv)(a))
          if (index != -1) { //重新找到支持
            residues(vv)(a) = index
          }
          else {
            deleted = true
            //无法找到支持, 删除(v, a)
            //println(s"      cons:${id} var:${v.id} remove new value:${a}")
            v.remove(a)
          }
        }
      }

      if (deleted) {
        // 论域删空退出
        if (v.isEmpty()) {
          //println(s"filter faild!!: ${Thread.currentThread().getName}, cid: ${id}, vid: ${v.id}")
          failWeight += 1
          return false
        }
        //更新lastMask
        v.mask(lastMask(vv))
        y += v
      }
    }

    return true
  }

  def filterDomains(): Boolean = {
    Xevt.clear()

    var i = 0
    while (i < Ssup.length && helper.isConsistent) {
      var deleted: Boolean = false
      val vv: Int = Ssup(i)
      val v = scope(vv)
      //      v.getValidValues(values)
      Constants.getValues(localMask(vv), values)

      for (a <- values) {
        var index = residues(vv)(a)
        if (index == Constants.kINDEXOVERFLOW || (currTab.words(helper.level)(index) & supports(vv)(a)(index)) == 0L) { //res失效
          index = currTab.intersectIndex(supports(vv)(a))
          if (index != -1) { //重新找到支持
            residues(vv)(a) = index
          }
          else {
            deleted = true
            //无法找到支持, 删除(v, a)
            //println(s"      cons:${id} var:${v.id} remove new value:${a}")
            //            v.remove(a)
            val (x, y) = INDEX.getXY(a)
            localMask(vv)(x) &= Constants.MASK0(y)
          }
        }
      }

      if (deleted) {
        val (changed, same) = v.submitMaskAndIsSame(localMask(vv))
        if (changed) {
          helper.pVarStamp.set(v.id, helper.pGlobalStamp.incrementAndGet())
          if (same) {
            scopeStamp(vv) = helper.pVarStamp.get(v.id)
          }
          // 本地线程删值
          if (v.isEmpty()) {
            helper.isConsistent = false
            failWeight += 1
            //println(s"filter faild!!: ${Thread.currentThread().getName}, cid: ${id}, vid: ${v.id}")
            return false
          }

          Xevt += v
        }
        var j = 0
        while (j < varNumBit(vv)) {
          lastMask(vv)(j) = localMask(vv)(j)
          j += 1
        }
      }

      i += 1
    }

    return true
  }

  //
  //  override def propagate(evt: ArrayBuffer[Var]): Boolean = {
  //    //println(s"${id} cons starts ------------------>")
  //    //L32~L33
  //    initial()
  //    //        val utStart = System.nanoTime
  //    val res = updateTable()
  //    //        val utEnd = System.nanoTime
  //    //        helper.updateTableTime += utEnd - utStart
  //    if (!res) {
  //      return false
  //    }
  //
  //    //        val fiStart = System.nanoTime
  //    val fi = filterDomains(evt)
  //    //        val fiEnd = System.nanoTime
  //    //        helper.filterDomainTime += fiEnd - fiStart
  //    if (helper.vcMap(id).removeValues(currTab.getWord())) {
  //      if (helper.vcMap(id).size() == 0) {
  //        return false
  //      }
  //      else {
  //        evt += helper.vcMap(id)
  //      }
  //
  //    }
  //    //    println(id+"   "+helper.vcMap(id).id+"   "+helper.vcMap(id).size()+"  "+evt.size)
  //    return fi
  //  }

  override def propagate(): Boolean = {
    //println(s"${id} cons starts ------------------>")
    //L32~L33
    val ini = initial()
    if (!ini) {
      helper.notChangedTabs.incrementAndGet()
      return false
    }
    //        val utStart = System.nanoTime
    val res = updateTable()
    //        val utEnd = System.nanoTime
    //        helper.updateTableTime += utEnd - utStart
    if (!res) {
      return false
    }

    //        val fiStart = System.nanoTime
    //    val fi = filterDomains(Xevt)
    val fi = filterDomains()
    //        val fiEnd = System.nanoTime
    //        helper.filterDomainTime += fiEnd - fiStart
    if (helper.vcMap(id).removeValues(currTab.getWord())) {
      if (helper.vcMap(id).size() == 0) {
        return false
      }
      else {
        Xevt += helper.vcMap(id)
      }

    }
    //    println(id+"   "+helper.vcMap(id).id+"   "+helper.vcMap(id).size()+"  "+evt.size)
    return fi
  }

  override def newLevel(): Unit = {
    level += 1
    currTab.newLevel(level)
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
    runningStatus.set(0)
  }

  override def stepUp(num_vars: Int): Unit = ???

  override def isEntailed(): Boolean = ???

  override def isSatisfied(): Unit = ???

  def submitPropagtors(): Boolean = {
    //println(s"   cur_ID: ${Thread.currentThread().getId()} cur_cid: ${id}  submitPropa")

    // 提交其它约束
    for (x <- Xevt) {
      if (helper.isConsistent) {
        for (c <- helper.subscription(x.id)) {
          val index = c.scopeMap(x.id)
          if (c.id != id && helper.pVarStamp.get(x.id) > c.scopeStamp(index)) {
            //          if (c.id != id) {
            helper.submitToPool(c)
          }
        }
      }
    }

    return false
  }

  override def run(): Unit = {
    //    println(s"${id} start  ----- cur_ID: ${Thread.currentThread().getId()}")
    do {
      helper.c_prop.incrementAndGet()
      runningStatus.set(1)
      if (propagate() && Xevt.nonEmpty) {
        submitPropagtors()
      }
    } while (!runningStatus.compareAndSet(1, 0))
    helper.counter.decrementAndGet()
    //    println(s"${id} end-----")
  }
}

