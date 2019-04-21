package cpscala.TSolver.Model.Constraint.SConstraint

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil._
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

class TableSTR2_SSet(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: SearchHelper) extends Propagator {
  val position = Array.range(0, tuples.length)

  val levelLimits = Array.fill(num_vars + 1)(-1)
  levelLimits(0) = tuples.length - 1
  //存变量Index
  val Ssup = new ArrayBuffer[Int](arity)
  val Sval = new ArrayBuffer[Int](arity)
  val lastSize = Array.fill(arity)(-1)
//  private[this] val removeValues = new ArrayBuffer[Int]()
//  private[this] val validValues = new ArrayBuffer[Int]() //(delta)

  //检查变量
  def initial(): Unit = {
    //println("c_id: " + id + " propagate ==============================>")
    Ssup.clear()
    Sval.clear()

    var i = 0
    while (i < arity) {
      val v: Var = scope(i)
      v.clearMark()
//      validValues.clear()
//      v.getValidValues(validValues)
      //println(s"       var: ${v.id} validValues: " + validValues.mkString(", "))

      if (lastSize(i) != v.size) {
        if (lastSize(i) != -1) {
//          removeValues.clear()
//          v.getLastRemovedValues(lastSize(i).toLong, removeValues)
          //println(s"       var: ${v.id} removedValues: " + removeValues.mkString(", "))
        }
        //          //println("name: " + name + ", add: " + i + " to sval")
        Sval += i
        lastSize(i) = v.size
      }

      if (v.unBind()) {
        //          //println("name: " + name + ", add: " + v.name + " to ssup")
        Ssup += i
      }
      i += 1
    }
  }

  def updateTable(): Unit = {
    var i = levelLimits(level)
    var tnum = 0
    while (i >= 0) {
      val index = position(i)
      val t = tuples(index)
      //      //println(s"id: $id, isValidTuple(${t.mkString(",")}): ", isValidTuple(t))
      if (isValidTuple(t)) {
        var j = 0
        while (j < Ssup.length) {
          val vv = Ssup(j)
          val v = scope(vv)
          val a = t(vv)
          v.mark(a)
          if (v.fullMark()) {
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
        //println(s"       invalid tuple is ${index}: " + t.mkString(","))
        removeTuples(i, level)
        tnum += 1
      }
      i -= 1
    }
    //println(s"       the number of invalid tuple: ${tnum}")
  }

  def filterDomains(evt: ArrayBuffer[Var]): Boolean = {
    //    val y = new ArrayBuffer[Var](arity)
    var i = 0
    val ssupN = Ssup.length

    while (i < ssupN) {
      val vv: Int = Ssup(i)
      val v = scope(vv)

      //利用变量中的Mark，Restrict()只需要O(1)的时间复杂度便可更新v的论域
      v.restrict()
      if (v.size == 0) {
        evt += v
        return false
      }

      //更新变量在该约束内的lastsize
      lastSize(vv) = v.size()
      evt += v

      i += 1
    }
    return true
  }

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

  override def propagate(evt: ArrayBuffer[Var]): Boolean = {
    initial()
    updateTable()
    return filterDomains(evt)
  }

  override def newLevel(): Unit = {
    level += 1
    levelLimits(level) = levelLimits(level - 1)
  }

  override def backLevel(): Unit = {
    levelLimits(level) = -1
    level -= 1
    var i = 0
    while (i < arity) {
      lastSize(i) = scope(i).size()
      i += 1
    }
  }

  def show(): Unit = {
    //      //println()
  }

  override def stepUp(num_vars: Int): Unit = ???

  override def isEntailed(): Boolean = ???

  override def isSatisfied(): Unit = ???
}
