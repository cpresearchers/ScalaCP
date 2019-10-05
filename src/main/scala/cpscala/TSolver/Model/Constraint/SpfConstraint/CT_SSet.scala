package cpscala.TSolver.Model.Constraint.SpfConstraint

import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.CpUtil.SparseBitSet.RSBitSet
import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

class CT_SSet(val id: Int, val arity: Int, val numVars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: SearchHelper) extends Propagator[Var] {
  private[this] val currTab = new RSBitSet(id, tuples.length, numVars)
  private[this] val supports = new Array[Array[Array[Long]]](arity)
  private[this] val numBit = currTab.numBit
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

  val vals = new ArrayBuffer[Int]()

  //检查变量
  def initial(): Unit = {
    Ssup.clear()
    Sval.clear()

    var i = 0
    while (i < arity) {
      val v: Var = scope(i)
      if (v.unBind()) {
        //println("      Ssup add: " + v.id)
        Ssup += i
        v.getValidValues(domValues(i))
      }

      if (lastSize(i) != v.size()) {
        //println("      Sval add: " + v.id)
        Sval += i
        if ((lastSize(i) - v.size()) < v.size()) {
          v.getLastRemovedValues(lastSize(i), deltaValues(i))
        } else if (v.isLastBind()) {
          v.getValidValues(domValues(i))
        }
      }
      i += 1
    }
  }


  def updateTable(): Boolean = {
    ////println(s"id:${id}-----------ut----------")
    var i = 0
    val SvalNum = Sval.length
    while (i < SvalNum) {
      val vi: Int = Sval(i)
      val v: Var = scope(vi)
      currTab.clearMask()

      if ((lastSize(vi) - v.size()) < v.size()) {
        // delta更新表
        //println(s"  vid: ${v.id}, size:${deltaValues(vi).size}, getLastRemovedValues: ", deltaValues(vi).mkString(","))
        for (a <- deltaValues(vi)) {
          currTab.addToMask(supports(vi)(a))
        }
        currTab.reverseMask()
      } else {
        // dom更新表
        //println(s"  vid: ${v.id}, size:${domValues(vi).size}, validValues: ", domValues(vi).mkString(","))
        for (a <- domValues(vi)) {
          currTab.addToMask(supports(vi)(a))
        }
      }
      lastSize(vi) = v.size()

      currTab.intersectWithMask()
      //传播失败
      if (currTab.isEmpty()) {
        failWeight += 1
        //println(s"update faild!! cid: ${id}")
        return false
      }
      i += 1
    }
    true
  }

  def filterDomains(Yevt: ArrayBuffer[Var]): Boolean = {
    //println(s"id:${id}-----------fd----------")
    Yevt.clear()
    var i = 0
    val SsupNum = Ssup.length
    while (i < SsupNum) {
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
        Yevt += v
        if (v.isEmpty()) {
          failWeight += 1
          //println(s"filter faild!! cid: ${id}, vid: ${v.id}")
          return false
        }
      }
      i += 1
    }
    true
  }

  override def propagate(Yevt: ArrayBuffer[Var]): Boolean = {
    //println(s"c_id: ${id} propagate==========================>")
    val iniStart = System.nanoTime
    initial()
    val iniEnd = System.nanoTime
    helper.initialTime += iniEnd - iniStart

    val utStart = System.nanoTime
    val res = updateTable()
    val utEnd = System.nanoTime
    helper.updateTableTime += utEnd - utStart
    if (!res) {
      return false
    }

    val fiStart = System.nanoTime
    val fi = filterDomains(Yevt)
    val fiEnd = System.nanoTime
    helper.filterDomainTime += fiEnd - fiStart

    fi
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
