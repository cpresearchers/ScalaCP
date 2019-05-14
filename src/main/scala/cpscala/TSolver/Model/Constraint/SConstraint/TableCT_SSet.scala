package cpscala.TSolver.Model.Constraint.SConstraint

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil.{Constants, INDEX, RSBitSet}
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

class TableCT_SSet(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: SearchHelper) extends Propagator[Var] {
  val currTab = new RSBitSet(id, tuples.length, num_vars)
  val supports = new Array[Array[Array[Long]]](arity)
  val num_bit = currTab.numBit
  val residues = new Array[Array[Int]](arity)
  level = 0

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

  //存变量Index
  val Ssup = new ArrayBuffer[Int](arity)
  val Sval = new ArrayBuffer[Int](arity)
  //  val lastSize = for (v <- scope) yield v.size()
  val lastSize = Array.fill(arity)(-1)
  //  val oldSize = for (v <- scope) yield v.size()
  // 第一次运行时要对所有变量值进行检查。所以这里将设为最大，delta就最大
  val oldSize = Array.fill[Long](arity)(0x3f3f3f3f)
  //  val oldMask = Array.fill[Long](arity)(Long.MaxValue)
  //  removedValues(delta)或lastValues
  val vals = new ArrayBuffer[Int]()

  //检查变量
  def initial(): Unit = {
    Ssup.clear()
    Sval.clear()

    var i = 0
    while (i < arity) {
      val v: Var = scope(i)
      if (lastSize(i) != v.size()) {
        //println("      Sval add: " + v.id)
        Sval += i
        lastSize(i) = v.size()
      }

      if (v.unBind()) {
        //println("      Ssup add: " + v.id)
        Ssup += i
      }
      i += 1
    }
  }

  def updateTable(): Boolean = {
    //    //println(s"id:${id}-----------ut----------")
    var i = 0
    val SvalN = Sval.length
    while (i < SvalN) {
      val vv: Int = Sval(i)
      val v: Var = scope(vv)
      currTab.clearMask()

      // !!此处delta重写了一次
      vals.clear()
      if ((oldSize(vv) - v.size()) < (v.size())) {
        // delta更新
        v.getLastRemovedValues(oldSize(vv), vals)
        //                //println(s"cid: ${id}, vid: ${v.id}, getLastRemovedValues: ", vals.mkString(","))
        for (a <- vals) {
          currTab.addToMask(supports(vv)(a))
        }
        currTab.reverseMask()
      } else {
        // 重头重新
        v.getValidValues(vals)
        //                //println(s"cid: ${id}, vid: ${v.id}, validValues: ", vals.mkString(","))
        for (a <- vals) {
          currTab.addToMask(supports(vv)(a))
        }
      }
      val changed = currTab.intersectWithMask()

      //传播失败
      if (currTab.isEmpty()) {
        failWeight += 1
        //println(s"update faild!! cid: ${id}")
        return false
      }
      i += 1
    }
    //    printf(s"      cid: %2d           after ut   table: ${currTab.words(helper.level)(0)}\n", id)
    //    printf(s"      cid: %2d           after ut   table: ${Constants.toFormatBinaryString(currTab.words(helper.level)(0))}\n", id)
    return true
  }

  def filterDomains(y: ArrayBuffer[Var]): Boolean = {
    //    //println(s"id:${id}-----------fd----------")
    y.clear()
    var i = 0
    val SsupN = Ssup.length
    while (i < SsupN) {
      var deleted: Boolean = false
      val vv: Int = Ssup(i)
      val v = scope(vv)

      vals.clear()
      v.getValidValues(vals)
      //      //println(s"cid: ${id}, vid: ${v.id}, validValues: ", v.validValues.mkString(","))
      for (a <- vals) {
        //        //println(s"      cid: ${id} var: ${v.id} value: ${a} support: ${Constants.toFormatBinaryString(supports(vv)(a)(0))}")
        var index = residues(vv)(a)
        if (index == -1 || (currTab.words(helper.level)(index) & supports(vv)(a)(index)) == 0L) { //res失效
          index = currTab.intersectIndex(supports(vv)(a))
          if (index != -1) { //重新找到支持
            residues(vv)(a) = index
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
        lastSize(vv) = v.size()
        oldSize(vv) = v.size()

        y += v
        if (v.isEmpty()) {
          failWeight += 1
          //println(s"filter faild!! cid: ${id}, vid: ${v.id}")
          return false
        }
      }
      i += 1
    }
    return true
  }

  override def propagate(evt: ArrayBuffer[Var]): Boolean = {
    //println(s"c_id: ${id} propagate==========================>")
    initial()
    val utStart = System.nanoTime
    val res = updateTable()
    val utEnd = System.nanoTime
    helper.updateTableTime += utEnd - utStart
    if (!res) {
      return false
    }

    val fiStart = System.nanoTime
    val fi = filterDomains(evt)
    val fiEnd = System.nanoTime
    helper.filterDomainTime += fiEnd - fiStart
    return fi
  }

  override def newLevel(): Unit = {
    level += 1
    currTab.newLevel(level)
  }

  override def backLevel(): Unit = {
    currTab.deleteLevel(level)
    level -= 1
    var i = 0
    while (i < arity) {
      lastSize(i) = scope(i).size()
      oldSize(i) = lastSize(i)
      i += 1
    }
  }

  override def stepUp(num_vars: Int): Unit = ???

  override def isEntailed(): Boolean = ???

  override def isSatisfied(): Unit = ???
}
