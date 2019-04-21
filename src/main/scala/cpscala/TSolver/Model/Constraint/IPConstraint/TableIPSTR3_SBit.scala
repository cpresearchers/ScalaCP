package cpscala.TSolver.Model.Constraint.IPConstraint

import cpscala.TSolver.CpUtil.SearchHelper.IPSearchHelper
import cpscala.TSolver.CpUtil._
import cpscala.TSolver.Model.Variable.PVar

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

// 变量类型使用SafeBitVar（可以处理论域任意大小的变量）

class TableIPSTR3_SBit(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[PVar], val tuples: Array[Array[Int]], val helper: IPSearchHelper) extends IPPropagator {

  // 子表，三维数组，第一维变量，第二维取值，第三维元组
  // 初始化变量时，其论域已经被序列化，诸如[0, 1, ..., var.size()]，所以可以直接用取值作为下标
  private[this] val subtables: Array[Array[Array[Int]]] = Array.tabulate(arity)(i => new Array[Array[Int]](scope(i).size()))
  // 分界符，二维数组，第一维变量，第二维取值
  private[this] val separators: Array[Array[Int]] = Array.tabulate(arity)(i => new Array[Int](scope(i).size()))
  // 分界符栈
  // 在搜索树初始层（0层)，若变量值的separator改变了，即更新变量栈顶层的HashMap（后来想了想，0层不需要保存，因为1层对应的栈顶保存的即是0层的信息）
  // 在搜索树的非初始层，当变量值的separator第一次发生改变时，将改变前的separator值保存在该变量栈顶层HashMap中
  // HashMap传入的范型中第一个Int为value，第二个Int为separator
  private[this] val StackS: Array[RestoreStack[Int, Int]] = Array.fill(arity)(new RestoreStack[Int, Int](num_vars))

  // 无效元组
  private[this] val invalidTuples: SparseSetInt = new SparseSetInt(tuples.length, num_vars)
  // 依赖表，用哈希表实现，key为变量在scope内的序号，value为取值
  private[this] val deps: Array[mutable.HashMap[Int, Int]] = Array.fill(tuples.length)(new mutable.HashMap[Int, Int]())


  // 变量的比特组个数
  private[this] val varBitNum: Array[Int] = Array.tabulate[Int](arity)(i => scope(i).getNumBit())
  // lastMask与变量Mask不同的值是该约束两次传播之间被过滤的值（delta）
  private[this] val lastMask = Array.tabulate(arity)(i => new Array[Long](varBitNum(i)))
  // 在约束传播开始时localMask获取变量最新的mask
  private[this] val localMask = Array.tabulate(arity)(i => new Array[Long](varBitNum(i)))
  // 记录该约束两次传播之间删值的mask
  private[this] val removeMask = Array.tabulate(arity)(i => new Array[Long](varBitNum(i)))
  // delta
  private[this] val removeValues = new ArrayBuffer[Int]()

  // 初始化标识变量
  // isInitial为false说明setup中表约束还未初始化数据结构
  // 为true说明表约束初始化完成，可以进行初始删值
  private[this] var isInitial = false

  override def setup(): Unit = {

    if (!isInitial) {

      // 初始化无效元组集
      invalidTuples.clear()

      // 临时子表
      val tempSupport = Array.tabulate(arity)(i => {
        Array.fill(scope(i).size())(new ArrayBuffer[Int]())
      })

      // 向临时子表内动态添加元组编号
      var t = 0
      while (t < tuples.length) {
        if (isValidTuple(tuples(t))) {
          var i = 0
          while (i < arity) {
            val a = tuples(t)(i)
            tempSupport(i)(a) += t
            i += 1
          }
        }
        t += 1
      }

      var i = 0
      while (i < arity) {
        val v = scope(i)
        v.mask(localMask(i))
        var j = v.size()
        // 因为变量还未删值，所以j既为index，又为取值
        while (j > 0) {
          j -= 1
          val subtable = tempSupport(i)(j).toArray
          subtables(i)(j) = subtable
          separators(i)(j) = subtable.length - 1
          if (!subtable.isEmpty) {
            // 15年论文伪代码是放在了最后一个元组对应的deps中
            deps(subtable(0)) += (i -> j)
          }
          else {
            // 巧妙，bit删值，即将mask中值j对应的bit位设置为0
            val (x, y) = INDEX.getXY(j)
            localMask(i)(x) &= Constants.MASK0(y)
            helper.varStamp(v.id) = helper.globalStamp + 1
            //            //println(s"     var:${v.id} remove new value:${j}")
          }
        }
        StackS(i).push()
        i += 1
      }
      // 表约束初始化完成
      isInitial = true
    }
    else {
      //      println(s"c_id: ${id} initial delete value==========================>")
      var i = 0
      while (i < arity) {
        val v = scope(i)
        // 更新变量论域
        v.submitMask(localMask(i))
        if (v.isEmpty()) {
          helper.isConsistent = false
          return
        }

        // 更新lastMask
        var j = 0
        while (j < varBitNum(i)) {
          lastMask(i)(j) = localMask(i)(j)
          j += 1
        }

        i += 1
      }
    }
  }

