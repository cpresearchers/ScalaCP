package cpscala.TSolver.Model.Solver.IPplusSolver

import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.Model.Variable.PVar
import cpscala.XModel.XModel

class IPbitFineSolver(xm: XModel, parallelism: Int, propagatorName: String, varType: String, heuName: String) extends IPplusSolver(xm, parallelism, propagatorName, varType, heuName) {

  private[this] val subMask = Array.fill[Long](numBitTabs)(-1L)

  def initialPropagate(): Boolean = {

    // 约束表初始化
    for (c <- tabs) {
      c.setup()
    }

    start_time = System.nanoTime
    prop_start_time = System.nanoTime

    // 初始删值
    for (c <- tabs) {
      c.setup()
    }
    if (!helper.isConsistent) {
      return false
    }

    var numSubTabs = 0
    helper.isConsistent = true

    do {
      // 获取subMask以及待提交的约束个数
      numSubTabs = helper.getTableMask(subMask)
      helper.clearTableMask()
      helper.varIsChange = false
      //      helper.c_sum = 0

      // 提交约束至线程池
      var i = 0
      var base = 0
      var j = 0
      var end = 0
      var cid = 0
      while (i < subMask.length && helper.isConsistent) {
        val a = subMask(i)
        if (a != 0) {
          base = i * Constants.BITSIZE
          j = Constants.FirstLeft(a)
          end = Constants.FirstRight(a)
          while (j <= end) {
            if ((a & Constants.MASK1(j)) != 0) {
              cid = j + base
              helper.c_sum += 1
              //println(s"${cid} cons submits")
              helper.submitToPool(tabs(cid))
            }
            j += 1
          }
        }
        i += 1
      }

      helper.poolAwait()
      helper.p_sum += 1
      //println(s"  ${helper.p_sum} p_sum's c_sum: ${helper.c_sum}")

      if (!helper.isConsistent) {
        return false
      }

    } while (helper.varIsChange)

    return true
  }

  def checkConsistencyAfterAssignment(ix: PVar): Boolean = {

    var numSubTabs = 0
    helper.clearTableMask()
    helper.addToTableMask(ix.id)
    helper.isConsistent = true

    do {
      // 获取subMask以及待提交的约束个数
      numSubTabs = helper.getTableMask(subMask)
      helper.clearTableMask()
      helper.varIsChange = false
      //      helper.c_sum = 0

      // 提交约束至线程池
      var i = 0
      var base = 0
      var j = 0
      var end = 0
      var cid = 0
      while (i < subMask.length && helper.isConsistent) {
        val a = subMask(i)
        if (a != 0) {
          base = i * Constants.BITSIZE
          j = Constants.FirstLeft(a)
          end = Constants.FirstRight(a)
          while (j <= end) {
            if ((a & Constants.MASK1(j)) != 0) {
              cid = j + base
              helper.c_sum += 1
              //println(s"${cid} cons is submitted")
              helper.submitToPool(tabs(cid))
            }
            j += 1
          }
        }
        i += 1
      }
      helper.poolAwait()
      helper.p_sum += 1
      //println(s"  ${helper.p_sum} p_sum's c_sum: ${helper.c_sum}")


      if (!helper.isConsistent) {
        return false
      }

    } while (helper.varIsChange)

    return true
  }

  def checkConsistencyAfterRefutation(ix: PVar): Boolean = {

    var numSubTabs = 0
    helper.clearTableMask()
    helper.addToTableMask(ix.id)
    helper.isConsistent = true

    do {
      // 获取subMask以及待提交的约束个数
      numSubTabs = helper.getTableMask(subMask)
      helper.clearTableMask()
      helper.varIsChange = false
      //      helper.c_sum = 0

      // 提交约束至线程池
      var i = 0
      var base = 0
      var j = 0
      var end = 0
      var cid = 0
      while (i < subMask.length && helper.isConsistent) {
        val a = subMask(i)
        if (a != 0) {
          base = i * Constants.BITSIZE
          j = Constants.FirstLeft(a)
          end = Constants.FirstRight(a)
          while (j <= end) {
            if ((a & Constants.MASK1(j)) != 0) {
              cid = j + base
              helper.c_sum += 1
              //println(s"${cid} cons is submitted")
              helper.submitToPool(tabs(cid))
            }
            j += 1
          }
        }
        i += 1
      }
      helper.poolAwait()
      helper.p_sum += 1
      //println(s"  ${helper.p_sum} p_sum's c_sum: ${helper.c_sum}")

      if (!helper.isConsistent) {
        return false
      }

    } while (helper.varIsChange)

    return true
  }

}

