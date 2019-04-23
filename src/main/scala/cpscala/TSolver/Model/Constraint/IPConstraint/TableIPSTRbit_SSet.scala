package cpscala.TSolver.Model.Constraint.IPConstraint

import cpscala.TSolver.Model.Constraint.SConstraint.BitSupport
import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.CpUtil.SearchHelper.IPSearchHelper
import cpscala.TSolver.Model.Variable.PVar

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

/**
  * 这是PSTRbit的第一个版本，变量类型使用SparseSet
  * 网络预处理时采用STRbit维持网络GAC，
  * 在搜索过程中也采用STRbit维持网络GAC，
  */

class TableIPSTRbit_SSet(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[PVar], val tuples: Array[Array[Int]], val helper: IPSearchHelper) extends IPPropagator {

  // 比特子表，三维数组，第一维变量，第二维取值，第三维元组
  // 初始化变量时，其论域已经被序列化，诸如[0, 1, ..., var.size()]，所以可以直接用取值作为下标
  private[this] val bitTables = Array.tabulate(arity)(i => new Array[Array[BitSupport]](scope(i).size()))
  // 分界符，二维数组，第一维变量，第二维取值
  private[this] val last = Array.tabulate(arity)(i => new Array[Int](scope(i).size()))
  // 分界符栈
  // 在搜索树初始层，若变量值的last改变了，即更新变量栈顶层的Array（后来想了想，0层不需要保存，因为1层对应的栈顶保存的即是0层初始化GAC后的信息）
  // 在搜索树的非初始层，当变量值的last第一次发生改变时，将改变前的last值保存在该变量栈顶层Array中
  private[this] val lastLevel = Array.fill[Array[Array[Int]]](num_vars + 1)(Array.tabulate(arity)(i => Array.fill[Int](scope(i).size())(-1)))

  private[this] val tupleLength = tuples.length
  // 比特元组的数量，tupleLength不能被64整除，要为余数创建一个比特元组
  private[this] val bitNum = if (tupleLength % 64 == 0) tupleLength / 64 else tupleLength / 64 + 1
  // 比特元组的集合，比特元组的每个比特位记录对应位置的元组是否有效
  private[this] val bitVal = Array.fill[Long](bitNum)(-1L)
  // 最后一个比特元组末尾清0
  bitVal(bitNum - 1) <<= 64 - tupleLength % 64
  // 比特元组栈
  // 在搜索树初始层，若比特元组改变了，即更新栈顶层的Array（后来想了想，0层不需要保存，因为1层对应的栈顶保存的即是0层初始化GAC后的信息）
  // 在搜索树的非初始层，当比特元组第一次发生改变时，将改变前的比特元组保存在栈顶层Array中
  private[this] val bitLevel = Array.fill[Long](num_vars + 1, bitNum)(0L)

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

  override def setup(): Unit = {

    if (!isInitial) {
      //println("c_id:" + id + " ===============>")

      val tempBitTable = Array.tabulate(arity)(i => {
        Array.fill(scope(i).size())(new ArrayBuffer[BitSupport]())
      })


      // 向临时子表内动态添加元组编号
      var t = 0
      while (t < tupleLength) {
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
        }
        i += 1
      }
      // 初始化数据结构完成
      isInitial = true
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
            x.safeRemove(value)
            helper.varStamp(x.id) = helper.globalStamp + 1
            //println(s"       var:${x.id} remove new value:${j}")
          }
        }
        i += 1
        if(x.isEmpty()) {
          helper.isConsistent = false
          return
        }
      }
    }
  }

  // 删除无效元组
  def deleteInvalidTuple(): Unit = {

    for (i <- 0 until arity) {
      val x = scope(i)

      if (oldSizes(i) != x.size()) {
        // 获得delta并更新oldSize
        removeValues.clear()
        oldSizes(i) = x.getLastRemovedValues(oldSizes(i).toLong, removeValues)

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
        validValues.clear()
        v.getValidValues(validValues)

        for (a <- validValues) {
          val bitSupports = bitTables(i)(a)
          val old = last(i)(a)
          // 寻找支持的比特元组
          var now = old

          while (now >= 0 && (bitSupports(now).mask & bitVal(bitSupports(now).ts)) == 0L) {
            now -= 1
          }

          if (now == -1) {
            v.safeRemove(a)
            // 因为是并行不同于串行，会出现多个约束删除同一个变量不同值的情况，所以这里不能让oldSize-1
            //            oldSizes(varId) -= 1
            // 论域若被修改，则全局时间戳加1
            helper.varStamp(v.id) = helper.globalStamp + 1

            if (v.isEmpty()) {
              helper.isConsistent = false
              return false
            }
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

    // 到达新层后不用更改oldSize，oldSize与上层保持一致
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
      // 回溯后重置oldSize，新旧大小相同，因为还没有传播
      oldSizes(i) = scope(i).size()
    }

    for (ts <- 0 until bitNum) {
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
