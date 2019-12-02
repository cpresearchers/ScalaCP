package cpscala.TSolver.Model.Solver.FDESolver

import cpscala.JModel.{JModel, JTab, JVar}
import cpscala.TSolver.CpUtil.{AssignedStack, CoarseQueue}
import cpscala.TSolver.CpUtil.SearchHelper.{FDESearchHelper, SearchHelper}
import cpscala.TSolver.Model.Constraint.FDEConstrain._
import cpscala.TSolver.Model.Constraint.SConstraint._
import cpscala.TSolver.Model.Heuristic.{HeuDomDdeg, HeuDomWdeg, Heuristic}
import cpscala.TSolver.Model.Variable._
import cpscala.XModel._

import scala.collection.mutable.ArrayBuffer

abstract class FDESolver(fdeM: FDEModel1, propagatorName: String, varType: String, heuName: String) {
  val numVars: Int = fdeM.num_vars
  val numTabs: Int = fdeM.num_tabs
  val vars = new Array[Var](numVars)
  val tabs = new Array[Propagator[Var]](numTabs)
  val helper = new FDESearchHelper(numVars, numTabs)

  //记录已赋值的变量
  val levelvsparse = Array.range(0, numVars)
  val levelvdense = Array.range(0, numVars)
  //记录已entail 的约束
  //  val levelcsparse = Array.range(0, numTabs)
  //  val levelcdense = Array.range(0, numTabs)
  //  val clevel = Array.fill(numVars + 1)(-1)

  val subscription = new Array[ArrayBuffer[Propagator[Var]]](numVars)
  for (i <- 0 until numVars) {
    subscription(i) = new ArrayBuffer[Propagator[Var]]()
  }

  // 启发式对象
  var heuristic: Heuristic[Var] = null

  // 初始化变量
  varType match {
    case "FDEBitSet" => {
      for (i <- 0 until numVars) {
        val xv: FDEVar = fdeM.vars(i)
        vars(i) = new FDEBitSetVar(xv.name, xv.id, numVars, xv.values, helper)
      }
    }

    case "BitSet" => {
      for (i <- 0 until numVars) {
        val xv: FDEVar = fdeM.vars(i)
        vars(i) = new BitSetVar(xv.name, xv.id, numVars, xv.values, helper)
      }
    }

    case "SipBIT" => {
      for (i <- 0 until numVars) {
        val xv: FDEVar = fdeM.vars(i)
        vars(i) = new SimpleBitVar(xv.name, xv.id, numVars, xv.values, helper)
      }
    }

    case "SparseSet" => {
      for (i <- 0 until numVars) {
        val xv: FDEVar = fdeM.vars(i)
        vars(i) = new SparseSetVar(xv.name, xv.id, numVars, xv.values, helper)
      }
    }

  }

