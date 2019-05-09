package cpscala.TSolver.Model.Solver.DSPSolver

import cpscala.TSolver.CpUtil.SearchHelper.DSPSearchHelper
import cpscala.TSolver.CpUtil.{AssignedStack}
import cpscala.TSolver.Model.Constraint.DSPConstraint._
import cpscala.TSolver.Model.Heuristic.{HeuDomDdeg, HeuDomWdeg, Heuristic}
import cpscala.TSolver.Model.Variable.{PVar, SafeBitSetVar, SafeSimpleBitVar}
import cpscala.XModel.{XModel, XTab, XVar}

import scala.collection.mutable.ArrayBuffer

abstract class DSPSolver(xm: XModel, val parallelism: Int, propagatorName: String, varType: String, heuName: String) {
  val vars = new Array[PVar](xm.num_vars)
  val tabs = new Array[DSPPropagator](xm.num_tabs)
  val numVars = xm.num_vars
  val numTabs = xm.num_tabs
  val ma = xm.max_arity
  val mds = xm.max_domain_size
  val helper = new DSPSearchHelper(numVars, numTabs, parallelism)
  //记录已赋值的变量
  val levelvsparse = Array.range(0, numVars)
  val levelvdense = Array.range(0, numVars)
  val I = new AssignedStack[PVar](numVars)

  // 启发式对象
  var heuristic: Heuristic[PVar] = null

  // 初始化变量

  varType match {
    case "SafeSimpleBit" => {
      for (i <- 0 until numVars) {
        val xv: XVar = xm.vars.get(i)
        vars(i) = new SafeSimpleBitVar(xv.name, xv.id, numVars, xv.values, helper)
      }
    }

    case "SafeBitSet" => {
      for (i <- 0 until numVars) {
        val xv: XVar = xm.vars.get(i)
        vars(i) = new SafeBitSetVar(xv.name, xv.id, numVars, xv.values, helper)
      }
    }
  }

  val Xevt = new ArrayBuffer[PVar]()

  //初始化约束
  propagatorName match {
    case "DSPCT_SBit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableDSPCT_SBit(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          helper.subscription(v.id) += tabs(i)
        }
      }
    }

    case "DSPSTRbit_SBit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableDSPSTRbit_SBit(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          helper.subscription(v.id) += tabs(i)
        }
      }
    }

    case "DSPSTR3_SBit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableDSPSTR3_SBit(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          helper.subscription(v.id) += tabs(i)
        }
      }
    }

    case "DSPCT_SSBit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableDSPCT_SSBit(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          helper.subscription(v.id) += tabs(i)
        }
      }
    }

    case _ => {
      //println("no math constraint type!")
    }
  }

  // 初始化启发式对象
  heuName match {
    case "Dom/Ddeg" => {
      heuristic = new HeuDomDdeg[PVar, DSPPropagator](numVars, vars, helper.subscription)
    }

    case "Dom/Wdeg" => {
      heuristic = new HeuDomWdeg[PVar, DSPPropagator](numVars, vars, helper.subscription)
    }
  }

  var start_time = 0L
  var branch_start_time = 0L
  var prop_start_time = 0L
  var back_start_time = 0L
  var end_time = 0L

  def shutdown(): Unit = {
    helper.pool.shutdown()
  }

  def search(timeLimit: Long): Unit = {
    var finished = false

    //initial propagate
    var consistent = initialPropagate()
    end_time = System.nanoTime
    helper.propTime += (end_time - prop_start_time)

    //infoShow()
    //    return

    if (!consistent) {
      finished = false
      end_time = System.nanoTime
      helper.time = end_time - start_time
      return
    }

    while (!finished) {
      end_time = System.nanoTime
      helper.time = end_time - start_time
      if (helper.time > timeLimit) {
        return
      }

      //      if (helper.nodes == 4) {
      //        infoShow()
      //        return
      //      }

      //      infoShow()
      branch_start_time = System.nanoTime
      val (v, a) = heuristic.selectLiteral(helper.level, levelvdense)
      newLevel()
      helper.nodes += 1
      //      println("nodes: " + helper.nodes)

      I.push(v, a)
      //      println(s"push:(${v.id}, ${a})")
      bind(v, a)
      end_time = System.nanoTime
      helper.branchTime += (end_time - branch_start_time)


      prop_start_time = System.nanoTime
      consistent = checkConsistencyAfterAssignment(v)
      end_time = System.nanoTime
      helper.propTime += (end_time - prop_start_time)
      //            infoShow()

      if (consistent && I.full()) {
        I.show()
        // 若想求出所有解，则将consistent设置为false，且不返回
        //        consistent = false
        end_time = System.nanoTime
        helper.time = end_time - start_time
        return
      }

      while (!consistent && !I.empty()) {
        back_start_time = System.nanoTime
        val (v, a) = I.pop()
        //        println(s"pop:(${v.id}, ${a})")
        backLevel()
        v.remove(a)
        remove(v, a)
        end_time = System.nanoTime
        helper.backTime += (end_time - back_start_time)

        prop_start_time = System.nanoTime
        consistent = !v.isEmpty() && checkConsistencyAfterRefutation(v)
        end_time = System.nanoTime
        helper.propTime += (end_time - prop_start_time)
        //infoShow()
      }

      if (!consistent) {
        finished = true
      }
    }
    end_time = System.nanoTime
    helper.time = end_time - start_time
    return
  }

  def initialPropagate(): Boolean

  def checkConsistencyAfterAssignment(ix: PVar): Boolean

  def checkConsistencyAfterRefutation(ix: PVar): Boolean

  def newLevel(): Unit = {
    helper.level += 1
    for (v <- vars) {
      v.newLevel()
    }
    for (c <- tabs) {
      c.newLevel()
    }
  }

  def backLevel(): Unit = {
    helper.level -= 1
    for (v <- vars) {
      v.backLevel()
    }
    for (c <- tabs) {
      c.backLevel()
    }
  }

  def remove(v: PVar, a: Int): Unit = {
    //约束的已实例化变量个数减1
    for (c <- helper.subscription(v.id)) {
      //      if (c.assignedCount.toInt != c.assignedCount)
      //        c.assignedCount -= 0.5
      //      else
      c.assignedCount -= 1
    }
    v.remove(a)
  }

  def bind(v: PVar, a: Int): Unit = {
    //在稀疏集上交换变量
    val minvi = levelvsparse(v.id)
    val vid = levelvdense(helper.level - 1)
    levelvdense(helper.level - 1) = levelvdense(minvi)

    levelvsparse(vid) = minvi
    levelvsparse(levelvdense(minvi)) = helper.level - 1

    levelvdense(minvi) = vid

    for (c <- helper.subscription(v.id)) {
      c.assignedCount += 1
    }
    v.bind(a)
  }

  def infoShow(): Unit = {
    //println("---------------------------------------show-model--------------------------------------------")
    for (v <- vars) {
      v.show()
    }
    //println("---------------------------------------------------------------------------------------------")
  }
}
