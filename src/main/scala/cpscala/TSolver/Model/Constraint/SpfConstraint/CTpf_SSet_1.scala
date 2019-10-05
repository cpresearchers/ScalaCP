package cpscala.TSolver.Model.Constraint.SpfConstraint

import java.util.concurrent.atomic.{AtomicInteger}

import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil.SparseBitSet.PRSBitSet
import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

class CTpf_SSet_1(val id: Int, val arity: Int, val numVars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: SearchHelper) extends Propagator[Var] {
  outer =>
  private[this] val currTab = new PRSBitSet(id, tuples.length, numVars)
  private[this] val supports = new Array[Array[Array[Long]]](arity)
  private[this] val numBit = currTab.numBit
  // 记录RSBitSet中words变为0的位置
  private[this] val Izero = new ArrayBuffer[Int](numBit)
  private[this] val residues = new Array[Array[Int]](arity)
  private[this] var level = 0

  for (vv <- 0 until arity) {
    supports(vv) = Array.ofDim[Long](scope(vv).size(), numBit)
    residues(vv) = Array.fill(scope(vv).size())(-1)
  }

  for (i <- tuples.indices) {
    val (x, y) = INDEX.getXY(i)
    val t = tuples(i)
    for (j <- t.indices) {
      supports(j)(t(j))(x) |= Constants.MASK1(y)
    }
  }

  //存变量Index
  private[this] val Sval = new ArrayBuffer[Int](arity)
  private[this] val Ssup = new ArrayBuffer[Int](arity)
  // 第一次运行时要使用所有变量值更新表。所以这里将设为最大，delta就最大。
  private[this] val lastSize = Array.fill[Long](arity)(0x3f3f3f3f)
  // 上次执行CT至本次执行CT之间各变量的删值集合
  private[this] val deltaValues = Array.fill(arity)(new ArrayBuffer[Int])
  // 变量的当前论域
  private[this] val domValues = Array.fill(arity)(new ArrayBuffer[Int])


  // 线程状态为0：初始化
  private[this] val initialState: Int = 0
  // 线程状态为1：更新表
  private[this] val updateState: Int = 1
  // 线程状态为2：过滤论域
  private[this] val filterState: Int = 2

  // max是创建线程数
  val maxParallel: Int = 2
  private[this] val threads: Array[pfThread] = Array.tabulate(maxParallel)(i => new pfThread(i, initialState))
  // active是活跃线程数
  private[this] var activeParallel: Int = 0
  private[this] val activeThread = new AtomicInteger(0)

  // 各线程独立的变量集
  private[this] val pSval = Array.fill(maxParallel)(new ArrayBuffer[Int])
  private[this] val pSsup = Array.fill(maxParallel)(new ArrayBuffer[Int])
  private[this] val pIzero = Array.fill(maxParallel)(new ArrayBuffer[Int])
  private[this] val pYevt = Array.fill(maxParallel)(new ArrayBuffer[Var])

  class pfThread(private[this] val tid: Int, private[this] var state: Int) extends Thread {

    def setState(state: Int): Unit = {
      this.state = state
    }

    // 初始化
    def pinitial(): Unit = {
      pSsup(tid).clear()
      pSval(tid).clear()

      // 划分
      val step = arity.toFloat / activeParallel
      var i = (tid * step).toInt
      val end = ((tid + 1) * step).toInt
      while (i < end) {
        val v: Var = scope(i)
        if (v.unBind()) {
          //          //println(s"      pSsup${i} add: ${v.id}")
          pSsup(tid) += i
          v.getValidValues(domValues(i))
        }

        if (lastSize(i) != v.size()) {
          //          //println(s"      pSval${i} add: ${v.id}")
          pSval(tid) += i
          if ((lastSize(i) - v.size()) < v.size()) {
            v.getLastRemovedValues(lastSize(i), deltaValues(i))
          } else if (v.isLastBind()) {
            v.getValidValues(domValues(i))
          }
        }
        i += 1
      }
    }

    def puptadeTable(): Unit = {
      //println(s"id:${id}-----------ut----------")
      pIzero(tid).clear()
      var i = 0
      val SvalNum = Sval.length
      while (i < SvalNum) {
        val vi: Int = Sval(i)
        val v: Var = scope(vi)
        currTab.clearMask(tid, activeParallel)

        if ((lastSize(vi) - v.size()) < v.size()) {
          //          if(tid == 1){
          //println(s"  vid: ${v.id}, size:${deltaValues(vi).size}, getLastRemovedValues: ", deltaValues(vi).mkString(","))
          //          }
          // delta更新表
          for (a <- deltaValues(vi)) {
            currTab.addToMask(supports(vi)(a), tid, activeParallel)
          }
          currTab.reverseMask(tid, activeParallel)
        } else {
          //          if(tid == 1){
          //println(s"  vid: ${v.id}, size:${domValues(vi).size}, validValues: ", domValues(vi).mkString(","))
          //          }
          // dom更新表
          for (a <- domValues(vi)) {
            currTab.addToMask(supports(vi)(a), tid, activeParallel)
          }
        }
        currTab.intersectWithMask(tid, activeParallel, pIzero(tid))
        i += 1
      }
    }

