package cpscala.TSolver.Model.Constraint.SConstraint

import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

/**
  * 这是STRbit的第一个版本，
  * 网络预处理时采用STRbit维持网络GAC，
  * 在搜索过程中也采用STRbit维持网络GAC，
  * 参考论文：2016_IJCAI_Optimizing Simple Table Reduction with Bitwise Representation
  */

class TableSTRbit_SSet(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: SearchHelper) extends Propagator {

  // 比特子表，三维数组，第一维变量，第二维取值，第三维元组
  // 初始化变量时，其论域已经被序列化，诸如[0, 1, ..., var.size()]，所以可以直接用取值作为下标
  private[this] val bitTables = Array.tabulate(arity)(i => new Array[Array[BitSupport]](scope(i).size()))
  // 分界符，二维数组，第一维变量，第二维取值
  private[this] val last = Array.tabulate(arity)(i => new Array[Int](scope(i).size()))
  // 分界符栈
  // 在搜索树初始层，若变量值的last改变了，即更新变量栈顶层的HashMap（后来想了想，0层不需要保存，因为1层对应的栈顶保存的即是0层初始化GAC后的信息）
  // 在搜索树的非初始层，当变量值的last第一次发生改变时，将改变前的last值保存在该变量栈顶层HashMap中
  // HashMap传入的范型中第一个Int为value，第二个Int为last
  //  private[this] val StackL = Array.fill(arity)(new RestoreStack[Int, Int](numVars))
  // 尝试一下Array是否比HashMap快（确实快一点）
  private[this] val lastLevel = Array.fill[Array[Array[Int]]](num_vars + 1)(Array.tabulate(arity)(i => Array.fill[Int](scope(i).size())(-1)))

  private[this] val lengthTuple = tuples.length
  // 下面两种计算方式相同
  private[this] val numBit = Math.ceil(lengthTuple.toDouble / Constants.BITSIZE.toDouble).toInt
  // 比特元组的数量，tupleLength不能被64整除，要为余数创建一个比特元组
  // private[this] val numBit = if (lengthTuple % 64 == 0) lengthTuple / 64 else lengthTuple / 64 + 1
  // 比特元组的集合，比特元组的每个比特位记录对应位置的元组是否有效
  private[this] val bitVal = Array.fill[Long](numBit)(-1L)
  // 最后一个比特元组末尾清0
  bitVal(numBit - 1) <<= Constants.BITSIZE - lengthTuple % Constants.BITSIZE
  // 比特元组栈
  // 在搜索树初始层，若比特元组改变了，即更新栈顶层的HashMap（后来想了想，0层不需要保存，因为1层对应的栈顶保存的即是0层初始化GAC后的信息）
  // 在搜索树的非初始层，当比特元组第一次发生改变时，将改变前的比特元组保存在栈顶层HashMap中
  // HashMap传入的范型中Int为ts，Long为mask
  //  private[this] val stackV = new RestoreStack[Int, Long](numVars)
  // 尝试一下Array是否比HashMap快（确实快一点）
  private[this] val bitLevel = Array.fill[Long](num_vars + 1, numBit)(0L)

  // oldSize与变量size之间的值是该约束两次传播之间被过滤的值（delta）
  // 详情见论文：Sparse-Sets for Domain Implementation
  private[this] val oldSizes = Array.tabulate(arity)(i => scope(i).size())
  private[this] val removeValues = new ArrayBuffer[Int]() //(delta)
  // 变量的剩余有效值
  private[this] val validValues = new ArrayBuffer[Int]()

  // 初始化标志变量
  // isInitial为false说明setup中还未初始化完成数据结构
  // 为true说明初始化数据结构完成，可以进行初始删值
  private[this] var isInitial = false

  override def setup(): Boolean = {

    if (!isInitial) {
      //println("c_id:" + id + " ===============>")

      // 临时比特子表, 实验证明ArrayBuffer版比HashMap版快
      // ArrayBuffer版时间复杂度O(trB), B为二分查找时间复杂度O(log(t/w)), w为比特向量的长度
      // HashMap版时间复杂度O(trH + drB), H为Scala内置哈希表查找时间复杂度

      //    val tempBitTable = Array.tabulate(arity)(i => {
      //      Array.fill(scope(i).size())(new m.HashMap[Int, Long]())
      //    })

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
            //          val bitSupportsMap = tempBitTable(i)(a)
            //          if (!bitSupportsMap.contains(ts)) {
            //            bitSupportsMap(ts) = Constants.MASK1(index)
            //          } else {
            //            bitSupportsMap(ts) = bitSupportsMap(ts) | Constants.MASK1(index)
            //          }
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

      //    val tempBitSupports = new ArrayBuffer[BitSupport]()
      var i = 0
      while (i < arity) {
        val x = scope(i)
        // 因为变量还未删值，所以j既为index，又为取值
        var j = x.size()
        while (j > 0) {
          j -= 1
          //        val bitSupportsMap = tempBitTable(i)(j)
          val tempBitSupports = tempBitTable(i)(j)
          //          tempBitSupports.clear()
          //          // 利用折半查找使得变量值的比特支持按序号递增排列
          //          for ((ts, mask) <- bitSupportsMap) {
          //            //            //////////println(s"ts: ${ts} mask: ${mask.toBinaryString}")
          //            var low = 0
          //            var high = tempBitSupports.length - 1
          //            var middle = 0
          //            while (low <= high) {
          //              middle = (low + high) / 2
          //              if (ts < tempBitSupports(middle).ts) {
          //                high = middle - 1
          //              } else {
          //                low = middle + 1
          //              }
          //            }
          //            val index = high + 1
          //            val bitSupport = new BitSupport(ts, mask)
          //            tempBitSupports.insert(index, bitSupport)
          //          }
          bitTables(i)(j) = tempBitSupports.toArray
          last(i)(j) = tempBitSupports.length - 1
        }
        //      stackL(i).push()
        i += 1
      }
      // 初始化数据结构完成
      isInitial = true
      return true
    }
    else {
      //println("c_id:" + id + " ===============>")
      var i = 0
      while (i < arity) {
        val x = scope(i)
        // j既为取值，又为下标
        var j = x.size()
        while (j > 0) {
          j -= 1
          // 因为变量可能已被删值，所以需要通过下标j来取得value
          val value = x.get(j)
          if (bitTables(i)(value).isEmpty) {
            x.remove(value)
            helper.varStamp(x.id) = helper.globalStamp
            //println(s"       var:${x.id} remove new value:${value}")
          }
        }
        //      stackL(i).push()
        i += 1
        if (x.isEmpty()) {
          return false
        }
      }
    }
    //    stackV.push()
    return true
  }

