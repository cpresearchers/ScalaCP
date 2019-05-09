package cpscala.TSolver.Model.Constraint.SConstraint

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil._
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer
import scala.collection.{mutable => m}
import scala.util.control.Breaks._

/**
  * 这是STRbit的第二个版本，
  * 网络预处理时采用STRbit维持网络GAC，
  * 在搜索过程中也采用STRbit维持网络GAC，
  * 与第一版的不同之处在于增添了一些加速机制，如下
  * 1.在更新比特元组时，第一版只处理自上次传播后变量被删去的值。
  * 但如果变量留下的值比被删去的值少，只处理删值就不如处理留值快。
  * 所以，第二版处理删值和留值中数量较少的一方。
  * 2.加入比特依赖表，缩减更新值集（貌似没有减少）
  */

class TableSTRbit_3(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: SearchHelper) extends Propagator[Var] {

  // 比特子表，三维数组，第一维变量，第二维取值，第三维比特支持
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

  private[this] val tupleLength = tuples.length
  // 比特元组的数量，tupleLength不能被64整除，要为余数创建一个比特元组
  private[this] val bitTupleNum = if (tupleLength % 64 == 0) tupleLength / 64 else tupleLength / 64 + 1
  // 比特元组的集合，比特元组的每个比特位记录对应位置的元组是否有效
  private[this] val bitTuple = Array.fill[Long](bitTupleNum)(-1L)
  // 最后一个比特元组末尾清0
  bitTuple(bitTupleNum - 1) <<= 64 - tupleLength % 64
  // 比特元组栈
  // 在搜索树初始层，若比特元组改变了，即更新栈顶层的HashMap（后来想了想，0层不需要保存，因为1层对应的栈顶保存的即是0层初始化GAC后的信息）
  // 在搜索树的非初始层，当比特元组第一次发生改变时，将改变前的比特元组保存在栈顶层HashMap中
  // HashMap传入的范型中Int为ts，Long为mask
  // private[this] val stackV = new RestoreStack[Int, Long](numVars)
  // 比特元组恢复数组，尝试一下Array是否比HashMap快（确实快一点）
  private[this] val bitTupleLevel = Array.fill[Long](num_vars + 1, bitTupleNum)(0L)
  // 有效比特元组集，保存有效比特元组（非全0）的下标，支持回溯
  private[this] val validBT = new SparseSetInt(bitTupleNum, num_vars + 1)
  // 比特掩码，用以保存留值或删值对应有效比特支持的逻辑运算结果，结果中bit为0表示无效，bit为1表示有效
  private[this] val bitMask = Array.fill[Long](bitTupleNum)(0L)
  // 比特依赖表，用哈希表实现，key为变量在scope内的序号，value为取值
  private[this] val bitDeps = Array.fill(tupleLength)(new ArrayBuffer[Literal])
  // 比特元组改变集，记录本次传播中发生改变的比特元组
  private[this] val bitChange = m.Set[Int]()


  // oldSize与变量size之间的值是该约束两次传播之间被过滤的值（delta）
  // 详情见论文：Sparse-Sets for Domain Implementation
  private[this] val oldSizes = Array.tabulate(arity)(i => scope(i).size())
  private[this] val removeValues = new ArrayBuffer[Int]() //(delta)
  // 变量的剩余有效值
  private[this] val validValues = new ArrayBuffer[Int]()

  override def setup(): Boolean = {
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
    while (t < tupleLength) {
      if (isValidTuple(tuples(t))) {
        //        //println("validTuple:" + tuples(t).mkString(","))
        // ts为比特元组下标
        val ts = t / 64
        // index为第ts个比特元组中元组的位置
        val index = t % 64
        //        //println(s"ts: ${ts} index: ${index}")
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
          //          //println(s"var: ${i} value: ${a} bitSupport: ${bitSupportsMap(ts).toBinaryString}")
          i += 1
        }
      }
      t += 1
    }

    //    val tempBitSupports = new ArrayBuffer[BitSupport]()
    var i = 0
    while (i < arity) {
      val x = scope(i)
      // j既为取值，又为下标
      var j = x.size()
      //      //println(s"var: ${i}")
      while (j > 0) {
        j -= 1
        //        //println(s"value: ${j}")
        //        val bitSupportsMap = tempBitTable(i)(j)
        val tempBitSupports = tempBitTable(i)(j)
        if (tempBitSupports.isEmpty) {
          x.remove(j)
          helper.varStamp(x.id) = helper.globalStamp
          //          ////////println("var:" + scope(i).id + "  removeValue:" + tuples(t))
        } else {
          //          tempBitSupports.clear()
          //          // 利用折半查找使得变量值的比特支持按序号递增排列
          //          for ((ts, mask) <- bitSupportsMap) {
          //            //            //println(s"ts: ${ts} mask: ${mask.toBinaryString}")
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
          // 将(i, j)加入到第一个比特元组对应的比特依赖表中
          bitDeps(tempBitSupports(0).ts) += new Literal(i, j)
        }
      }
      //      stackL(i).push()
      i += 1
    }
    return true

