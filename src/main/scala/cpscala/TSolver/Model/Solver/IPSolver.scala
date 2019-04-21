package cpscala.TSolver.Model.Solver

import java.util.ArrayList

import cpscala.TSolver.Model.Constraint.IPConstraint._
import cpscala.TSolver.CpUtil.SearchHelper.IPSearchHelper
import cpscala.TSolver.CpUtil.{CoarseQueue, PAssignedStack, PVal}
import cpscala.TSolver.Model.Variable.{PVar, SafeBitSetVar, SafeSimpleBitVar, SparseSetVar}
import cpscala.XModel.{XModel, XTab, XVar}

import scala.collection.mutable.ArrayBuffer

abstract class IPSolver(xm: XModel, parallelism: Int, propagatorName: String, varType: String, heuName: String) {

  val vars = new Array[PVar](xm.num_vars)
  val tabs = new Array[IPPropagator](xm.num_tabs)
  val numVars = xm.num_vars
  val numTabs = xm.num_tabs
  val ma = xm.max_arity
  val mds = xm.max_domain_size

  val subscription = new Array[ArrayBuffer[IPPropagator]](numVars)
  for (i <- 0 until numVars) {
    subscription(i) = new ArrayBuffer[IPPropagator]()
  }

  //记录已赋值的变量
  val levelvsparse = Array.range(0, numVars)
  val levelvdense = Array.range(0, numVars)
  val I = new PAssignedStack(numVars)

  val Q = new CoarseQueue[PVar](numVars)
  val Yevt = new ArrayBuffer[PVar](numVars)
  val Cevt = new ArrayList[IPPropagator](numTabs)
  //inCevt[i]表示第i个约束是否在Cevt中
  val inCevt = Array.fill(numTabs)(false)

  val helper = new IPSearchHelper(numVars, numTabs, parallelism)
  //时间戳
  helper.globalStamp = 0L


  // 初始化变量
  varType match {

    case "SafeBitSet" => {
      for (i <- 0 until numVars) {
        val xv: XVar = xm.vars.get(i)
        vars(i) = new SafeBitSetVar(xv.name, xv.id, numVars, xv.values, helper)
      }
    }

    case "SafeSimpleBit" => {
      for (i <- 0 until numVars) {
        val xv: XVar = xm.vars.get(i)
        vars(i) = new SafeSimpleBitVar(xv.name, xv.id, numVars, xv.values, helper)
      }
    }

    case "SparseSet" => {
      for (i <- 0 until numVars) {
        val xv: XVar = xm.vars.get(i)
        vars(i) = new SparseSetVar(xv.name, xv.id, numVars, xv.values, helper)
      }
    }
  }

  //初始化约束
  propagatorName match {
    case "IPSTR2_SSBit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))

        tabs(i) = new TableIPSTR2_SSBit(xc.id, xc.arity, numVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "IPSTR3_SSet" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))

