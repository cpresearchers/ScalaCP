package cpscala.TSolver.Model.Solver.IPbitSolver

import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.Model.Variable.PVar
import cpscala.XModel.XModel

class IPbitCoarseSolver(xm: XModel, parallelism: Int, propagatorName: String, varType: String, heuName: String) extends IPbitSolver(xm, parallelism, propagatorName, varType, heuName) {

  private[this] val subMask = Array.fill[Long](numBitTabs)(-1L)
  subMask(numBitTabs - 1) <<= (Constants.BITSIZE - numTabs % Constants.BITSIZE)

  def initialPropagate(): Boolean = {

    start_time = System.nanoTime
    prop_start_time = System.nanoTime

    helper.isConsistent = true

    do {
      helper.varIsChange = false
      helper.clearTableMask()
//      helper.c_sum = 0

      // 提交约束至线程池
      var i = 0
      var base = 0
      var j = 0
      var end = 0
      var cid = 0
      while (i < subMask.length) {
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

      // 获取subMask
      helper.getTableMask(subMask)
    } while (helper.varIsChange)

    return true
  }

  def checkConsistencyAfterAssignment(ix: PVar): Boolean = {

    helper.isConsistent = true

    helper.getSrb(ix.id, subMask)
    do {
      helper.varIsChange = false
      helper.clearTableMask()
//      helper.c_sum = 0

      // 提交约束至线程池
      var i = 0
      var base = 0
      var j = 0
      var end = 0
      var cid = 0
      while (i < subMask.length) {
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

      // 获取subMask
      helper.getTableMask(subMask)
    } while (helper.varIsChange)

    return true
  }

  def checkConsistencyAfterRefutation(ix: PVar): Boolean = {

    helper.isConsistent = true

    helper.getSrb(ix.id, subMask)
    do {
      helper.varIsChange = false
      helper.clearTableMask()
//      helper.c_sum = 0

      // 提交约束至线程池
      var i = 0
      var base = 0
      var j = 0
      var end = 0
      var cid = 0
      while (i < subMask.length) {
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

      // 获取subMask
      helper.getTableMask(subMask)
    } while (helper.varIsChange)

    return true
  }

}

