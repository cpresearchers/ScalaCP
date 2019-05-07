package cpscala.TSolver.Model.Solver.SSolver

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil.{AssignedStack, CoarseQueue, Literal}
import cpscala.TSolver.Model.Constraint.SConstraint._
import cpscala.TSolver.Model.Variable._
import cpscala.XModel.{XModel, XTab, XVar}

import scala.collection.mutable.ArrayBuffer

abstract class SSolver(xm: XModel, propagatorName: String, varType: String, heuName: String) {
  val numVars: Int = xm.num_vars
  val numTabs: Int = xm.num_tabs
  val vars = new Array[Var](numVars)
  val tabs = new Array[Propagator](numTabs)
  val helper = new SearchHelper(numVars, numTabs)

  //记录已赋值的变量
  val levelvsparse = Array.range(0, numVars)
  val levelvdense = Array.range(0, numVars)
  //记录已entail 的约束
  //  val levelcsparse = Array.range(0, numTabs)
  //  val levelcdense = Array.range(0, numTabs)
  //  val clevel = Array.fill(numVars + 1)(-1)

  val subscription = new Array[ArrayBuffer[Propagator]](numVars)
  for (i <- 0 until numVars) {
    subscription(i) = new ArrayBuffer[Propagator]()
  }

  // 初始化变量
  varType match {
    case "BitSet" => {
      for (i <- 0 until numVars) {
        val xv: XVar = xm.vars.get(i)
        vars(i) = new BitSetVar(xv.name, xv.id, numVars, xv.values, helper)
      }
    }

    case "SipBIT" => {
      for (i <- 0 until numVars) {
        val xv: XVar = xm.vars.get(i)
        vars(i) = new SimpleBitVar(xv.name, xv.id, numVars, xv.values, helper)
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
    case "STR2" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableSTR2_SSet(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "STR3_SSet" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableSTR3_SSet(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "STRbit_SSet" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableSTRbit_SSet(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "STRbit_2" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableSTRbit_2(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "STRbit_1_SSet" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableSTRbit_1_SSet(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "STRbit_Bit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableSTRbit_Bit(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "CT_SSet" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableCT_SSet(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }

    case "CT_Bit" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TableCT_Bit(xc.id, xc.arity, numVars, scope, ts, helper)

        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }
  }

  val Q = new CoarseQueue[Var](numVars)
  var Y_evt: ArrayBuffer[Var] = new ArrayBuffer[Var](xm.max_arity)

  val I = new AssignedStack[Var](xm.num_vars)

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

    var literal: Literal[Var] = null

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

      helper.nodes += 1
      //println("nodes: " + helper.nodes)
      infoShow()
      branch_start_time = System.nanoTime
      literal = selectLiteral()
      newLevel()
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
        // 若想求出所有解，则将consistent设置为false，且不返回
//        consistent = false
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

  def checkConsistencyAfterAssignment(ix: Var): Boolean

  def checkConsistencyAfterRefutation(ix: Var): Boolean

  //修改levelvdense
  def selectLiteral(): Literal[Var] = {
    var mindmdd = Double.MaxValue
    var minvid = levelvdense(helper.level)

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

      if (ddeg == 0 && mindmdd == Double.MaxValue) {
        minvid = vid
      } else {
        val sizeD: Double = v.size.toDouble
        val dmdd = sizeD / ddeg

        // 变量论域若恰好为1，但又未赋值，则应先考虑论域大小大于1的变量，以尽早传播
        if (sizeD > 1 && dmdd < mindmdd) {
          minvid = vid
          mindmdd = dmdd
        }
      }

      i += 1
    }

    return new Literal(vars(minvid), vars(minvid).minValue())
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

  def remove(literal: Literal[Var]): Unit = {
    //约束的已实例化变量个数减1
    for (c <- subscription(literal.v.id)) {
      //      if (c.assignedCount.toInt != c.assignedCount)
      //        c.assignedCount -= 0.5
      //      else
      c.assignedCount -= 1
    }
    literal.v.remove(literal.a)
    helper.globalStamp += 1
    helper.varStamp(literal.v.id) = helper.globalStamp
  }

  def bind(literal: Literal[Var]): Unit = {
    //在稀疏集上交换变量
    val minvi = levelvsparse(literal.v.id)
    val a = levelvdense(helper.level - 1)
    levelvdense(helper.level - 1) = levelvdense(minvi)

    levelvsparse(a) = minvi
    levelvsparse(levelvdense(minvi)) = helper.level - 1

    levelvdense(minvi) = a

    for (c <- subscription(literal.v.id)) {
      c.assignedCount += 1
    }
    //    //println(s"bind literal is ${literal.a}")
    literal.v.bind(literal.a)
    helper.globalStamp += 1
    helper.varStamp(literal.v.id) = helper.globalStamp
  }

  def infoShow(): Unit = {
    for (x <- vars) {
      //println(s"     var:${x.id} size:${x.size()}")
    }
  }

}
