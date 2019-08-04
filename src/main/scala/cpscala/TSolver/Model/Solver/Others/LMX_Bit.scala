package cpscala.TSolver.Model.Solver.Others

import cpscala.TSolver.CpUtil.{Constants, INDEX, RSBitSet}
import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.{BitSetVar, Var}

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

class LMX_Bit(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[BitSetVar_LMX], val tuples: Array[Array[Int]], val helper: LMXSearchHelper, val parallelism: Int) {
  // 获取所有变量的numbit
  var level = 0
  var assignedCount = 0
  val numBits = Array.tabulate(arity)(i => Constants.getNumBit(scope(i).size()))
  val maxNumBits = numBits.max
  val bitSup = Array.tabulate(arity)(i => Array.ofDim[Long](scope(i).size(), maxNumBits))
  //  // 带线程id
  //  val lastPC = Array.tabulate(parallelism)(Array.tabulate(arity)(i => Array.fill(scope(i).size())(INDEX.kOVERFLOW)))
  //  val lastAC = Array.tabulate(parallelism)(Array.tabulate(arity)(i => Array.fill(scope(i).size())(INDEX.kOVERFLOW)))
  val values = new ArrayBuffer[Int]()


  //  println(s"cid: ${id}")

  for (t <- tuples) {
    //    println(t.mkString(","))
    val bIdx = (INDEX.getXY(t(0)), INDEX.getXY(t(1)))
    //    println(s"bitSup(0)(${t(0)})(${bIdx._2._1}) = ${bitSup(0)(t(0))(bIdx._2._1).toBinaryString}")
    //    println(s"bitSup(1)(${t(1)})(${bIdx._1._1}) = ${bitSup(1)(t(1))(bIdx._1._1).toBinaryString}")
    bitSup(0)(t(0))(bIdx._2._1) |= Constants.MASK1(bIdx._2._2)
    bitSup(1)(t(1))(bIdx._1._1) |= Constants.MASK1(bIdx._1._2)
    //    println(s"bitSup(0)(${t(0)})(${bIdx._2._1}) = ${bitSup(0)(t(0))(bIdx._2._1).toBinaryString}")
    //    println(s"bitSup(1)(${t(1)})(${bIdx._1._1}) = ${bitSup(1)(t(1))(bIdx._1._1).toBinaryString}")
  }

  //  for (kk <- 0 until 2) {
  //    val vv = scope(kk)
  //    for (aa <- 0 until vv.size()) {
  //      println(s"bitSup(${vv.id})($aa)(0) = ${bitSup(kk)(aa)(0).toHexString}")
  //    }
  //  }

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

  // 变量的比特组个数
  private[this] val varNumBit: Array[Int] = Array.tabulate[Int](arity)(i => scope(i).getNumBit())
  // lastMask与变量Mask不同的值是该约束两次传播之间被过滤的值（delta）
  private[this] val lastMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  // 在约束传播开始时localMask获取变量最新的mask
  private[this] val localMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  // 记录该约束两次传播之间删值的mask
  private[this] val removeMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  //  // 保存delta或者变量的剩余有效值
  //  private[this]

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
    var i = 0
    while (i < Sval.length && helper.isConsistent) {
      val vv: Int = Sval(i)
      val v: Var = scope(vv)
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
        v.getValidValues(values)
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

  def filterDomains(y: ArrayBuffer[BitSetVar_LMX]): Boolean = {
    y.clear()

    var i = 0
    while (i < Ssup.length && helper.isConsistent) {
      var deleted: Boolean = false
      val vv: Int = Ssup(i)
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
            //            println(s"      cons:${id} var:${v.id} remove new value:${a}")
            println(s"ac  remove: (${v.id},${a})")
            v.remove(a)
          }
        }
      }

      if (deleted) {
        // 论域删空退出
        if (v.isEmpty()) {
          //          println("ac field")
          helper.isConsistent = false
          //println(s"filter faild!!: ${Thread.currentThread().getName}, cid: ${id}, vid: ${v.id}")
          return false
        }
        //更新lastMask
        v.mask(lastMask(vv))
        y += v
      }

