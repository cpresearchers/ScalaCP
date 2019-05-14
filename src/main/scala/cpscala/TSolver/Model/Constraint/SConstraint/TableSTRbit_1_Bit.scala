package cpscala.TSolver.Model.Constraint.SConstraint

import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

class TableSTRbit_1_Bit(val id: Int, val arity: Int, val numVars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: SearchHelper) extends Propagator[Var] {

  // 比特子表，三维数组，第一维变量，第二维取值，第三维元组
  // 初始化变量时，其论域已经被序列化，诸如[0, 1, ..., var.size()]，所以可以直接用取值作为下标
  private[this] val bitTables = Array.tabulate(arity)(i => new Array[Array[BitSupport]](scope(i).size()))
  // 分界符，二维数组，第一维变量，第二维取值
  private[this] val last = Array.tabulate(arity)(i => new Array[Int](scope(i).size()))
  // 分界符栈
  // 在搜索树初始层，若变量值的last改变了，即更新变量栈顶层的Array（后来想了想，0层不需要保存，因为1层对应的栈顶保存的即是0层初始化GAC后的信息）
  // 在搜索树的非初始层，当变量值的last第一次发生改变时，将改变前的last值保存在该变量栈顶层Array中
  private[this] val lastLevel = Array.fill[Array[Array[Int]]](numVars + 1)(Array.tabulate(arity)(i => Array.fill[Int](scope(i).size())(-1)))

  private[this] val lengthTuple = tuples.length
  // 比特元组的数量，tupleLength不能被64整除，要为余数创建一个比特元组
  private[this] val numBit = Math.ceil(lengthTuple.toDouble / Constants.BITSIZE.toDouble).toInt
  // 比特元组的集合，比特元组的每个比特位记录对应位置的元组是否有效
  private[this] val bitVal = Array.fill[Long](numBit)(-1L)
  // 最后一个比特元组末尾清0
  bitVal(numBit - 1) <<= Constants.BITSIZE - lengthTuple % Constants.BITSIZE
  // 比特元组栈
  // 在搜索树初始层，若比特元组改变了，即更新栈顶层的Array（后来想了想，0层不需要保存，因为1层对应的栈顶保存的即是0层初始化GAC后的信息）
  // 在搜索树的非初始层，当比特元组第一次发生改变时，将改变前的比特元组保存在栈顶层Array中
  private[this] val bitLevel = Array.fill[Long](numVars + 1, numBit)(-1L)
  bitLevel(0)(numBit - 1) <<= Constants.BITSIZE - lengthTuple % Constants.BITSIZE

  // 变量的比特组个数
  private[this] val varNumBit: Array[Int] = Array.tabulate[Int](arity)(i => scope(i).getNumBit())
  // lastMask与变量Mask不同的值是该约束两次传播之间被过滤的值（delta）
  private[this] val lastMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  // 在约束传播开始时localMask获取变量最新的mask
  private[this] val localMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  // 记录该约束两次传播之间删值的mask
  private[this] val removeMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  // delta
  private[this] val removeValues = new ArrayBuffer[Int]()
  // 变量的剩余有效值
  private[this] val validValues = new ArrayBuffer[Int]()

  // 初始化标志变量
  // isInitial为false说明setup中还未初始化完成数据结构
  // 为true说明初始化数据结构完成，可以进行初始删值
  private[this] var isInitial = false

  override def setup(): Boolean = {

    if (!isInitial) {
      //      println(s"cons: ${id} setup ===============>")

      val tempBitTable = Array.tabulate(arity)(i => {
        Array.fill(scope(i).size())(new ArrayBuffer[BitSupport]())
      })


      // 向临时子表内动态添加元组编号
      var t = 0
      while (t < lengthTuple) {
        if (isValidTuple(tuples(t))) {
          // ts为比特元组下标
          val ts = t / 64
          // index为第ts个比特元组中元组的位置
          val index = t % 64
          var i = 0
          while (i < arity) {
            val a = tuples(t)(i)
            // 利用折半查找使得变量值的比特支持按序号递增排列
            val bitSupportsArray = tempBitTable(i)(a)

            var low = 0
            var high = bitSupportsArray.length - 1
            var middle = 0
            var find = false
            breakable {
              while (low <= high) {
                middle = (low + high) / 2
                if (ts == bitSupportsArray(middle).ts) {
                  bitSupportsArray(middle).mask |= Constants.MASK1(index)
                  find = true
                  break
                } else if (ts < bitSupportsArray(middle).ts) {
                  high = middle - 1
                } else {
                  low = middle + 1
                }
              }
            }
            if (!find) {
              val loc = high + 1
              val bitSupport = new BitSupport(ts, Constants.MASK1(index))
              bitSupportsArray.insert(loc, bitSupport)
            }
            i += 1
          }
        }
        t += 1
      }

      var i = 0
      while (i < arity) {
        val v = scope(i)
        // 初始化lastMask
        v.mask(lastMask(i))
        // 因为变量还未删值，所以j既为index，又为取值
        var j = v.size()
        while (j > 0) {
          j -= 1
          val tempBitSupports = tempBitTable(i)(j)
          bitTables(i)(j) = tempBitSupports.toArray
//          last(i)(j) = tempBitSupports.length - 1
          lastLevel(level)(i)(j) = tempBitSupports.length - 1
        }
        i += 1
      }
      // 初始化数据结构完成
      isInitial = true
      return true
    }
    else {
      var i = 0
      while (i < arity) {
        val v = scope(i)
        // j既为取值，又为下标
        var j = v.size()
        while (j > 0) {
          j -= 1
          if (bitTables(i)(j).isEmpty) {
            v.remove(j)
            helper.varStamp(v.id) = helper.globalStamp
            //println(s"       var:${x.id} remove new value:${value}")
          }
        }
        if (v.isEmpty()) {
          return false
        }
        i += 1
      }
    }
    return true
  }

