package cpscala.TSolver.Model.Constraint.SConstraint

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil.{Constants, INDEX, RSBitSet, SearchHelper}
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

class TableCT_Bit(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: SearchHelper) extends Propagator {
  val currTab = new RSBitSet(id, tuples.length, num_vars)
  val supports = new Array[Array[Array[Long]]](arity)
  val num_bit = currTab.num_bit
  val residues = new Array[Array[Int]](arity)
  level = 0

  for (i <- 0 until arity) {
    supports(i) = Array.ofDim[Long](scope(i).size, num_bit)
    residues(i) = Array.fill(scope(i).size)(-1)
  }

  var ii = 0
  while (ii < tuples.length) {
    //  for (i <- 0 until tuples.length) {
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

  // 获取最大论域大小
  var maxDomainSize = Int.MinValue
  scope.foreach(x => {
    maxDomainSize = math.max(maxDomainSize, x.size())
  })

  val var_num_bit = Math.ceil(maxDomainSize.toDouble / Constants.BITSIZE.toDouble).toInt

  // 局部变量标记
  // 2. localMask：当前调用时不断修改的论域的mask
  // 3. lastMask：上一次调用后的mask
  val localMask = Array.fill[Long](arity, var_num_bit)(0L)
  val lastMask = Array.fill[Long](arity, var_num_bit)(0L)
  val tmpMask = Array.fill[Long](var_num_bit)(0L)

  // 来存变量值
  val values = new ArrayBuffer[Int](maxDomainSize)
  values.clear()

  // 是否首次传播
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
    while (i < Sval.length) {
      val vv: Int = Sval(i)
      val v: Var = scope(vv)
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
        v.getValidValues(values)
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
        //println(s"update faild!!: ${Thread.currentThread().getName}, cid: ${id}")
        return false
      }

      i += 1
    }

    firstPropagate = false
    //    printf(s"      cid: %2d           after ut   table: ${currTab.words(helper.level)(0)}\n", id)
    //    printf(s"      cid: %2d           after ut   table: ${Constants.toFormatBinaryString(currTab.words(helper.level)(0))}\n", id)

    return true
  }

  def filterDomains(y: ArrayBuffer[Var]): Boolean = {
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
          return false
        }
        //更新lastMask
        v.mask(lastMask(vv))
        y += v
      }
    }

    return true
  }

  override def propagate(evt: ArrayBuffer[Var]): Boolean = {
    //L32~L33
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
      scope(i).mask(lastMask(i))
      i += 1
    }
  }

  override def stepUp(num_vars: Int): Unit = ???

  override def isEntailed(): Boolean = ???

  override def isSatisfied(): Unit = ???
}
