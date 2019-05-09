package cpscala.TSolver.Model.Constraint.IPConstraint

import cpscala.TSolver.Model.Constraint.SConstraint.BitSupport
import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.CpUtil.SearchHelper.IPSearchHelper
import cpscala.TSolver.Model.Variable.PVar

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

/**
  * 这是IPSTRbit使用SafeSimpleBitVar作为变量类型的版本（仅处理论域大小小于64的变量）
  * 网络预处理时采用STRbit维持网络GAC，
  * 在搜索过程中也采用STRbit维持网络GAC，
  */

class TableIPSTRbit_SSBit(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[PVar], val tuples: Array[Array[Int]], val helper: IPSearchHelper) extends IPPropagator {

  // 比特子表，三维数组，第一维变量，第二维取值，第三维元组
  // 初始化变量时，其论域已经被序列化，诸如[0, 1, ..., var.size()]，所以可以直接用取值作为下标
  private[this] val bitTables = Array.tabulate(arity)(i => new Array[Array[BitSupport]](scope(i).size()))
  // 分界符，二维数组，第一维变量，第二维取值
  private[this] val last = Array.tabulate(arity)(i => new Array[Int](scope(i).size()))
  // 分界符栈
  // 在搜索树初始层，若变量值的last改变了，即更新变量栈顶层的Array（后来想了想，0层不需要保存，因为1层对应的栈顶保存的即是0层初始化GAC后的信息）
  // 在搜索树的非初始层，当变量值的last第一次发生改变时，将改变前的last值保存在该变量栈顶层Array中
  private[this] val lastLevel = Array.fill[Array[Array[Int]]](num_vars + 1)(Array.tabulate(arity)(i => Array.fill[Int](scope(i).size())(-1)))

  private[this] val lengthTuple = tuples.length
  // 比特元组的数量，tupleLength不能被64整除，要为余数创建一个比特元组
  private[this] val numBitTuple = Math.ceil(lengthTuple.toDouble / Constants.BITSIZE.toDouble).toInt
  // 比特元组的集合，比特元组的每个比特位记录对应位置的元组是否有效
  private[this] val bitVal = Array.fill[Long](numBitTuple)(-1L)
  // 最后一个比特元组末尾清0
  bitVal(numBitTuple - 1) <<= Constants.BITSIZE - lengthTuple % Constants.BITSIZE
  // 比特元组栈
  // 在搜索树初始层，若比特元组改变了，即更新栈顶层的Array（后来想了想，0层不需要保存，因为1层对应的栈顶保存的即是0层初始化GAC后的信息）
  // 在搜索树的非初始层，当比特元组第一次发生改变时，将改变前的比特元组保存在栈顶层Array中
  private[this] val bitLevel = Array.fill[Long](num_vars + 1, numBitTuple)(0L)

