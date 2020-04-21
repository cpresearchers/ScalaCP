package cpscala.TSolver.Model.Solver.DSPFDESolver

import cpscala.TSolver.CpUtil.AssignedStack
import cpscala.TSolver.CpUtil.SearchHelper.{DSPFDESearchHelper, DSPSearchHelper, FDESearchHelper}
import cpscala.TSolver.Model.Constraint.DSPConstraint.{DSPPropagator, TableDSPCT_SBit, TableDSPCT_SSBit, TableDSPSTR3_SBit, TableDSPSTRbit_SBit}
import cpscala.TSolver.Model.Constraint.DSPFDEConstraint.{TableDSPFDECT, TableDSPFDESTR}
import cpscala.TSolver.Model.Constraint.FDEConstrain.{Table_CTAddition, Table_STRFDE1}
import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Heuristic.{HeuDomDdeg, HeuDomWdeg, Heuristic}
import cpscala.TSolver.Model.Variable.{BitSetVar, FDEBitSetVar, PVar, SafeBitSetVar, SafeFDEBitSetVar, SafeSimpleBitVar, SimpleBitVar, SparseSetVar, Var}
import cpscala.XModel.{FDEModel1, FDETab, FDEVar, XModel, XTab, XVar}

import scala.collection.mutable.ArrayBuffer

abstract class DSPFDESolver(fdeM: FDEModel1, val parallelism: Int, propagatorName: String, varType: String, heuName: String) {
  val numVars: Int = fdeM.num_vars
  val numTabs: Int = fdeM.num_tabs
  val vars = new Array[PVar](numVars)
  val tabs = new Array[DSPPropagator](numTabs)
  val helper = new DSPFDESearchHelper(numVars, numTabs, parallelism)

  //记录已赋值的变量
  val levelvsparse = Array.range(0, numVars)
  val levelvdense = Array.range(0, numVars)
  //记录已entail 的约束
  //  val levelcsparse = Array.range(0, numTabs)
  //  val levelcdense = Array.range(0, numTabs)
  //  val clevel = Array.fill(numVars + 1)(-1)

  val subscription = new Array[ArrayBuffer[DSPPropagator]](numVars)
  for (i <- 0 until numVars) {
    subscription(i) = new ArrayBuffer[DSPPropagator]()
  }

  val I = new AssignedStack[PVar](numVars)

  // 启发式对象
  var heuristic: Heuristic[PVar] = null

  // 初始化变量
  varType match {
    case "SafeFDEBitSet" => {
      for (i <- 0 until numVars) {
        val xv: FDEVar = fdeM.vars(i)
        vars(i) = new SafeFDEBitSetVar(xv.name, xv.id, numVars, xv.values, helper)
      }
    }
  }

  val Xevt = new ArrayBuffer[PVar]()

  //初始化约束
  propagatorName match {
    case "STRbit_FDE" => {
      for (i <- 0 until fdeM.num_OriTabs) {
        val xc: FDETab = fdeM.tabs(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        // str fde
        tabs(i) = new TableDSPFDESTR(xc.id, xc.arity, fdeM.num_OriVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
      for (i <- fdeM.num_OriTabs until numTabs) {
        val xc: FDETab = fdeM.tabs(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableDSPFDECT(xc.id, xc.arity, fdeM.num_OriVars, scope, ts, helper) //只包含原始变量层数为原始变量即可
        for (v <- scope) {
          subscription(v.id) += tabs(i)
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
