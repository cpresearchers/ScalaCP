package cpscala.TSolver.Model.Constraint.IPbitConstraint

import cpscala.TSolver.CpUtil.{Constants, INDEX, RSBitSet}
import cpscala.TSolver.CpUtil.SearchHelper.{IPbit2SearchHelper, IPbitSearchHelper}
import cpscala.TSolver.Model.Variable.PVar

import scala.collection.mutable.ArrayBuffer

//class TableIPtmpCT_SBit(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[PVar], val tuples: Array[Array[Int]], val helper: IPbitSearchHelper) extends IPbitPropagator {
class TableIPtmpCT_SBit(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[PVar], val tuples: Array[Array[Int]], val helper: IPbit2SearchHelper) extends IPbitPropagator {
  val currTab = new RSBitSet(id, tuples.length, num_vars)
  val supports = new Array[Array[Array[Long]]](arity)
  val num_bit = currTab.num_bit
  val residues = new Array[Array[Int]](arity)

  for (i <- 0 until arity) {
    supports(i) = Array.ofDim[Long](scope(i).size, num_bit)
    residues(i) = Array.fill(scope(i).size)(-1)
  }

  var ii = 0
  while (ii < tuples.length) {
    val (x, y) = INDEX.getXY(ii)
    val t = tuples(ii)

    for (j <- 0 until t.length) {
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

  // 标识是否为初次传播。初次传播updateTable时利用valid更新，非初次传播updateTable时根据比较结果确定。
  var firstPropagate = true

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

      //      本地论域快照与全局论域不同
      //      更新本地论域快照
      //      snapshotChanged 即为需要propagate，否则不用propagate
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
    //    println(s"      ut cid: ${id}===========>")
    var i = 0
    while (i < Sval.length && helper.isConsistent) {
      val vv: Int = Sval(i)
      val v: PVar = scope(vv)
      //      v.mask(localMask(vv))

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
    var i = 0
    while (i < Ssup.length && helper.isConsistent) {
      var deleted: Boolean = false
      val vv: Int = Ssup(i)
      val v = scope(vv)

      Constants.getValues(localMask(vv), values)

      for (a <- values) {
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
        if(v.submitMask(localMask(vv))){
          helper.varStamp(v.id) = helper.globalStamp + 1
          // 本地线程删值
          if (v.isEmpty()) {
            helper.isConsistent = false
            //println(s"filter faild!!: ${Thread.currentThread().getName}, cid: ${id}, vid: ${v.id}")
            return false
          }
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
  }

  override def backLevel(): Unit = {
    currTab.deleteLevel(level)
    level -= 1
    var i = 0
    while (i < arity) {
      scope(i).mask(lastMask(i))
      i += 1
    }
  }

  override def call(): Unit = {
        println(s"${id} start  ----- cur_ID: ${Thread.currentThread().getId()}")
    if (helper.isConsistent) {
      propagate()
    }
    helper.numSubCons.decrementAndGet()
    //    println(s"${id} end-----")
  }

}