  // lastMask与变量Mask不同的值是该约束两次传播之间被过滤的值（delta）
  private[this] val lastMask = Array.tabulate[Long](arity)(i => scope(i).simpleMask())
  // 在约束传播开始时localMask获取变量最新的mask
  private[this] val localMask = Array.tabulate[Long](arity)(i => scope(i).simpleMask())
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
      //println("c_id:" + id + " ===============>")

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
        val x = scope(i)
        // 因为变量还未删值，所以j既为index，又为取值
        var j = x.size()
        while (j > 0) {
          j -= 1
          val tempBitSupports = tempBitTable(i)(j)
          bitTables(i)(j) = tempBitSupports.toArray
          last(i)(j) = tempBitSupports.length - 1
          if (tempBitSupports.isEmpty) {
            // 巧妙，bit删值，即将mask中值j对应的bit位设置为0
            localMask(i) &= Constants.MASK0(j)
            helper.varStamp(x.id) = helper.globalStamp + 1
            //            println(s"     var:${x.id} remove new value:${j}")
          }
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
        val x = scope(i)
        // 更新变量论域
        x.submitMask(localMask(i))
        if (x.isEmpty()) {
          helper.isConsistent = false
          return false
        }
        // 更新变量在该约束中的Mask（这里不能更新oldMasks，因为还未传播）
        lastMask(i) = localMask(i)
        i += 1
      }
    }
    return true
  }

  // 删除无效元组
  def deleteInvalidTuple(): Unit = {

    for (i <- 0 until arity) {
      val x = scope(i)
      localMask(i) = x.simpleMask()

      // 根据新旧mask的比较确定是否有删值
      if (lastMask(i) != localMask(i)) {
        val removeMask: Long = (~localMask(i)) & lastMask(i)
        removeValues.clear()

        var j = 0
        while (j < x.capacity) {
          // 巧妙，判断第j个bit处是否为1
          if ((removeMask & Constants.MASK1(j)) != 0L) {
            removeValues += j
          }
          j += 1
        }
        //        //println(s"cur_ID: ${Thread.currentThread().getId()},cur_name: ${Thread.currentThread().getName()}, cur_cid: ${id}, var: ${i}, removeValues: " + removeValues.mkString(","))
        // 更新oldMasks
        lastMask(i) = localMask(i)

        // 寻找新的无效元组
        for (a <- removeValues) {
          val old = last(i)(a)
          val bitSupports = bitTables(i)(a)

          for (l <- 0 to old) {
            val ts = bitSupports(l).ts
            val u = bitSupports(l).mask & bitVal(ts)

            // 与结果非0，说明bit为1的位置对应的元组变为无效
            if (u != 0L) {
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
  def searchSupport(): Boolean = {

    for (i <- 0 until arity) {
      val v = scope(i)

      if (v.unBind()) {
        var deleted = false

        // 这里不能获取最新的mask，因为只在约束传播开始时获取最新的mask
        // localMask(i) = v.simpleMask()
        validValues.clear()
        var j = 0

        while (j < v.capacity) {
          if ((localMask(i) & Constants.MASK1(j)) != 0L) {
            validValues += j
          }
          j += 1
        }
        //        //println(s"cur_ID: ${Thread.currentThread().getId()},cur_name: ${Thread.currentThread().getName()}, cur_cid: ${id}, var: ${i}, removeValues: " + removeValues.mkString(","))


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
            // 巧妙，bit删值，即将mask中值value对应的bit位设置为0
            localMask(i) &= Constants.MASK0(a)

            //println(s"cur_ID: ${Thread.currentThread().getId()},cur_name: ${Thread.currentThread().getName()}, cur_cid: ${id}, var: ${i}, remove val: ${a}")
          } else {
            if (now != old) {
              // 将第一次改变之前的last记录下来
              if (lastLevel(level)(i)(a) == -1) {
                lastLevel(level)(i)(a) = old
              }
              last(i)(a) = now
            }
          }
        }
        if (deleted) {
          v.submitMask(localMask(i))
          if (v.isEmpty()) {
            helper.isConsistent = false
            failWeight += 1
            return false
          }
          // 更新oldMasks
          lastMask(i) = localMask(i)
          // 论域若被修改，则全局时间戳加1
          helper.varStamp(v.id) = helper.globalStamp + 1
        }
      }
    }
    return true
  }

  def propagate(): Boolean = {

    deleteInvalidTuple()

    return searchSupport()
  }

  def call(): Boolean = {
    if (!helper.isConsistent) {
      return false
    }

    return propagate()
  }

  // 新层
  def newLevel(): Unit = {
    level += 1
    // 到达新层后不用更改lastMask，lastMask与上层保持一致
  }

  // 回溯
  def backLevel(): Unit = {
    for (i <- 0 until arity) {
      for (a <- 0 until scope(i).capacity) {
        if (lastLevel(level)(i)(a) != -1) {
          last(i)(a) = lastLevel(level)(i)(a)
          lastLevel(level)(i)(a) = -1
        }
      }
      // 回溯后重置oldMasks，新旧mask相同，因为还没有传播
      lastMask(i) = scope(i).simpleMask()
    }

    for (ts <- 0 until numBitTuple) {
      if (bitLevel(level)(ts) != 0L) {
        bitVal(ts) = bitLevel(level)(ts)
        bitLevel(level)(ts) = 0L
      }
    }

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
}