  // 删除无效元组
  def deleteInvalidTuple(): Unit = {

    var i = 0
    while (i < arity) {
      val v = scope(i)
      v.mask(localMask(i))

      // 根据新旧mask的比较确定是否有删值
      var diff = false
      var j = 0
      while (j < varNumBit(i)) {
        // 需先将removeMask清空，如果不清空，那么遇到lastMask和localMask相等的情况，removeMask仍然维持原样，若原样非全0，则会出错
        removeMask(i)(j) = 0L
        // 根据新旧mask的比较确定是否有删值
        if (lastMask(i)(j) != localMask(i)(j)) {
          removeMask(i)(j) = (~localMask(i)(j)) & lastMask(i)(j)
          lastMask(i)(j) = localMask(i)(j)
          diff = true
        }
        j += 1
      }

      if (diff) {
        // 若有删值，则获得delta
        Constants.getValues(removeMask(i), removeValues)
        // 寻找新的无效元组
        for (a <- removeValues) {
          //          val old = last(i)(a)
          val old = lastLevel(level)(i)(a)
          val bitSupports = bitTables(i)(a)

          for (l <- 0 to old) {
            val ts = bitSupports(l).ts
            //            val u = bitSupports(l).mask & bitVal(ts)
            val tmpBit = bitLevel(level)
            val u = bitSupports(l).mask & tmpBit(ts)
            // 与结果非0，说明bit为1的位置对应的元组变为无效
            if (u != 0L) {
              // 将第一次改变之前的比特元组记录下来
              //              if (bitLevel(level)(ts) == 0L) {
              //                bitLevel(level)(ts) = bitVal(ts)
              //              }
              // 更新比特元组
              //              bitVal(ts) &= ~u
              tmpBit(ts) &= ~u
            }
          }
        }
      }
      i += 1
    }
  }

  // 寻找没有支持的值
  def searchSupport(evt: ArrayBuffer[Var]): Boolean = {

    var i = 0
    while (i < arity) {
      val v = scope(i)

      if (v.unBind()) {
        var deleted = false
        v.getValidValues(validValues)

        for (a <- validValues) {
          val bitSupports = bitTables(i)(a)
          //          val old = last(i)(a)
          val tmp = lastLevel(level)
          val old = tmp(i)(a)

          // 寻找支持的比特元组
          var now = old

          //          while (now >= 0 && (bitSupports(now).mask & bitVal(bitSupports(now).ts)) == 0L) {
          val tmpBit = bitLevel(level)
          while (now >= 0 && (bitSupports(now).mask & tmpBit(bitSupports(now).ts)) == 0L) {
            now -= 1
          }

          if (now == -1) {
            deleted = true
            v.remove(a)
//                        println(s"    cur_cid: ${id}, var: ${v.id}, remove val: ${a}")
          } else {
            if (now != old) {
              // 将第一次改变之前的last记录下来
//              if (lastLevel(level)(i)(a) == -1) {
//                lastLevel(level)(i)(a) = old
//              }
//              last(i)(a) = now
              tmp(i)(a) = now
            }
          }
        }
        if (deleted) {
          if (v.isEmpty()) {
            failWeight += 1
            return false
          }
          //更新lastMask
          v.mask(lastMask(i))
          evt += v
        }
      }
      i += 1
    }
    return true
  }

  override def propagate(evt: ArrayBuffer[Var]): Boolean = {

    //    val ditStart = System.nanoTime
    deleteInvalidTuple()
    //    val ditEnd = System.nanoTime
    //    helper.updateTableTime += ditEnd - ditStart

    //    val ssStart = System.nanoTime
    val ss = searchSupport(evt)
    //    val ssEnd = System.nanoTime
    //    helper.filterDomainTime += ssEnd - ssStart

    return ss
  }

  // 新层
  def newLevel(): Unit = {
    val oldLevel = level
    level += 1
    var i = 0
    while (i < arity) {
      scope(i).getValidValues(validValues)
      for (a <- validValues) {
        lastLevel(level)(i)(a) = lastLevel(oldLevel)(i)(a)
      }
      i += 1
    }

    for (ts <- 0 until numBit) {
      bitLevel(level)(ts) = bitLevel(oldLevel)(ts)
    }
    // 到达新层后不用更改lastMask，lastMask与上层保持一致
  }

  // 回溯
  def backLevel(): Unit = {
    for (i <- 0 until arity) {
//      for (a <- 0 until scope(i).capacity) {
//        if (lastLevel(level)(i)(a) != -1) {
//          last(i)(a) = lastLevel(level)(i)(a)
//          lastLevel(level)(i)(a) = -1
//        }
//      }
      // 回溯后重置lastMask，新旧mask相同，因为还没有传播
      scope(i).mask(lastMask(i))
    }

//    for (ts <- 0 until numBit) {
//      if (bitLevel(level)(ts) != 0L) {
//        bitVal(ts) = bitLevel(level)(ts)
//        bitLevel(level)(ts) = 0L
//      }
//    }

    level -= 1
  }

  // 若元组有效，则返回真
  @inline private def isValidTuple(tuple: Array[Int]): Boolean = {
    var i = arity
    while (i > 0) {
      i -= 1
      if (!scope(i).contains(tuple(i))) return false
    }
    return true
  }

  override def stepUp(num_vars: Int): Unit = ???

  override def isEntailed(): Boolean = ???

  override def isSatisfied(): Unit = ???

}