      i += 1
    }


    return true
  }

  def AC(evt: ArrayBuffer[BitSetVar_LMX]): Boolean = {
    //L32~L33
    initial()
    //    val utStart = System.nanoTime
    val res = updateTable()
    //    val utEnd = System.nanoTime
    //    helper.updateTableTime += utEnd - utStart
    if (!res) {
      helper.isConsistent = false
      return false
    }

    //    val fiStart = System.nanoTime
    val fi = filterDomains(evt)
    //    val fiEnd = System.nanoTime
    //    helper.filterDomainTime += fiEnd - fiStart
    return fi
  }

  // 等几个线程
  def LMX(evt: ArrayBuffer[BitSetVar_LMX], m: MultiLevel): (Boolean, Boolean) = {
    //    println("enter c.lmx")
    //获取传入的两个变量
    val i = evt(0)
    val j = evt(1)
    //    println(s"${i.id}, ${j.id}")
    //判断变量 i,j 的位置
    // 保存变量的有效值
    val lmxValues = new ArrayBuffer[Int]()
    evt.clear()
    var changed = false
    val (iIdx, jIdx) = if (scope(0) == i) (0, 1) else (1, 0)
    i.getValidValues(lmxValues, m)

    for (a <- lmxValues) {

      // 若主线程已完成
      if (helper.ACFinished) {
        return (true, changed)
      }

      if (!helper.isConsistent) {
        return (false, changed)
      }

      //      println(s"have_pc_support（${i.id}, ${a}, ${j.id})")
      if (!havePCSupport(iIdx, a, jIdx, m)) {
        if (i.contains(a)) {
          //          println(s"lmx remove: (${i.id},${a})")
          i.remove(a)
        }
        i.remove(a, m)
        changed = true

        if (i.isEmpty(m)) {
          //          println("lmx field")
          helper.isConsistent = false
          return (false, changed)
        }

        if (i.isEmpty()) {
          //          println("ac field")
          helper.isConsistent = false
          return (false, changed)
        }

        evt += i
      }
    }

    return (true, changed)
  }

  // 等几个线程
  def LMXAsync(evt: ArrayBuffer[BitSetVar_LMX], m: MultiLevel): (Boolean, Boolean) = {
    //    println("enter c.lmx")
    //获取传入的两个变量
    val i = evt(0)
    val j = evt(1)
    //    println(s"${i.id}, ${j.id}")
    //判断变量 i,j 的位置
    // 保存变量的有效值
    val lmxValues = new ArrayBuffer[Int]()
    evt.clear()
    var changed = false
    val (iIdx, jIdx) = if (scope(0) == i) (0, 1) else (1, 0)
    i.getValidValues(lmxValues, m)

    for (a <- lmxValues) {
      // 需要停下来了，一般是由外部通知
      if (helper.States(m) != LCState.Running) {
        helper.States(m) == LCState.Stopped
        return (true, changed)
      }

      //      println(s"have_pc_support（${i.id}, ${a}, ${j.id})")
      if (!havePCSupport(iIdx, a, jIdx, m)) {
        if (i.contains(a)) {
          println(s"lmx remove main value: (${i.id},${a}), at ${m.toString()}")
          i.remove(a)
        }
        println(s"lmx remove sub value: (${i.id},${a}), at ${m.toString()}")
        i.remove(a, m)
        changed = true

        if (i.isEmpty(m)) {
          //          println("lmx field")
          helper.States(m) == LCState.Fail
          return (false, changed)
        }

        if (i.isEmpty()) {
          //          println("ac field")
          helper.States(m) == LCState.Fail
          return (false, changed)
        }

        evt += i
      }
    }
    helper.States(m) == LCState.Success
    return (true, changed)
  }

  def havePCSupport(iIdx: Int, a: Int, jIdx: Int, m: MultiLevel): Boolean = {
    //    val lastPC_iaj = lastPC(iIdx)(a)
    val i = scope(iIdx)
    val j = scope(jIdx)

    //    if (lastPC_iaj != INDEX.kOVERFLOW && scope(jIdx).contains(lastPC_iaj)) {
    //      return true
    //    }

    val v = j.minValue()

    var b = nextSupportBit(iIdx, a, jIdx, v, m)

    while (b != INDEX.kOVERFLOW) {
      var pcWitness = true
      breakable {
        for (k <- helper.commonVar(i.id)(j.id)) {
          if (k.unBind(m.searchLevel)) {
            if (!havePCWit(iIdx, a, jIdx, b, k, m)) {
              pcWitness = false
              break()
            }
          }
        }
      }

      if (pcWitness) {
        //        lastPC(iIdx)(a) = b
        //        lastPC(jIdx)(b) = a
        //        lastAC(iIdx)(a) = b / Constants.BITSIZE
        return true
      }

      b = nextSupportBit(iIdx, a, jIdx, b + 1, m)
    }


    //....

    return false
  }

  def havePCWit(iIdx: Int, a: Int, jIdx: Int, b: Int, k: BitSetVar_LMX, m: MultiLevel): Boolean = {
    val i = scope(iIdx)
    val j = scope(jIdx)
    val c_ik = helper.commonCon(i.id)(k.id)(0)
    val c_jk = helper.commonCon(j.id)(k.id)(0)


    val iIdx_In_c_ik = c_ik.getVarIndex(i)
    val jIdx_In_c_jk = c_jk.getVarIndex(j)

    //    val d = c_ik.lastAC(iIdx_In_c_ik)(a)
    //    if (d != INDEX.kOVERFLOW) {
    //      val aa = c_ik.bitSup(iIdx_In_c_ik)(a)(d)
    //      val bb = c_jk.bitSup(jIdx_In_c_jk)(b)(d)
    //      val cc = k.bitDoms(m.tmpLevel)(d)
    //
    //      if ((aa & bb & cc) != 0L) {
    //        return true
    //      }
    //    }
    //
    //    val e = c_jk.lastAC(jIdx_In_c_jk)(b)
    //    if (e != INDEX.kOVERFLOW) {
    //      val aa = c_ik.bitSup(iIdx_In_c_ik)(a)(e)
    //      val bb = c_jk.bitSup(jIdx_In_c_jk)(b)(e)
    //      val cc = k.bitDoms(m.tmpLevel)(e)
    //
    //      if ((aa & bb & cc) != 0L) {
    //        return true
    //      }
    //    }

    var f = 0
    while (f < k.numBit) {
      val aa = c_ik.bitSup(iIdx_In_c_ik)(a)(f)
      val bb = c_jk.bitSup(jIdx_In_c_jk)(b)(f)
      val cc = k.bitDoms(m.tmpLevel)(f)

      if ((aa & bb & cc) != 0L) {
        //        c_ik.lastAC(iIdx_In_c_ik)(a) = f
        //        c_jk.lastAC(jIdx_In_c_jk)(b) = f
        return true
      }

      f += 1
    }

    return false
  }

  def nextSupportBit(iIdx: Int, a: Int, jIdx: Int, v: Int, m: MultiLevel): Int = {
    // 若传入的v已越界
    if (v > scope(jIdx).capacity - 1 || v == INDEX.kOVERFLOW) {
      return INDEX.kOVERFLOW
    }

    val (x, y) = INDEX.getXY(v)
    val j = scope(jIdx)
    //左小，向左移，右小向右移
    val b = (bitSup(iIdx)(a)(x) & j.bitDoms(m.tmpLevel)(x)) << y

    if (b != 0L) {
      return v + Constants.FirstLeft(b)
    }

    var u = x + 1
    while (u < j.numBit) {
      val mask = bitSup(iIdx)(a)(u) & j.bitDoms(m.tmpLevel)(u)
      if (mask != 0) {
        return INDEX.getIndex(u, Constants.FirstLeft(mask))
      }
      u += 1
    }

    return INDEX.kOVERFLOW
  }

  def getVarIndex(v: Var): Int = {
    if (scope(0).id == v.id) 0 else 1
  }


  def newLevel(): Unit = {
    level += 1
    currTab.newLevel(level)
  }

  def backLevel(): Unit = {
    currTab.deleteLevel(level)
    level -= 1
    var i = 0
    while (i < arity) {
      scope(i).mask(lastMask(i))
      i += 1
    }
  }

  def backLevel(a: Int): Unit = {
    currTab.deleteLevel(a)
    level -= 1
    var i = 0
    while (i < arity) {
      scope(i).mask(lastMask(i), a)
      i += 1
    }
  }
}