  // 删除无效元组
  def deleteInvalidTuple(): Unit = {

    for (i <- 0 until arity) {
      val x = scope(i)

      if (oldSizes(i) != x.size()) {
        // 获得delta并更新oldSize
        removeValues.clear()
        oldSizes(i) = x.getLastRemovedValues(oldSizes(i).toLong, removeValues)
        //println(s"       var: ${x.id} dit removedValues: " + removeValues.mkString(", "))

        // 寻找新的无效元组
        for (a <- removeValues) {
          val old = last(i)(a)

          val bitSupports = bitTables(i)(a)
          for (l <- 0 to old) {
            val ts = bitSupports(l).ts
            val u = bitSupports(l).mask & bitVal(ts)

            // 与结果非0，说明bit为1的位置对应的元组变为无效
            if (u != 0L) {

              //              val topHashV = stackV.top
              //              if (!topHashV.contains(ts)) {
              //                topHashV(ts) = bitVal(ts)
              //              }
              // 将第一次改变之前的比特元组记录下来
              if (bitLevel(level)(ts) == 0L) {
                bitLevel(level)(ts) = bitVal(ts)
              }
              // 更新比特元组
              bitVal(ts) &= ~u
            }
          }
        }
      }
    }
  }

  // 寻找没有支持的值
  def searchSupport(evt: ArrayBuffer[Var]): Boolean = {

    for (i <- 0 until arity) {
      val v = scope(i)
      if (v.unBind()) {
        var deleted: Boolean = false
        validValues.clear()
        v.getValidValues(validValues)

        //println(s"       var: ${v.id} ss validValues: " + validValues.mkString(", "))

        for (a <- validValues) {
          val bitSupports = bitTables(i)(a)
          val old = last(i)(a)

          // 寻找支持的比特元组
          var now = old
          while (now >= 0 && (bitSupports(now).mask & bitVal(bitSupports(now).ts)) == 0L) {
            now -= 1
          }

          if (now == -1) {
            deleted = true
            v.remove(a)
//            println(s"    cur_cid: ${id}, var: ${v.id}, remove val: ${a}")

            // 以下操作放在这里不如放在循环外面快，因为删了多个值会重复执行
            //            oldSizes(i) -= 1
            //            evt += v
            //            if (v.isEmpty()) return false
          } else {
            if (now != old) {
              // 更新变量栈顶的哈希表
              //            val topHashL = stackL(i).top
              //
              //            if (!topHashL.contains(a)) {
              //              topHashL(a) = old
              //            }
              // 将第一次改变之前的last记录下来
              if (lastLevel(level)(i)(a) == -1) {
                lastLevel(level)(i)(a) = old
              }
              last(i)(a) = now
            }
          }
        }
        if (deleted) {
          if (v.isEmpty()) return false
          oldSizes(i) = v.size()
          evt += v
        }
      }
    }
    return true
  }

  override def propagate(evt: ArrayBuffer[Var]): Boolean = {

//    println(s"c_id: ${id} propagate==========================>")
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
    level += 1
    // 向stackL压入一个新的HashMap（对应新层）
    //    for (i <- 0 until arity) {
    //      stackL(i).push()
    //    }
    // 向stackV压入一个新的HashMap（对应新层）
    //    stackV.push()

    // 到达新层后不用更改oldSize，oldSize与上层保持一致
  }

  // 回溯
  def backLevel(): Unit = {
    for (i <- 0 until arity) {
      //      val topHashL = stackL(i).pop
      // i为变量编号，a为取值，l为相应子表的last
      //      for ((a, l) <- topHashL) {
      //      last(i)(a) = l
      for (a <- 0 until scope(i).capacity) {
        if (lastLevel(level)(i)(a) != -1) {
          last(i)(a) = lastLevel(level)(i)(a)
          lastLevel(level)(i)(a) = -1
        }
      }
      // 回溯后重置oldSize，新旧大小相同，因为还没有传播
      oldSizes(i) = scope(i).size()
    }

    // 恢复bitVal
    //    val topHashV = stackV.pop
    //    for ((ts, mask) <- topHashV) {
    //      bitVal(ts) = mask
    //    }
    for (ts <- 0 until numBit) {
      if (bitLevel(level)(ts) != 0L) {
        bitVal(ts) = bitLevel(level)(ts)
        bitLevel(level)(ts) = 0L
      }
    }

    level -= 1
  }

  override def stepUp(num_vars: Int): Unit = ???

  override def isEntailed(): Boolean = ???

  override def isSatisfied(): Unit = ???

  // 若元组有效，则返回真
  @inline private def isValidTuple(tuple: Array[Int]): Boolean = {
    var i = arity
    while (i > 0) {
      i -= 1
      if (!scope(i).contains(tuple(i))) return false
    }
    return true
  }
}

class BitSupport(val ts: Int, var mask: Long) {

}