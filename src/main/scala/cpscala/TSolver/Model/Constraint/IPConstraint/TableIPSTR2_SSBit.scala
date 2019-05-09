package cpscala.TSolver.Model.Constraint.IPConstraint

import cpscala.TSolver.CpUtil.SearchHelper.IPSearchHelper
import cpscala.TSolver.CpUtil._
import cpscala.TSolver.Model.Variable.PVar

import scala.collection.mutable.ArrayBuffer

class TableIPSTR2_SSBit(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[PVar], val tuples: Array[Array[Int]], val helper: IPSearchHelper) extends IPPropagator {
  val position = Array.range(0, tuples.length)
  // 局部
  val gacValue = new Array[SingleBitSet](arity)

  val levelLimits = Array.fill(num_vars + 1)(-1)
  levelLimits(0) = tuples.length - 1
  //存变量Index
  val Ssup = new ArrayBuffer[Int](arity)
  val Sval = new ArrayBuffer[Int](arity)
  //  val lastsize = Array.fill(arity)(-1)
  //替代lastsize若它与v.simpleMask不一样则需要加入Sval
  val lastMask = Array.fill(arity)(0L)
  level = 0

  // 初始化gacvalue
  var ii = 0
  while (ii < arity) {
    val v = scope(ii)
    gacValue(ii) = new SingleBitSet(v.size())
    lastMask(ii) = v.simpleMask()
    ii += 1
  }

  //检查变量
  def initial(): Unit = {
    Ssup.clear()
    Sval.clear()

    var i = 0
    while (i < arity) {
      val v = scope(i)
      gacValue(i).clear()

      val mask = v.simpleMask()
      if (lastMask(i) != mask) {
        Sval += i
        lastMask(i) = mask
      }

      if (v.unBind()) {
        Ssup += i
      }
      i += 1
    }
  }

  // 这里的值加到gacValue中
  def updateTable(): Boolean = {
    var i = levelLimits(level)
    while (i >= 0) {
      if (!helper.isConsistent) {
        return false
      }
      val index = position(i)
      val t = tuples(index)

      if (isValidTuple(t)) {
        var j = 0
        while (j < Ssup.length) {
          val vv = Ssup(j)
          val v = scope(vv)
          val a = t(vv)

          gacValue(vv).add(a)
          // 应该比较lastmask而不是当前的v.simplemask，单线程是可以比的，但是多线程不可以
          //          if (gacValue(vv).mask() == v.simpleMask()){
          if (gacValue(vv).mask() == lastMask(vv)) {
            //            printf("remove from cid: %d, var: %d\n", id, v.id)
            val lastPos = Ssup.length - 1
            //先将Ssup的最后一个元素复制到当前j位置
            Ssup(j) = Ssup(lastPos)
            //再将最后一个元素删除，这样能节约时间
            Ssup.remove(lastPos)
            j -= 1
          }
          j += 1
        }
      } else {
        //        println("id: " + id + ", remove: " + t.mkString(","))
        removeTuples(i, level)
        // 只有改动表的时候才会改动时间戳
        helper.tabStamp(id) = helper.globalStamp
      }
      i -= 1
    }
    true
  }

  def filterDomains(): Boolean = {
    var i = 0
    val ssupN = Ssup.length

    while (i < ssupN) {
      val vv: Int = Ssup(i)
      val v = scope(vv)
      val mask = gacValue(vv).mask()
      val newMask = v.submitMaskAndGet(mask)
      //      printf("cid: %d, var: %d, fd:%64s~%64s~%64s\n", id, v.id, mask.toBinaryString, newMask.toBinaryString, lastMask(vv).toBinaryString)

      // 变量论域改变，时间戳更新为全局时间戳+1
      //      if (lastMask(vv) != newMask) {
      //      val newMask = v.simpleMask()
      if (lastMask(vv) != newMask) {
        // 论域若被修改，则全局时间戳加1
        helper.varStamp(v.id) = helper.globalStamp + 1
        // 论域为空返回false
        // 这里用newmask不行
        // 一定要从变量取得
        if (v.simpleMask() == 0L) {
          //          println(s"fail: cid: ${id}, vid:${v.id}")
          helper.isConsistent = false
          failWeight += 1
          return false
        }

        //更新变量在该约束内的lastmask
        //        lastMask(vv) = newMask
        lastMask(vv) = mask
      }
      i += 1
    }
    return true
  }

  // !!这里查看全局的变量值是否存在，而没有用快照的，不影响算法正确性
  // !!我们在保证并行程序无错的情况下 尽量加速算法效率
  def isValidTuple(t: Array[Int]): Boolean = {
    for (vidx <- Sval) {
      if (!scope(vidx).contains(t(vidx))) {
        return false
      }
    }
    return true
  }

  def removeTuples(i: Int, p: Int): Unit = {
    val tmp = position(i)
    position(i) = position(levelLimits(p))
    position(levelLimits(p)) = tmp
    levelLimits(p) -= 1
  }

  override def propagate(): Boolean = {
    initial()
    if (!updateTable())
      return false
    return filterDomains()
  }

  override def newLevel(): Unit = {
    //      currentLimit = levelLimits(tabLevel)
    levelLimits(level + 1) = levelLimits(level)
    level += 1
  }

  override def backLevel(): Unit = {
    levelLimits(level) = -1
    level -= 1
    for (i <- 0 until arity) {
      lastMask(i) = scope(i).simpleMask()
    }
  }

  def show(): Unit = {
    //      println()
  }

  def call(): Boolean = {
    //    println(s"start: cur_ID: ${Thread.currentThread().getId()},cur_name: ${Thread.currentThread().getName()},cur_cid: ${id}")
    //    if (!helper.isConsistent) {
    //      return false
    //    }
    val res = propagate()
    //    println(s"end:   cur_ID: ${Thread.currentThread().getId()},cur_name: ${Thread.currentThread().getName()},cur_cid: ${id},propagate_res: ${res}")
    return res
  }
}
