package cpscala.TSolver.Model.Constraint.IPConstraint

import cpscala.TSolver.CpUtil.SearchHelper.IPSearchHelper
import cpscala.TSolver.CpUtil.{RestoreStack, SparseSetInt}
import cpscala.TSolver.Model.Variable.PVar

import scala.collection.mutable.ArrayBuffer
import scala.collection.{mutable => m}

// 变量类型使用SparseSet

class TableIPSTR3_SSet(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[PVar], val tuples: Array[Array[Int]], val helper: IPSearchHelper) extends IPPropagator {

  // 子表，三维数组，第一维变量，第二维取值，第三维元组
  // 初始化变量时，其论域已经被序列化，诸如[0, 1, ..., var.size()]，所以可以直接用取值作为下标
  private[this] val subtables = Array.tabulate(arity)(i => new Array[Array[Int]](scope(i).size()))
  // 分界符，二维数组，第一维变量，第二维取值
  private[this] val separators = Array.tabulate(arity)(i => new Array[Int](scope(i).size()))
  // 分界符栈
  // 在搜索树初始层（0层)，若变量值的separator改变了，即更新变量栈顶层的HashMap（后来想了想，0层不需要保存，因为1层对应的栈顶保存的即是0层的信息）
  // 在搜索树的非初始层，当变量值的separator第一次发生改变时，将改变前的separator值保存在该变量栈顶层HashMap中
  // HashMap传入的范型中第一个Int为value，第二个Int为separator
  private[this] val StackS = Array.fill(arity)(new RestoreStack[Int, Int](num_vars))

  // 无效元组
  private[this] val invalidTuples = new SparseSetInt(tuples.length, num_vars)
  // 依赖表，用哈希表实现，key为变量在scope内的序号，value为取值
  private[this] val deps = Array.fill(tuples.length)(new m.HashMap[Int, Int]())

  // oldSize与变量size之间的值是该约束两次传播之间被过滤的值（delta）
  // 详情见论文：Sparse-Sets for Domain Implementation
  private[this] val oldSizes = Array.tabulate(arity)(i => scope(i).size())
  private[this] val removeValues = new ArrayBuffer[Int]() //(delta)

  // 初始化标识变量
  // isInitial为false说明setup中表约束还未初始化数据结构
  // 为true说明表约束初始化完成，可以进行初始删值
  private[this] var isInitial = false

  override def setup(): Boolean = {

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
        val x = scope(i)
        var j = x.size()
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
        }
        StackS(i).push()
        i += 1
      }
      // 表约束初始化完成
      isInitial = true

      return true
    }
    // 初始删值
    else {
      //println(s"c_id: ${id} initial delete value==========================>")
      var i = 0
      while (i < arity) {
        val x = scope(i)
        var j = x.size()
        while (j > 0) {
          j -= 1
          // 因为变量可能已被删值，所以需要通过下标j来取得value
          val value = x.get(j)
          if (subtables(i)(value).isEmpty) {
            x.safeRemove(value)
            //println(s"     var:${x.id} remove new value:${value}")
            // 论域若被修改，则全局时间戳加1
            helper.varStamp(x.id) = helper.globalStamp + 1
          }
        }
        if (x.isEmpty()) {
          helper.isConsistent = false
          return false
        }
        i += 1
      }
    }
    return true
  }


  def propagate(): Boolean = {

    //println(s"c_id: ${id} propagate==========================>")
    val membersBefore = invalidTuples.size()

    // 15年论文中的伪代码每次只处理一个值
    var i = 0
    while (i < arity && helper.isConsistent) {
      val x = scope(i)

      if (oldSizes(i) != x.size()) {

        // 获得delta并更新oldSize
        removeValues.clear()
        oldSizes(i) = x.getLastRemovedValues(oldSizes(i).toLong, removeValues)
        //println(s"       var: ${x.id} removedValues: " + removeValues.mkString(", "))
        // 寻找新的无效元组
        for (a <- removeValues) {
          val sep = separators(i)(a)
          for (p <- 0 to sep) {
            val k = subtables(i)(a)(p)
            invalidTuples.add(k)
          }
        }
      }
      i += 1
    }

    // 无效元组没有更新
    val membersAfter = invalidTuples.size()
    if (membersBefore == membersAfter) {
      return true
    }

    //println(s"       the number of invalid tuple: ${membersAfter - membersBefore}")

    // 寻找没有支持的值
    var t = membersBefore
    while (t < membersAfter) {

      val k = invalidTuples.get(t)
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
            v.safeRemove(value)
            //println(s"     var:${v.id} remove new value:${value}")
            // 因为是并行不同于串行，会出现多个约束删除同一个变量不同值的情况，所以这里不能让oldSize-1
            //            oldSizes(varId) -= 1
            // 论域若被修改，则全局时间戳加1
            helper.varStamp(v.id) = helper.globalStamp + 1
            if (v.isEmpty()) {
              helper.isConsistent = false
              failWeight += 1
              return false
            }
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
      t += 1
    }
    return true
  }

  def call(): Boolean = {
    if (!helper.isConsistent) {
      return false
    }

    return propagate()
  }

  // 新层
  def newLevel(): Unit = {
    //println(s"c_id: ${id} newlevel==================>")
    level += 1
    // 向inStackS压入一个新的HashMap（对应新层）
    for (i <- 0 until arity) {
      StackS(i).push()
    }
    // 保存上层invalidTuples的边界cursize（15年论文中的member）
    invalidTuples.newLevel()
    // 到达新层后不用更改oldSize，oldSize与上层保持一致
  }

  // 回溯
  def backLevel(): Unit = {
    level -= 1
    //println(s"c_id: ${id} backlevel==================>")
    for (i <- 0 until arity) {
      // inStackS先弹出一个HashMap（当前层），再获取顶层的HashMap（上一层），将上一层的sep恢复
      //      StackS(i).pop()
      val topHash = StackS(i).pop
      // i为变量编号，a为取值，s为相应子表的sep
      for ((a, s) <- topHash) {
        separators(i)(a) = s
      }
      // 回溯后重置oldSize，新旧大小相同，因为还没有传播
      oldSizes(i) = scope(i).size()
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
