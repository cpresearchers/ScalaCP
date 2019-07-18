package cpscala.TSolver.Model.Solver.Others

import cpscala.TSolver.CpUtil.{AssignedStack, CoarseQueue, Literal}
import cpscala.TSolver.Model.Solver.SSolver.SSolver
import cpscala.TSolver.Model.Variable.{BitSetVar, Var}
import cpscala.XModel.{XModel, XTab, XVar}

import scala.collection.mutable._


class LMaxRPCSolver(xm: XModel, propagatorName: String, varType: String, heuName: String) {

  val numVars: Int = xm.num_vars
  val numTabs: Int = xm.num_tabs
  val vars = new Array[BitSetVar_LMRPC](numVars)
  val tabs = new Array[LMaxRPC_BitRM](numTabs)
  val helper = new LMaxRPCSearchHelper(numVars, numTabs, xm)

  //记录已赋值的变量
  val levelvsparse = Array.range(0, numVars)
  val levelvdense = Array.range(0, numVars)
  //记录已entail 的约束
  //  val levelcsparse = Array.range(0, numTabs)
  //  val levelcdense = Array.range(0, numTabs)
  //  val clevel = Array.fill(numVars + 1)(-1)

  val subscription = new Array[ArrayBuffer[LMaxRPC_BitRM]](numVars)
  for (i <- 0 until numVars) {
    subscription(i) = new ArrayBuffer[LMaxRPC_BitRM]()
  }

  // 初始化变量
  for (i <- 0 until numVars) {
    val xv: XVar = xm.vars.get(i)
    vars(i) = new BitSetVar_LMRPC(xv.name, xv.id, numVars, xv.values, helper)
  }

  //初始化约束
  for (i <- 0 until numTabs) {
    val xc: XTab = xm.tabs.get(i)
    val ts: Array[Array[Int]] = xc.tuples
    val scope: Array[BitSetVar_LMRPC] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
    tabs(i) = new LMaxRPC_BitRM(xc.id, xc.arity, numVars, scope, ts, helper)

    for (v <- scope) {
      subscription(v.id) += tabs(i)
    }
  }

  // 初始化搜索引擎中用到的数据结构
  val Q = new CoarseQueue[Var](numVars)
  var Y_evt: ArrayBuffer[BitSetVar_LMRPC] = new ArrayBuffer[BitSetVar_LMRPC](xm.max_arity)
  val I = new AssignedStack[Var](xm.num_vars)

  // 初始化helper中的部分数据结构
  for (c <- tabs) {
    helper.commonCon(c.scope(0).id)(c.scope(1).id) += c
    helper.commonCon(c.scope(1).id)(c.scope(0).id) += c
  }

  for (x <- vars) {
    for (y <- vars) {
      if ((x != y) && helper.commonCon(x.id)(y.id).nonEmpty) {
        helper.neiVar(x.id) += y
      }
    }
  }

  for (x <- vars) {
    for (y <- vars) {
      if ((x != y)) {
        for (z <- vars) {
          if (x != z && y != z && helper.commonCon(x.id)(z.id).nonEmpty && helper.commonCon(y.id)(z.id).nonEmpty) {
            helper.commonVar(x.id)(y.id) += z
          }
        }
      }
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

    var literal: Literal[Var] = null

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
      literal = selectLiteral()
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
      //infoShow()

      if (consistent && I.full()) {
        //        //成功再加0.5
        //        for (c <- subscription(literal.v.name)) {
        //          c.assignedCount += 0.5
        //        }
        //        I.show()
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

  def initialPropagate(): Boolean = {
    return propagate(null)
  }

  def checkConsistencyAfterAssignment(x: Var): Boolean = {
    return propagate(x)
  }

  def checkConsistencyAfterRefutation(x: Var): Boolean = {
    return propagate(x)
  }


  def propagate(x: Var): Boolean = {
    Q.clear()

    // 初始化传播队列
    if (x == null) {
      for (z <- vars) {
        Q.push(z)
      }
    } else {
      Q.push(x)
    }

    while (!Q.empty()) {
      val j = Q.pop().asInstanceOf[BitSetVar_LMRPC]

      for (i <- helper.neiVar(j.id)) {
        if (i.unBind()) {
          val c = helper.commonCon(i.id)(j.id)(0)
          Y_evt.clear()
          Y_evt += i
          Y_evt += j
          c.propagate(Y_evt)

          for (y <- Y_evt) {
            Q.push(y)
          }
        }
      }
    }

    return true
  }

  //修改levelvdense
  def selectLiteral(): Literal[Var] = {
    var mindmdd = Double.MaxValue
    var minvid = 0

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
        return new Literal(v, v.minValue())
        //        return new Literal(v, v.dense(0))
      }

      val sizeD: Double = v.size.toDouble
      val dmdd = sizeD / ddeg

      if (dmdd < mindmdd) {
        minvid = vid
        mindmdd = dmdd
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
      println(s"     var:${x.id} size:${x.size()}")
    }
  }
}
