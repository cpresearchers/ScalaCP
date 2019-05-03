package cpscala.TSolver.Model.Solver.DSPSolver

import cpscala.TSolver.CpUtil.SearchHelper.DSPSearchHelper
import cpscala.TSolver.CpUtil.{AssignedStack, Literal}
import cpscala.TSolver.Model.Constraint.DSPConstraint._
import cpscala.TSolver.Model.Variable.{PVar, SafeBitSetVar, SafeSimpleBitVar}
import cpscala.XModel.{XModel, XTab, XVar}

import scala.collection.mutable.ArrayBuffer

abstract class DSPSolver(xm: XModel, val parallelism: Int, propagatorName: String, varType: String, heu_name: String) {
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

  var start_time = 0L
  var branch_start_time = 0L
  var prop_start_time = 0L
  var back_start_time = 0L
  var end_time = 0L

  def shutdown(): Unit = {
    helper.pool.shutdown()
  }

  def initialPropagate(): Boolean

  def checkConsistencyAfterAssignment(ix: PVar): Boolean

  def checkConsistencyAfterRefutation(ix: PVar): Boolean

  def search(timeLimit: Long): Unit = {
    var finished = false
    //infoShow()
    //initial propagate
    //    //println("-----initial-----")
    var consistent = initialPropagate()
    end_time = System.nanoTime
    helper.propTime += (end_time - prop_start_time)
//    infoShow()
//        return

    if (!consistent) {
      finished = false
      end_time = System.nanoTime
      helper.time = end_time - start_time
      return
    }
    //
    //
    var literal: Literal[PVar] = null
    //
    while (!finished) {
      end_time = System.nanoTime
      helper.time = end_time - start_time
      if (helper.time > timeLimit) {
        return
      }

      //      if (helper.nodes == 42) {
      //        infoShow()
      //        return
      //      }

      branch_start_time = System.nanoTime
      literal = selectLiteral()
      //println("new level --------------------------------------------")
      //infoShow()
      newLevel()
      helper.nodes += 1
      //println("nodes: " + helper.nodes)

      I.push(literal)
      //println("push:" + literal.toString())
      bind(literal)
      end_time = System.nanoTime
      helper.branchTime += (end_time - branch_start_time)

      prop_start_time = System.nanoTime
      consistent = checkConsistencyAfterAssignment(literal.v)
      end_time = System.nanoTime
      helper.propTime += (end_time - prop_start_time)
      //      infoShow()

      if (consistent && I.full()) {
        //        //成功再加0.5
        //        for (c <- bitSrb(literal.v.name)) {
        //          c.assignedCount += 0.5
        //        }
        I.show()
        end_time = System.nanoTime
        helper.time = end_time - start_time
        return
      }

      while (!consistent && !I.empty()) {
        back_start_time = System.nanoTime
        literal = I.pop()
        //println("pop:" + literal.toString())
        backLevel()
        literal.v.remove(literal.a)
        remove(literal)
        end_time = System.nanoTime
        helper.backTime += (end_time - back_start_time)

        prop_start_time = System.nanoTime
        consistent = !literal.v.isEmpty() && checkConsistencyAfterRefutation(literal.v)
        end_time = System.nanoTime
        helper.propTime += (end_time - prop_start_time)
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

  //修改levelvdense
  def selectLiteral(): Literal[PVar] = {
    var mindmdd = Double.MaxValue
    var minv: PVar = null

    var i = helper.level
    while (i < numVars) {
      val vid = levelvdense(i)
      val v = vars(vid)
      var ddeg: Double = 0L

      for (c <- helper.subscription(vid)) {
        if (c.assignedCount + 1 < c.arity) {
          ddeg += 1
        }
      }

      if (ddeg == 0) {
        //        val a = v.minValue()
        //        ////println(s"(${v.id}, ${a}): ${v.simpleMask().toBinaryString}")
        return new Literal(v, v.minValue())
        //        return new Literal(v, v.dense(0))
      }

      val sizeD: Double = v.size.toDouble
      val dmdd = sizeD / ddeg

      if (dmdd < mindmdd) {
        minv = v
        mindmdd = dmdd
      }
      i += 1
    }

    //    val a = minv.minValue()
    //    ////println(s"(${minv.id}, ${a}): ${minv.simpleMask().toBinaryString}")
    return new Literal(minv, minv.minValue())
    //    return new Literal(minv, minv.dense(0))
  }

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

  def remove(literal: Literal[PVar]): Unit = {
    //约束的已实例化变量个数减1
    for (c <- helper.subscription(literal.v.id)) {
      //      if (c.assignedCount.toInt != c.assignedCount)
      //        c.assignedCount -= 0.5
      //      else
      c.assignedCount -= 1
    }
    literal.v.remove(literal.a)
    //    helper.globalStamp += 1
    //    helper.varStamp(literal.v.id) = helper.globalStamp
  }

  def bind(literal: Literal[PVar]): Unit = {
    //在稀疏集上交换变量
    val minvi = levelvsparse(literal.v.id)
    val a = levelvdense(helper.level - 1)
    levelvdense(helper.level - 1) = levelvdense(minvi)

    levelvsparse(a) = minvi
    levelvsparse(levelvdense(minvi)) = helper.level - 1

    levelvdense(minvi) = a

    for (c <- helper.subscription(literal.v.id)) {
      c.assignedCount += 1
    }
    literal.v.bind(literal.a)
    //    helper.globalStamp += 1
    //    helper.varStamp(literal.v.id) = helper.globalStamp
  }

  def infoShow(): Unit = {
    //println("---------------------------------------show-model--------------------------------------------")
    for (v <- vars) {
      v.show()
    }
    //println("---------------------------------------------------------------------------------------------")
  }
}