        tabs(i) = new TableIPSTR3_SSet(xc.id, xc.arity, numVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "IPSTR3_SSBit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))

        tabs(i) = new TableIPSTR3_SSBit(xc.id, xc.arity, numVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "IPSTR3_SBit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))

        tabs(i) = new TableIPSTR3_SBit(xc.id, xc.arity, numVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "IPSTRbit_SSet" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))

        tabs(i) = new TableIPSTRbit_SSet(xc.id, xc.arity, numVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "IPSTRbit_SSBit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))

        tabs(i) = new TableIPSTRbit_SSBit(xc.id, xc.arity, numVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "IPCT_SBit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))

        tabs(i) = new TableIPCT_SBit(xc.id, xc.arity, numVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "IPCT_SSBit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[PVar] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))

        tabs(i) = new TableIPCT_SSBit(xc.id, xc.arity, numVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }
  }

  def ClearInCevt() = {
    var i = 0
    while (i < numTabs) {
      inCevt(i) = false
      i += 1
    }
  }

  var start_time = 0L
  var branch_start_time = 0L
  var prop_start_time = 0L
  var back_start_time = 0L
  var end_time = 0L

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

    var v_a: PVal = null

    while (!finished) {
      end_time = System.nanoTime
      helper.time = end_time - start_time
      if (helper.time > timeLimit) {
        return
      }

      //      if (helper.nodes == 8) {
      //                //infoShow()
      //        return
      //      }

      branch_start_time = System.nanoTime
      v_a = select_val()
      new_level()
      helper.nodes += 1
      //println("nodes: " + helper.nodes)
      I.push(v_a)
      //println("push:" + v_a.toString())
      bind(v_a)


      end_time = System.nanoTime
      helper.branchTime += (end_time - branch_start_time)


      prop_start_time = System.nanoTime
      consistent = checkConsistencyAfterAssignment(v_a.v.asInstanceOf[PVar])
      end_time = System.nanoTime
      helper.propTime += (end_time - prop_start_time)
      //infoShow()

      if (consistent && I.full()) {
        //        //成功再加0.5
        //        for (c <- subscription(v_a.v.name)) {
        //          c.assignedCount += 0.5
        //        }
        I.show()
        end_time = System.nanoTime
        helper.time = end_time - start_time
        return
      }

      while (!consistent && !I.empty()) {
        back_start_time = System.nanoTime
        v_a = I.pop()
        //println("pop:" + v_a.toString())
        back_level()
        v_a.v.remove(v_a.a)
        remove(v_a)
        end_time = System.nanoTime
        helper.backTime += (end_time - back_start_time)

        prop_start_time = System.nanoTime
        consistent = !v_a.v.isEmpty() && checkConsistencyAfterRefutation(v_a.v.asInstanceOf[PVar])
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

  def shutdown(): Unit = {
    helper.pool.shutdown()
  }

  def initialPropagate(): Boolean

  def checkConsistencyAfterAssignment(ix: PVar): Boolean

  def checkConsistencyAfterRefutation(ix: PVar): Boolean

  //修改levelvdense
  def select_val(): PVal = {
    var mindmdd = Double.MaxValue
    var minv: PVar = null

    var i = helper.level
    while (i < numVars) {
      val vid = levelvdense(i)
      val v = vars(vid)
      var ddeg: Double = 0L

      for (c <- subscription(vid)) {
        if (c.assignedCount + 1 < c.arity) {
          ddeg += 1
        }
      }

      if (ddeg == 0) {
        //                val a = v.minValue()
        //        //println(s"(${v.id}, ${a}): ${v.simpleMask().toBinaryString}")
        return new PVal(v, v.minValue())
        //        return new Val(v, v.dense(0))
      }

      val sizeD: Double = v.size.toDouble
      val dmdd = sizeD / ddeg

      if (dmdd < mindmdd) {
        minv = v
        mindmdd = dmdd
      }
      i += 1
    }

    //        val a = minv.minValue()
    //        //println(s"(${minv.id}, ${a})")
    //        //println(s"(${minv.id}, ${a}): ${minv.simpleMask().toBinaryString}")
    return new PVal(minv, minv.minValue())
    //    return new Val(minv, minv.dense(0))
  }

  def new_level(): Unit = {
    helper.level += 1
    for (v <- vars) {
      v.newLevel()
    }

    //    helper.searchState = 1
    //    Cevt.clear()
    for (c <- tabs) {
      c.newLevel()
      //      Cevt.add(c)
      //      helper.pool.submit(c)
    }
    //    helper.pool.invokeAll(Cevt)
    //    //println("check newLevel")
  }

  def back_level(): Unit = {
    helper.level -= 1
    for (v <- vars) {
      v.backLevel()
    }

    //        helper.searchState = 3
    //        Cevt.clear()

    for (c <- tabs) {
      //            Cevt.add(c)
      c.backLevel()
    }

    //        helper.pool.invokeAll(Cevt)
    //println("check backLevel")
  }

  def remove(v_a: PVal): Unit = {
    //约束的已实例化变量个数减1
    for (c <- subscription(v_a.v.id)) {
      //      if (c.assignedCount.toInt != c.assignedCount)
      //        c.assignedCount -= 0.5
      //      else
      c.assignedCount -= 1
    }
    v_a.v.remove(v_a.a)
    helper.globalStamp += 1
    helper.varStamp(v_a.v.id) = helper.globalStamp
  }

  def bind(v_a: PVal): Unit = {
    //在稀疏集上交换变量
    val minvi = levelvsparse(v_a.v.id)
    val a = levelvdense(helper.level - 1)
    levelvdense(helper.level - 1) = levelvdense(minvi)

    levelvsparse(a) = minvi
    levelvsparse(levelvdense(minvi)) = helper.level - 1

    levelvdense(minvi) = a

    for (c <- subscription(v_a.v.id)) {
      c.assignedCount += 1
    }
    //    //println(s"bind v_a is ${v_a.a}")
    v_a.v.bind(v_a.a)
    helper.globalStamp += 1
    helper.varStamp(v_a.v.id) = helper.globalStamp
  }

  def infoShow(): Unit = {
    //println("---------------------------------------show-model--------------------------------------------")
    for (v <- vars) {
      v.show()
    }
    //println("---------------------------------------------------------------------------------------------")
  }
}