    //println("dep of tuple====>")
    //        i = bitTupleNum
    //        while (i > 0) {
    //          i -= 1
    //          //println("bitTuple:" + i + "  deps:")
    //          bitDeps(i).foreach(l => l.show())
    //        }
    //    stackV.push()
  }

  // 删除无效元组
  def deleteInvalidTuple(): Boolean = {

    // 清空比特元组改变集
    bitChange.clear()
    for (i <- 0 until arity) {
      val x = scope(i)
      val sizeX = x.size()

      if (oldSizes(i) != sizeX) {

        // 删值更新
        if ((oldSizes(i) - sizeX) <= sizeX) {

          // 将有效比特元组对应的比特掩码初始化为全1
          for (j <- 0 until validBT.size()) {
            val ts = validBT.dense(j)
            bitMask(ts) = -1L
          }

          // 获得delta并更新oldSize
          removeValues.clear()
          oldSizes(i) = x.getLastRemovedValues(oldSizes(i).toLong, removeValues)
          //          //println(s"       var: ${x.id} dit removedValues: " + removeValues.mkString(", "))

          // 寻找无效元组，保存于最终的比特掩码中，即bit位为0的位置
          for (a <- removeValues) {
            val old = last(i)(a)
            val bitSupports = bitTables(i)(a)

            for (l <- 0 to old) {
              val ts = bitSupports(l).ts
              // 若比特支持对应的比特元组仍然有效，则将比特支持取反后同对应的比特掩码相与
              // 之所以比特支持取反，是因为删值的比特支持中有效元组（bit为1）变为无效元组（bit为0）
              // 之所以与运算，是因为与运算能够累计得到所有的无效元组（0与运算的不变性）。
              if (validBT.has(ts)) {
                bitMask(ts) &= ~bitSupports(l).mask
              }
            }
          }
        }

        // 留值更新
        else {

          // 将有效比特元组对应的比特掩码初始化为全0
          for (j <- 0 until validBT.size()) {
            val ts = validBT.dense(j)
            bitMask(ts) = 0L
          }

          // 获得有效值集合并更新oldSize
          validValues.clear()
          oldSizes(i) = x.getValidValues(validValues)
          //          //println(s"       var: ${x.id} dit validValues: " + validValues.mkString(", "))

          // 寻找有效元组，保存于最终的比特掩码中，即bit位为1的位置
          for (a <- validValues) {
            val old = last(i)(a)
            val bitSupports = bitTables(i)(a)

            for (l <- 0 to old) {
              val ts = bitSupports(l).ts
              // 若比特支持对应的比特元组仍然有效，则将比特支持同对应的比特掩码相或
              // 之所以或运算，是因为或运算能够累计得到所有的有效元组（1或运算的不变性）。
              if (validBT.has(ts)) {
                bitMask(ts) |= bitSupports(l).mask
              }
            }
          }
        }

        // 最后将有效比特元组和相应的比特掩码相与
        // 曾经的bug，这里循环必须是从后向前，从前向后会出错误，因为在循环体中SparseSet会移除(remove)元素，导致size发生改变
        var j = validBT.size() - 1
        while (j >= 0) {
          val ts = validBT.dense(j)
          val u = bitMask(ts) & bitTuple(ts)

          if (u != bitTuple(ts)) {
            //              val topHashV = stackV.top

            //              if (!topHashV.contains(ts)) {
            //                topHashV(ts) = bitVal(ts)
            //              }
            // 将第一次改变之前的比特元组记录下来
            if (bitTupleLevel(level)(ts) == 0L) {
              bitTupleLevel(level)(ts) = bitTuple(ts)
            }
            // 更新比特元组
            bitTuple(ts) = u
            bitChange += ts
            // 若比特元组全为0，则为无效
            if (u == 0L) {
              validBT.remove(ts)
            }
          }
          j -= 1
        }
        // 若所有的比特元组均无效，则传播失败
        if (validBT.empty()) {
          failWeight += 1
          return false
        }
      }
    }
    //    for (i <- 0 until bitTupleNum) {
    //      val binaryMask = bitTuple(i).toBinaryString
    //      val binaryLength = binaryMask.length
    //      val highBit = "0" * (64 - binaryLength)
    //              //println(s"       bitSupport: (${i}, ${highBit}${binaryMask})")
    //println(s"       af bitSupport: (${i}, ${bitTuple(i)})")
    //    }
    return true
  }

  // 寻找没有支持的值
  def searchSupport(evt: ArrayBuffer[Var]): Boolean = {

    for (ts <- bitChange) {
      val bitDep = bitDeps(ts)
      //println(s"       ss change ts: ${ts} ")

      var j = bitDep.length - 1
      while (j >= 0) {

        //    for (i <- 0 until arity) {
        val literal = bitDep(j)
        val i = literal.v
        val x = scope(i)
        val a = literal.a
        if (x.contains(a)) {


          //      validValues.clear()
          //      x.getValidValues(validValues)
          //                //println(s"       var: ${x.id} ss validValues: " + validValues.mkString(", "))
          //println(s"       var: ${x.id} ss validValues: " + a)
          //
          //      for (a <- validValues) {
          val bitSupports = bitTables(i)(a)
          val old = last(i)(a)

          // 寻找支持的比特元组
          var now = old
          //println(s"       val: ${a} now: ${now}")
          //        var ts = bitSupports(now).ts
          while (now >= 0 && (bitSupports(now).mask & bitTuple(bitSupports(now).ts)) == 0L) {
            now -= 1
            //println(s"       val: ${a} now: ${now}")
            //          if (now != -1) {
            //            ts = bitSupports(now).ts
            //          }
          }

          if (now == -1) {
            x.remove(a)
            oldSizes(i) -= 1
            //println(s"       var:${x.id} remove new value:${a}  old: ${old}")
            evt += x

            if (x.isEmpty()) {
              failWeight += 1
              return false
            }
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

              //更新比特依赖表
            }
            if (ts != bitSupports(now).ts) {
              //println("bitDeps changed ===>")
              //println(s"      be old dep: ${ts}")
              //              bitDep.foreach(l => l.show())
              //println(s"      be new dep: ${bitSupports(now).ts}")
              //              bitDeps(bitSupports(now).ts).foreach(l => l.show())

              bitDeps(bitSupports(now).ts) += literal
              val lastPos = bitDep.length - 1
              bitDep(j) = bitDep(lastPos)
              bitDep.remove(lastPos)

              //println(s"      af old dep: ${ts}")
              //              bitDep.foreach(l => l.show())
              //println(s"      af new dep: ${bitSupports(now).ts}")
              //              bitDeps(bitSupports(now).ts).foreach(l => l.show())
            }
          }
        }
        j -= 1
      }
    }
    //    //println("dep of tuple====>")
    //    var i = bitTupleNum
    //    while (i > 0) {
    //      i -= 1
    //      //println("bitTuple:" + i + "  deps:")
    //      bitDeps(i).foreach(l => l.show())
    //    }

    return true
  }

  override def propagate(evt: ArrayBuffer[Var]): Boolean = {

    //println(s"c_id: ${id} propagate==========================>")
    val ditStart = System.nanoTime
    val dit = deleteInvalidTuple()
    val ditEnd = System.nanoTime
    helper.updateTableTime += ditEnd - ditStart
    if (!dit) {
      return false
    }

    val ssStart = System.nanoTime
    val ss = searchSupport(evt)
    val ssEnd = System.nanoTime
    helper.filterDomainTime += ssEnd - ssStart
    return ss
  }

  // 新层
  def newLevel(): Unit = {
    level += 1

    validBT.newLevel()
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
    //    //println(s"c_id: ${id} backlevel==================>")
    // 比特元组有效集回溯
    validBT.backLevel()

    for (i <- 0 until arity) {
      //      val topHashL = stackL(i).pop
      // i为变量编号，a为取值，l为相应子表的last
      //      for ((a, l) <- topHashL) {
      //        if (scope(i).id == 3) {
      //          //println(s"       now var:${scope(i).id} value:${a} last:${last(i)(a)}")
      //          //println(s"       after var:${scope(i).id} value:${a} last:${l}")
      //        }
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
    //      var binaryMask = bitVal(ts).toBinaryString
    //      var binaryLength = binaryMask.length
    //      var highBit = "0" * (64 - binaryLength)
    //      //println(s"be bitSupport: (${ts}, ${highBit}${binaryMask})")
    //      bitVal(ts) = mask
    //      binaryMask = bitVal(ts).toBinaryString
    //      binaryLength = binaryMask.length
    //      highBit = "0" * (64 - binaryLength)
    //      //println(s"af bitSupport: (${ts}, ${highBit}${binaryMask})")
    //    }
    for (ts <- 0 until bitTupleNum) {
      if (bitTupleLevel(level)(ts) != 0L) {
        bitTuple(ts) = bitTupleLevel(level)(ts)
        bitTupleLevel(level)(ts) = 0L
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
