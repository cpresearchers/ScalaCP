package cpscala.TSolver.Model.Constraint.IPConstraint

import cpscala.TSolver.CpUtil.SearchHelper.IPSearchHelper
import cpscala.TSolver.CpUtil._
import cpscala.TSolver.Model.Variable.PVar

import scala.collection.mutable.ArrayBuffer

class TableIPCT_SSBit(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[PVar], val tuples: Array[Array[Int]], val helper: IPSearchHelper) extends IPPropagator {
  val currTab = new RSBitSet(id, tuples.length, num_vars)
  val supports = new Array[Array[Array[Long]]](arity)
  val num_bit = currTab.num_bit
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

  // 获取最大论域大小
  var maxDomainSize = Int.MinValue
  scope.foreach(x => {
    maxDomainSize = math.max(maxDomainSize, x.size())
  })

  // 局部变量标记
  //  val gacValue = new Array[SingleBitSet](arity)
  val newMask = Array.fill[Long](arity)(0L)
  val oldMask = Array.fill[Long](arity)(Constants.ALLONELONG)

  val lastRemovedValues = new ArrayBuffer[Int](maxDomainSize)
  lastRemovedValues.clear()
  val validValues = new ArrayBuffer[Int](maxDomainSize)
  lastRemovedValues.clear()

  // 标识是否为初次传播。初次传播updateTable时利用valid更新，非初次传播updateTable时根据比较结果确定。
  var firstProp = true
  //  // 初始化gacValue，lastMask
  //  var ii = 0
  //  while (ii < arity) {
  //    val v = scope(ii)
  //    //    gacValue(ii) = new SingleBitSet(v.size())
  //    lastMask(ii) = v.simpleMask()
  //    ii += 1
  //  }

  //检查变量
  def initial(): Unit = {
    Ssup.clear()
    Sval.clear()

    var i = 0
    while (i < arity) {
      val v = scope(i)
      newMask(i) = v.simpleMask()

      if (newMask(i) != oldMask(i)) {
        Sval += i
      }

      if (v.unBind()) {
        Ssup += i
      }
      i += 1
    }
  }

  //返回true为delta更新，false从头更新
  @inline def getValues(vidx: Int, v: PVar): Boolean = {
    val lastRemovedMask: Long = (~newMask(vidx)) & oldMask(vidx)
    val valid = java.lang.Long.bitCount(newMask(vidx))
    val remove = java.lang.Long.bitCount(lastRemovedMask)

    // 标记是否需要delta更新
    val needDelta: Boolean = remove < valid
    if (needDelta) {
      // delta更新
      var i = 0
      lastRemovedValues.clear()
      while (i < v.capacity) {
        if ((lastRemovedMask & Constants.MASK1(i)) != 0L) {
          lastRemovedValues += i
        }
        i += 1
      }
    } else {
      // 从头计算
      var i = 0
      validValues.clear()
      while (i < v.capacity) {
        if ((newMask(vidx) & Constants.MASK1(i)) != 0L) {
          validValues += i
        }
        i += 1
      }
    }

    return needDelta
  }

  def updateTable(): Boolean = {
    //    //println(s"id:${id}-----------ut----------")
    var i = 0
    val SvalN = Sval.length
    while (i < SvalN && helper.isConsistent) {
      val vv: Int = Sval(i)
      val v: PVar = scope(vv)
      // 因为initial之后还可能有值被删除
      newMask(vv) = v.simpleMask()
      currTab.clearMask()

      val lastRemovedMask: Long = (~newMask(vv)) & oldMask(vv)
      val valid = java.lang.Long.bitCount(newMask(vv))
      val remove = java.lang.Long.bitCount(lastRemovedMask)

      // 标记是否需要delta更新
      if (remove < valid && !firstProp) {
        // delta更新
        lastRemovedValues.clear()
        var j = 0
        while (j < v.capacity) {
          if ((lastRemovedMask & Constants.MASK1(j)) != 0L) {
            lastRemovedValues += j
            //            currTab.addToMask(supports(vv)(j))
          }
          j += 1
        }

        //        //println(s"cid: ${id}, vid: ${v.id}, lastRemovedValues: ", lastRemovedValues.mkString(","))
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
          if ((newMask(vv) & Constants.MASK1(j)) != 0L) {
            validValues += j
            //            currTab.addToMask(supports(vv)(j))
          }
          j += 1
        }
        //        //println(s"cid: ${id}, vid: ${v.id}, validValues: ", validValues.mkString(","))
        // 重头重新
        for (a <- validValues) {
          currTab.addToMask(supports(vv)(a))
        }
      }

      val changed = currTab.intersectWithMask()

      //传播失败
      if (currTab.isEmpty()) {
        helper.isConsistent = false
        return false
      }
      i += 1
    }
    // 首次传播updateTable完成
    firstProp = false
    return true
  }

  def filterDomains(): Boolean = {
    //    //println(s"id:${id}-----------fd----------")
    val SsupN = Ssup.length
    var i = 0
    while (i < SsupN && helper.isConsistent) {
      var deleted: Boolean = false
      val vv: Int = Ssup(i)
      val v = scope(vv)

      validValues.clear()
      var j = 0
      while (j < v.capacity) {
        if ((newMask(vv) & Constants.MASK1(j)) != 0L) {
          validValues += j
        }
        j += 1
      }

      //      //println(s"cid: ${id}, vid: ${v.id}, validValues: ", validValues.mkString(","))
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
            newMask(vv) &= Constants.MASK0(a)
            //            //println("name: " + id + ", delete: " + v.id + "," + a + ", level: " + helper.level)
          }
        }
      }


      if (deleted) {
        helper.varStamp(v.id) = helper.globalStamp + 1
        v.submitMaskAndGet(newMask(vv))

        if (v.isEmpty()) {
          helper.isConsistent = false
          return false
        }

        // 这里不能等于newMask因为表是基于lastMask更新的
        oldMask(vv) = newMask(vv)

        //        if (lastMask(vv) != v.simpleMask()) {
        //          conti = true
        //        }
      }
      i += 1
    }

    return true
  }

  override def propagate(): Boolean = {
    //    conti = true
    //    while (conti) {
    //      conti = false
    initial()
    if (!updateTable()) {
      return false
    }
    if (!filterDomains()) {
      return false
    }
    //    }
    return true
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
      oldMask(i) = scope(i).simpleMask()
      i += 1
    }
  }

  override def call(): Boolean = {
    if (helper.isConsistent) {
      //    //println(s"start: cur_ID: ${Thread.currentThread().getId()},cur_name: ${Thread.currentThread().getName()},cur_cid: ${id}
      return propagate()
      //    //println(s"end:   cur_ID: ${Thread.currentThread().getId()},cur_name: ${Thread.currentThread().getName()},cur_cid: ${id},propagate_res: ${res}")
    } else {
      return false
    }
//    if (!helper.isConsistent) {
//      return false
//    }
//
//    helper.searchState match {
//      case 0 => {
//        //println("setup")
//        setup()
//      };
//      case 1 => {
//        //println("newLevel")
//        newLevel()
//      };
//      case 2 => {
//        //println("propagate")
//        propagate()
//      };
//      case 3 => {
//        //println("backLevel")
//        backLevel()
//      };
//    }
//
//    return true
  }


}