  //初始化约束
  propagatorName match {

    case "FDE_CT"=>{
      for (i <- 0 until fdeM.num_OriTabs) {
        val xc: FDETab = fdeM.tabs(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new Table_CTFDE1(xc.id, xc.arity, fdeM.num_OriVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
      for (i <- fdeM.num_OriTabs until numTabs) {
        val xc: FDETab = fdeM.tabs(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i)=new Table_CTAddition(xc.id, xc.arity, fdeM.num_OriVars, scope, ts, helper)      //只包含原始变量层数为原始变量即可
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "STRbit_FDE"=>{
      for (i <- 0 until fdeM.num_OriTabs) {
        val xc: FDETab = fdeM.tabs(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new Table_STRFDE1(xc.id, xc.arity, fdeM.num_OriVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
      for (i <- fdeM.num_OriTabs until numTabs) {
        val xc: FDETab = fdeM.tabs(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i)=new Table_CTAddition(xc.id, xc.arity, fdeM.num_OriVars, scope, ts, helper)      //只包含原始变量层数为原始变量即可
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }
    case "STRbit_FDE1"=>{
      for (i <- 0 until numTabs) {
        val xc: FDETab = fdeM.tabs(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new Table_STRFDE1(xc.id, xc.arity, fdeM.num_OriVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "STR2" => {
      for (i <- 0 until numTabs) {
        val xc: FDETab = fdeM.tabs(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableSTR2_SSet(xc.id, xc.arity, numVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "STRbit_Bit" => {
      for (i <- 0 until numTabs) {
        val xc: FDETab = fdeM.tabs(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableSTRbit_Bit(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "CT_Bit" => {
      for (i <- 0 until numTabs) {
        val xc: FDETab = fdeM.tabs(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableCT_Bit(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

  }

  // 初始化启发式类
  heuName match {
    case "Dom/Ddeg" => {
      heuristic = new HeuDomDdeg[Var, Propagator[Var]](fdeM.num_OriVars, vars, subscription)
    }
    case "Dom/Wdeg" => {
      heuristic = new HeuDomWdeg[Var, Propagator[Var]](fdeM.num_OriVars, vars, subscription)
    }
  }

  helper.num_old=fdeM.num_OriVars
  var i=fdeM.num_OriVars
  var j=fdeM.num_OriTabs
  while(i<numVars){
    helper.vcMap += (j->vars(i))
    j+=1
    i+=1
  }

  val Q = new CoarseQueue[Var](numVars)
  var Y_evt: ArrayBuffer[Var] = new ArrayBuffer[Var](fdeM.max_arity)

  val I = new AssignedStack[Var](fdeM.num_OriVars)

  var start_time = 0L
  var branch_start_time = 0L
  var prop_start_time = 0L
  var back_start_time = 0L
  var end_time = 0L

  def searchMemory(): Unit={
    var finished = false
    //initial propagate
    var consistent = initialPropagate()
    System.gc()
    val mb = 1
    val runtime = Runtime.getRuntime
//    println("\nMemory in MB")
//    println("** Used Memory:  " + (runtime.totalMemory - runtime.freeMemory)/mb)
//    println("** Free Memory:  " + runtime.freeMemory/mb)
//    println("** Total Memory: " + runtime.totalMemory/mb)
//    println("** Max Memory:   " + runtime.maxMemory/mb)
    helper.memory=(runtime.totalMemory - runtime.freeMemory)/mb
  }

  def search(timeLimit: Long): Unit = {
    var finished = false

    //initial propagate
    var consistent = initialPropagate()

//    System.gc()
//    val mb = 1024*1024
//    val runtime = Runtime.getRuntime
//    println("\nMemory in MB")
//    println("** Used Memory:  " + (runtime.totalMemory - runtime.freeMemory)/mb)
//    println("** Free Memory:  " + runtime.freeMemory/mb)
//    println("** Total Memory: " + runtime.totalMemory/mb)
//    println("** Max Memory:   " + runtime.maxMemory/mb)

    end_time = System.nanoTime
    helper.propTime += (end_time - prop_start_time)
//    println(end_time - prop_start_time)

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
//            println("nodes: " + helper.nodes)
      I.push(v, a)
//            println(s"push:(${v.id}, ${a})")
      bind(v, a)
      end_time = System.nanoTime
      helper.branchTime += (end_time - branch_start_time)
      prop_start_time = System.nanoTime
      consistent = checkConsistencyAfterAssignment(v)
      end_time = System.nanoTime
      helper.propTime += (end_time - prop_start_time)
//      println(end_time - prop_start_time)

      //            infoShow()
      if (consistent && I.full()) {
        I.show()
        // 若想求出所有解，则将consistent设置为false，且不返回
//                consistent = false
//        println(helper.filterDomainTime* 1e-9)
//        println(helper.updateTableTime* 1e-9)
        end_time = System.nanoTime
        helper.time = end_time - start_time
        return
      }
      while (!consistent && !I.empty()) {
        back_start_time = System.nanoTime
        val (v, a) = I.pop()
//                println(s"pop:(${v.id}, ${a})")
        backLevel()
//        v.remove(a)
        remove(v, a)
        end_time = System.nanoTime
        helper.backTime += (end_time - back_start_time)

        prop_start_time = System.nanoTime
        consistent = !v.isEmpty() && checkConsistencyAfterRefutation(v)
        end_time = System.nanoTime
        helper.propTime += (end_time - prop_start_time)
//        println(end_time - prop_start_time)

//        infoShow()
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

  def checkConsistencyAfterAssignment(ix: Var): Boolean

  def checkConsistencyAfterRefutation(ix: Var): Boolean

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

  def remove(v: Var, a: Int): Unit = {
    //约束的已实例化变量个数减1
    for (c <- subscription(v.id)) {
      //      if (c.assignedCount.toInt != c.assignedCount)
      //        c.assignedCount -= 0.5
      //      else
      c.assignedCount -= 1
    }
    v.remove(a)
    helper.globalStamp += 1
    helper.varStamp(v.id) = helper.globalStamp
  }

  def bind(v: Var, a: Int): Unit = {
    //在稀疏集上交换变量
    val minvi = levelvsparse(v.id)
    val vid = levelvdense(helper.level - 1)
    levelvdense(helper.level - 1) = levelvdense(minvi)

    levelvsparse(vid) = minvi
    levelvsparse(levelvdense(minvi)) = helper.level - 1

    levelvdense(minvi) = vid

    for (c <- subscription(v.id)) {
      c.assignedCount += 1
    }
    v.bind(a)
    helper.globalStamp += 1
    helper.varStamp(v.id) = helper.globalStamp
  }

  def infoShow(): Unit = {
    for (x <- vars) {
      println(s"     var:${x.id} size:${x.size()}")
    }
  }

}