  def propagate(): Boolean = {

    //println(s"c_id: ${id} propagate==========================>")
    val membersBefore = invalidTuples.size()

    // 15年论文中的伪代码每次只处理一个值
    for (i <- 0 until arity) {
      val v = scope(i)
      v.mask(localMask(i))

      var diff = false
      var j = 0
      while (j < varBitNum(i)) {
        // 需先将removeMask清空，如果不清空，那么遇到lastMask和localMask相等的情况，removeMask仍然维持原样，若原样非全0，则会出错
        removeMask(i)(j) = 0L
        // 根据新旧mask的比较确定是否有删值
        if (lastMask(i)(j) != localMask(i)(j)) {
          removeMask(i)(j) = (~localMask(i)(j)) & lastMask(i)(j)
          // 更新lastMasks
          lastMask(i)(j) = localMask(i)(j)
          diff = true
        }
        //println(s"     localMask${j}: ${Constants.toFormatBinaryString(localMask(i)(j))}")
        //println(s"     lastMask${j}: ${Constants.toFormatBinaryString(lastMask(i)(j))}")
        //println(s"     removeMask${j}: ${Constants.toFormatBinaryString(removeMask(i)(j))}")
        j += 1
      }

      if (diff) {
        // 获得delta
        Constants.getValues(removeMask(i), removeValues)
        //println(s"       cons: ${id} var: ${v.id} removedValues: " + removeValues.mkString(", "))
        // 寻找新的无效元组
        for (a <- removeValues) {
          val sep = separators(i)(a)
          for (p <- 0 to sep) {
            val k = subtables(i)(a)(p)
            invalidTuples.add(k)
          }
        }
      }

    }

    // 无效元组没有更新
    val membersAfter = invalidTuples.size()
    if (membersBefore == membersAfter) {
      return true
    }

    //println(s"    cons: ${id}  the number of invalid tuple: ${membersAfter - membersBefore}")

    // 寻找没有支持的值
    var i = membersBefore
    while (i < membersAfter) {

      val k = invalidTuples.get(i)
      val dep = deps(k)

      for ((varId, value) <- dep) {

        val v = scope(varId)
        if (v.unBind() && v.contains(value)) {

          val subtable = subtables(varId)(value)
          val sep = separators(varId)(value)

          // 寻找支持
          var p = sep
          while (p >= 0 && invalidTuples.has(subtable(p))) p -= 1

          // 没有支持，删去该值
          if (p == -1) {
            // 巧妙，bit删值，即将mask中值value对应的bit位设置为0
            val (x, y) = INDEX.getXY(value)
            localMask(varId)(x) &= Constants.MASK0(y)
            if (v.submitMask(localMask(varId))) {
              // 论域若被修改，则全局时间戳加1
              helper.varStamp(v.id) = helper.globalStamp + 1
              //println(s"       cons: ${id}  var: ${v.id}  remove new value: ${value}")
              if (v.isEmpty()) {
                helper.isConsistent = false
                return false
              }
            }
            // 更新lastMask
            lastMask(varId)(x) = localMask(varId)(x)
          } else {
            if (p != sep) {
              // 更新变量栈顶的哈希表
              val topHash = StackS(varId).top
              if (!topHash.contains(value)) {
                topHash(value) = sep
              }
              separators(varId)(value) = p
            }
            // 将变量值对从无效的依赖表(k)挪入支持的依赖表(subtable(p))
            deps(k) -= (varId)
            deps(subtable(p)) += ((varId, value))
          }
        }
      }
      i += 1
    }
    return true
  }

  def call(): Boolean = {
    if (!helper.isConsistent) {
      return false
    }

    helper.searchState match {
      case 0 => {
        ////println("setup")
        setup()
      };
      case 1 => {
        ////println("newLevel")
        newLevel()
      };
      case 2 => {
        ////println("propagate")
        propagate()
      };
      case 3 => {
        ////println("backLevel")
        backLevel()
      };
    }

    return true
  }

  // 新层
  def newLevel(): Unit = {
    level += 1
    // 向inStackS压入一个新的HashMap（对应新层）
    for (i <- 0 until arity) {
      StackS(i).push()
    }
    // 保存上层invalidTuples的边界cursize（15年论文中的member）
    invalidTuples.newLevel()
    // 到达新层后不用更改oldMasks，oldMasks与上层保持一致
  }

  // 回溯
  def backLevel(): Unit = {
    level -= 1
    for (i <- 0 until arity) {
      // inStackS先弹出一个HashMap（当前层），再获取顶层的HashMap（上一层），将上一层的sep恢复
      //      StackS(i).pop()
      val topHash = StackS(i).pop
      // i为变量编号，a为取值，s为相应子表的sep
      for ((a, s) <- topHash) {
        separators(i)(a) = s
      }
      // 回溯后重置lastMasks，新旧mask相同，因为还没有传播
      scope(i).mask(lastMask(i))
    }
    // 恢复上层invalidTuples的边界cursize（15年论文中的member）
    invalidTuples.backLevel()
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