    def pfilterDomain(): Unit = {
      //println(s"id:${id}-----------fd----------")
      pYevt(tid).clear()
      // 划分
      val step = Ssup.length.toFloat / activeParallel
      var i = (tid * step).toInt
      val end = ((tid + 1) * step).toInt
      while (i < end && helper.isConsistent) {
        var deleted: Boolean = false
        val vi: Int = Ssup(i)
        val v = scope(vi)

        //      //println(s"cid: ${id}, vid: ${v.id}, validValues: ", v.validValues.mkString(","))
        for (a <- domValues(vi)) {
          var index = residues(vi)(a)
          if (index == -1 || (currTab.words(helper.level)(index) & supports(vi)(a)(index)) == 0L) {
            //res失效
            index = currTab.intersectIndex(supports(vi)(a))
            if (index != -1) {
              //重新找到支持
              residues(vi)(a) = index
            }
            else {
              deleted = true
              //无法找到支持, 删除(v, a)
              v.remove(a)
              //println(s"      var:${v.id} remove new value:${a}")
            }
          }
        }

        if (deleted) {
          lastSize(vi) = v.size()
          pYevt(tid) += v
          if (v.isEmpty()) {
            failWeight += 1
            //println(s"filter faild!! cid: ${id}, vid: ${v.id}")
            helper.isConsistent = false
          }
        }
        i += 1
      }
    }

    override def run(): Unit = {
      state match {
        case outer.initialState => pinitial()

        case outer.updateState => puptadeTable()

        case outer.filterState => pfilterDomain()
      }
      activeThread.decrementAndGet()
    }
  }

  def initial(): Unit = {
    Sval.clear()
    Ssup.clear()
    activeParallel = if (arity < maxParallel) arity else maxParallel
    activeThread.set(activeParallel)
    for (i <- 0 until activeParallel) {
      threads(i).setState(initialState)
      threads(i).run()
    }

    while (activeThread.get() != 0) {}

    for (i <- 0 until activeParallel) {
      Sval ++= pSval(i)
      Ssup ++= pSsup(i)
    }
  }

  def updateTable(): Unit = {
    Izero.clear()
    val limit = currTab.limit(level) + 1
    activeParallel = if (limit < maxParallel) limit else maxParallel
    activeThread.set(activeParallel)
    for (i <- 0 until activeParallel) {
      threads(i).setState(updateState)
      threads(i).run()
    }

    while (activeThread.get() != 0) {}

    val joinStart = System.nanoTime
    for (vi <- Sval) {
      lastSize(vi) = scope(vi).size()
    }

    for (i <- 0 until activeParallel) {
      Izero ++= pIzero(i)
    }

    //传播失败
    if (currTab.isEmpty(Izero)) {
      failWeight += 1
      //println(s"update faild!! cid: ${id}")
      helper.isConsistent = false
    }
    val joinEnd = System.nanoTime
    helper.joinTime += joinEnd - joinStart
  }

  def filterDomains(Yevt: ArrayBuffer[Var]): Unit = {
    Yevt.clear()
    val length = Ssup.length
    activeParallel = if (length < maxParallel) length else maxParallel
    activeThread.set(activeParallel)
    for (i <- 0 until activeParallel) {
      threads(i).setState(filterState)
      threads(i).run()
    }

    while (activeThread.get() != 0) {}

    for (i <- 0 until activeParallel) {
      Yevt ++= pYevt(i)
    }
  }

  override def propagate(Yevt: ArrayBuffer[Var]): Boolean = {
    //println(s"c_id: ${id} propagate==========================>")

    helper.isConsistent = true

    val iniStart = System.nanoTime
    initial()
    val iniEnd = System.nanoTime
    helper.initialTime += iniEnd - iniStart

    val utStart = System.nanoTime
    updateTable()
    val utEnd = System.nanoTime
    helper.updateTableTime += utEnd - utStart
    if (!helper.isConsistent) {
      return false
    }

    val fiStart = System.nanoTime
    filterDomains(Yevt)
    val fiEnd = System.nanoTime
    helper.filterDomainTime += fiEnd - fiStart

    helper.isConsistent
  }

  override def newLevel(): Unit = {
    level += 1
    currTab.newLevel()
  }

  override def backLevel(): Unit = {
    currTab.backLevel()
    level -= 1
    var i = 0
    while (i < arity) {
      lastSize(i) = scope(i).size()
      i += 1
    }
  }
}
