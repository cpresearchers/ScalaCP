package cpscala.TSolver.Model.Constraint.SConstraint

import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.CpUtil.SearchHelper.{LMaxRPCSearchHelper, SearchHelper}
import cpscala.TSolver.Model.Variable.{BitSetVar, Var}

import scala.collection.mutable.ArrayBuffer

class LMaxRPC_BitRM(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: LMaxRPCSearchHelper) extends Propagator {

  // 获取所有变量的numbit
  val bitScope = Array.tabulate[BitSetVar](arity)(i => scope(i).asInstanceOf[BitSetVar])
  val numBits = Array.tabulate(arity)(i => Constants.getNumBit(scope(i).size()))
  val maxNumBits = numBits.max
  val bitSup = Array.tabulate(arity)(i => Array.ofDim[Long](scope(i).size(), maxNumBits))
  val lastPC = Array.tabulate(arity)(i => Array.fill(scope(i).size())(INDEX.kOVERFLOW))
  val lastAC = Array.tabulate(arity)(i => Array.fill(scope(i).size())(INDEX.kOVERFLOW))
  // 保存变量的有效值
  private[this] val values = new ArrayBuffer[Int]()


  for (t <- tuples) {
    val bIdx = (INDEX.getXY(t(0)), INDEX.getXY(t(1)))

    bitSup(0)(t(0))(bIdx._2._1) |= Constants.MASK1(bIdx._2._2)
    bitSup(1)(t(1))(bIdx._1._1) |= Constants.MASK1(bIdx._1._2)
  }


  override def propagate(evt: ArrayBuffer[Var]): Boolean = {
    //获取传入的两个变量
    val i = evt(0)
    val j = evt(1)
    //判断变量 i,j 的位置
    var iIdx: Int = 0
    var jIdx: Int = 1

    if (scope(0) == i) {
      iIdx = 0
      jIdx = 1
    } else {
      iIdx = 1
      jIdx = 0
    }

    i.getValidValues(values)


    return false
  }

  def havePCSupport(iIdx: Int, a: Int, jIdx: Int, b: Int): Boolean = {
    val lastPC_iaj = lastPC(iIdx)(a)

    if (lastPC_iaj != INDEX.kOVERFLOW && bitScope(jIdx).contains(lastPC_iaj)) {
      return true
    }

    //....

    return false
  }

  def havePCWit(iIdx: Int, a: Int, jIdx: Int, b: Int, k: BitSetVar): Boolean = {
    val i = bitScope(iIdx)
    val j = bitScope(jIdx)
    val c_ik = helper.commonCon(i.id)(k.id)(0)
    val c_jk = helper.commonCon(j.id)(k.id)(0)


    val iIdx_In_c_ik = c_ik.getVarIndex(i)
    val jIdx_In_c_jk = c_jk.getVarIndex(j)

    val d = c_ik.lastAC(iIdx_In_c_ik)(a)
    if (d != INDEX.kOVERFLOW) {
      val aa = c_ik.bitSup(iIdx_In_c_ik)(a)(d)
      val bb = c_jk.bitSup(jIdx_In_c_jk)(b)(d)
      val cc = k.bitDoms(k.level)(d)

      if ((aa & bb & cc) != 0L) {
        return true
      }
    }

    val e = c_jk.lastAC(jIdx_In_c_jk)(b)
    if (e != INDEX.kOVERFLOW) {
      val aa = c_ik.bitSup(iIdx_In_c_ik)(a)(e)
      val bb = c_jk.bitSup(jIdx_In_c_jk)(b)(e)
      val cc = k.bitDoms(k.level)(e)

      if ((aa & bb & cc) != 0L) {
        return true
      }
    }

    var f = 0
    while (f < k.numBit) {
      val aa = c_ik.bitSup(iIdx_In_c_ik)(a)(f)
      val bb = c_jk.bitSup(jIdx_In_c_jk)(b)(f)
      val cc = k.bitDoms(k.level)(f)

      if ((aa & bb & cc) != 0L) {
        c_ik.lastAC(iIdx_In_c_ik)(a) = f
        c_jk.lastAC(jIdx_In_c_jk)(b) = f
        return true
      }

      f += 1
    }

    return false
  }


  def nextSupportBit(iIdx: Int, a: Int, jIdx: Int, v: Int): Int = {
    // 若传入的v已越界
    if (v > bitScope(jIdx).capacity - 1 || v == INDEX.kOVERFLOW) {
      return INDEX.kOVERFLOW
    }

    val (x, y) = INDEX.getXY(v)
    val j = bitScope(jIdx)
    val b = (bitSup(iIdx)(a)(x) & j.bitDoms(j.level)(x)) >> y

    if (b != 0L) {
      return v + Constants.FirstLeft(b)
    }

    var u = x + 1
    while (u < j.numBit) {
      val mask = bitSup(iIdx)(a)(u) & j.bitDoms(j.level)(u)
      if (mask != 0) {
        return INDEX.getIndex(u, Constants.FirstLeft(mask))
      }
      u += 1
    }

    return INDEX.kOVERFLOW
  }

  //  def getLastAC(v: Var, a: Int): Int = {
  //    val vIdx = if (scope(0).id == v.id) 0 else 1
  //    return lastAC(vIdx)(a)
  //  }
  //
  //  def setLastAC(v: Var, a: Int, b: Int): Unit = {
  //    val vIdx = if (scope(0).id == v.id) 0 else 1
  //    lastAC(vIdx)(a) = b
  //  }
  //
  //  def getBitSup(v: Var, a: Int, index: Int): Int = {
  //    val vIdx = if (scope(0).id == v.id) 0 else 1
  //    return lastAC(vIdx)(a)
  //  }
  //
  //  def setLastAC(v: Var, a: Int, b: Int): Unit = {
  //    val vIdx = if (scope(0).id == v.id) 0 else 1
  //    lastAC(vIdx)(a) = b
  //  }

  def getVarIndex(v: Var): Int = {
    if (scope(0).id == v.id) 0 else 1
  }

  override def newLevel(): Unit = ???

  override def backLevel(): Unit = ???

  override def stepUp(num_vars: Int): Unit = ???

  override def isEntailed(): Boolean = ???

  override def isSatisfied(): Unit = ???
}
