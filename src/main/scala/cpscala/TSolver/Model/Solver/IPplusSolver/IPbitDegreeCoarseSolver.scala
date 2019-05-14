package cpscala.TSolver.Model.Solver.IPplusSolver

import cpscala.TSolver.CpUtil.{Constants, INDEX}
import cpscala.TSolver.Model.Variable.PVar
import cpscala.XModel.XModel

class IPbitDegreeCoarseSolver(xm: XModel, parallelism: Int, propagatorName: String, varType: String, heuName: String) extends IPplusSolver(xm, parallelism, propagatorName, varType, heuName) {

  private[this] val subMask = Array.fill[Long](numBitTabs)(-1L)
  // 约束邻接表，取值为degree
  private[this] val tabsAdjList = Array.fill[Int](numTabs, numTabs)(0)

  // 初始化约束邻接表
  for (i <- 0 until numTabs) {
    for (v <- tabs(i).scope) {
      for (c <- subscription(v.id)) {
        if (c.id != i) {
          tabsAdjList(i)(c.id) += 1
        }
      }
    }
  }

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
      var lastcid = -1
      var mincid = 0
      var minDegree = 0
      var continue = true

      while (i < subMask.length && continue) {
        val mask = subMask(i)
        if (mask != 0) {
          j = Constants.FirstLeft(mask)
          lastcid = i * Constants.BITSIZE + j
          subMask(i) &= Constants.MASK0(j)
          continue = false
        }
        i += 1
      }
      helper.submitToPool(tabs(lastcid))
      numSubTabs -= 1

      while (numSubTabs > 0 && helper.isConsistent) {
        i = 0
        minDegree = Int.MaxValue
        continue = true
        // 寻找degree最小的约束
        while (i < subMask.length && continue) {
          val mask = subMask(i)
          if (mask != 0) {
            base = i * Constants.BITSIZE
            j = Constants.FirstLeft(mask)
            end = Constants.FirstRight(mask)
            while (j <= end && continue) {
              if ((mask & Constants.MASK1(j)) != 0) {
                cid = j + base
                if (tabsAdjList(lastcid)(cid) < minDegree) {
                  minDegree = tabsAdjList(lastcid)(cid)
                  mincid = cid
                  if (minDegree == 0) {
                    continue = false
                  }
                }
              }
              j += 1
            }
          }
          i += 1
        }
        helper.submitToPool(tabs(mincid))
        lastcid = mincid
        // 将被提交约束对应的bit位置0
        val (x, y) = INDEX.getXY(mincid)
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
      var lastcid = -1
      var mincid = 0
      var minDegree = 0
      var continue = true

      while (i < subMask.length && continue) {
        val mask = subMask(i)
        if (mask != 0) {
          j = Constants.FirstLeft(mask)
          lastcid = i * Constants.BITSIZE + j
          subMask(i) &= Constants.MASK0(j)
          continue = false
        }
        i += 1
      }
      helper.submitToPool(tabs(lastcid))
      numSubTabs -= 1

      while (numSubTabs > 0 && helper.isConsistent) {
        i = 0
        minDegree = Int.MaxValue
        continue = true
        // 寻找degree最小的约束
        while (i < subMask.length && continue) {
          val mask = subMask(i)
          if (mask != 0) {
            base = i * Constants.BITSIZE
            j = Constants.FirstLeft(mask)
            end = Constants.FirstRight(mask)
            while (j <= end && continue) {
              if ((mask & Constants.MASK1(j)) != 0) {
                cid = j + base
                if (tabsAdjList(lastcid)(cid) < minDegree) {
                  minDegree = tabsAdjList(lastcid)(cid)
                  mincid = cid
                  if (minDegree == 0) {
                    continue = false
                  }
                }
              }
              j += 1
            }
          }
          i += 1
        }
        helper.submitToPool(tabs(mincid))
        lastcid = mincid
        // 将被提交约束对应的bit位置0
        val (x, y) = INDEX.getXY(mincid)
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
      var lastcid = -1
      var mincid = 0
      var minDegree = 0
      var continue = true

      while (i < subMask.length && continue) {
        val mask = subMask(i)
        if (mask != 0) {
          j = Constants.FirstLeft(mask)
          lastcid = i * Constants.BITSIZE + j
          subMask(i) &= Constants.MASK0(j)
          continue = false
        }
        i += 1
      }
      helper.submitToPool(tabs(lastcid))
      numSubTabs -= 1

      while (numSubTabs > 0 && helper.isConsistent) {
        i = 0
        minDegree = Int.MaxValue
        continue = true
        // 寻找degree最小的约束
        while (i < subMask.length && continue) {
          val mask = subMask(i)
          if (mask != 0) {
            base = i * Constants.BITSIZE
            j = Constants.FirstLeft(mask)
            end = Constants.FirstRight(mask)
            while (j <= end && continue) {
              if ((mask & Constants.MASK1(j)) != 0) {
                cid = j + base
                if (tabsAdjList(lastcid)(cid) < minDegree) {
                  minDegree = tabsAdjList(lastcid)(cid)
                  mincid = cid
                  if (minDegree == 0) {
                    continue = false
                  }
                }
              }
              j += 1
            }
          }
          i += 1
        }
        helper.submitToPool(tabs(mincid))
        lastcid = mincid
        // 将被提交约束对应的bit位置0
        val (x, y) = INDEX.getXY(mincid)
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


