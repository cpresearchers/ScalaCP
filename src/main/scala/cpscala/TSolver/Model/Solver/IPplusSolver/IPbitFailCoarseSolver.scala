package cpscala.TSolver.Model.Solver.IPplusSolver

import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.Model.Variable.PVar
import cpscala.XModel.XModel

// 按照failWeight由大到小的顺序提交约束至线程池
class IPbitFailCoarseSolver(xm: XModel, parallelism: Int, propagatorName: String, varType: String, heuName: String) extends IPplusSolver(xm, parallelism, propagatorName, varType, heuName) {

  private[this] val subMask = Array.fill[Long](numBitTabs)(-1L)

  def initialPropagate(): Boolean = {

    start_time = System.nanoTime
    prop_start_time = System.nanoTime

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
      var maxcid = 0
      var maxFailWeight = -1

      while (numSubTabs > 0 && helper.isConsistent) {
        i = 0
        maxFailWeight = -1
        // 寻找failWeight最大的约束
        while (i < subMask.length) {
          val mask = subMask(i)
          if (mask != 0) {
            base = i * Constants.BITSIZE
            j = Constants.FirstLeft(mask)
            end = Constants.FirstRight(mask)
            while (j <= end) {
              if ((mask & Constants.MASK1(j)) != 0) {
                cid = j + base
                if (tabs(cid).failWeight > maxFailWeight) {
                  maxFailWeight = tabs(cid).failWeight
                  maxcid = cid
                }
              }
              j += 1
            }
          }
          i += 1
        }
        helper.submitToPool(tabs(maxcid))
        // 将被提交约束对应的bit位置0
        val (x, y) = INDEX.getXY(maxcid)
        subMask(x) &= Constants.MASK0(y)
        helper.c_sum += 1
        numSubTabs -= 1
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
      var maxcid = 0
      var maxFailWeight = -1

      while (numSubTabs > 0 && helper.isConsistent) {
        i = 0
        maxFailWeight = -1
        // 寻找failWeight最大的约束
        while (i < subMask.length) {
          val mask = subMask(i)
          if (mask != 0) {
            base = i * Constants.BITSIZE
            j = Constants.FirstLeft(mask)
            end = Constants.FirstRight(mask)
            while (j <= end) {
              if ((mask & Constants.MASK1(j)) != 0) {
                cid = j + base
                if (tabs(cid).failWeight > maxFailWeight) {
                  maxFailWeight = tabs(cid).failWeight
                  maxcid = cid
                }
              }
              j += 1
            }
          }
          i += 1
        }
        helper.submitToPool(tabs(maxcid))
        // 将被提交约束对应的bit位置0
        val (x, y) = INDEX.getXY(maxcid)
        subMask(x) &= Constants.MASK0(y)
        helper.c_sum += 1
        numSubTabs -= 1
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
      var maxcid = 0
      var maxFailWeight = -1

      while (numSubTabs > 0 && helper.isConsistent) {
        i = 0
        maxFailWeight = -1
        // 寻找failWeight最大的约束
        while (i < subMask.length) {
          val mask = subMask(i)
          if (mask != 0) {
            base = i * Constants.BITSIZE
            j = Constants.FirstLeft(mask)
            end = Constants.FirstRight(mask)
            while (j <= end) {
              if ((mask & Constants.MASK1(j)) != 0) {
                cid = j + base
                if (tabs(cid).failWeight > maxFailWeight) {
                  maxFailWeight = tabs(cid).failWeight
                  maxcid = cid
                }
              }
              j += 1
            }
          }
          i += 1
        }
        helper.submitToPool(tabs(maxcid))
        // 将被提交约束对应的bit位置0
        val (x, y) = INDEX.getXY(maxcid)
        subMask(x) &= Constants.MASK0(y)
        helper.c_sum += 1
        numSubTabs -= 1
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

