package cpscala.TSolver.Model.Solver.Others

import cpscala.TSolver.CpUtil.{AssignedStack, CoarseQueue, Literal}
import cpscala.TSolver.Model.Solver.SSolver.SSolver
import cpscala.TSolver.Model.Variable.{BitSetVar, Var}
import cpscala.XModel.{XModel, XTab, XVar}

import scala.collection.mutable
import scala.collection.mutable._
import scala.util.control.Breaks.{break, breakable}


class LMXPSolver(xm: XModel, parallelism: Int) {

  class LCSync(val x: Var, val m: MultiLevel) extends Thread {
    override def run(): Unit = {
      LMX(x, m)
    }
  }

  class LCAsync(val x: Var, val m: MultiLevel) extends Thread {
    override def run(): Unit = {
      LMXAsync(x, m)
    }
  }

//  object LCState extends Enumeration {
//    type LCState = Value
//    val Idle = Value(0)
//    val NeedStop = Value(1)
//    val Running = Value(2)
//  }

  val numVars: Int = xm.num_vars
  val numTabs: Int = xm.num_tabs
  val vars = new Array[BitSetVar_LMX](numVars)
  val tabs = new Array[LMX_Bit](numTabs)
  val helper = new LMXSearchHelper(numVars, numTabs, xm)

  // 记录已赋值的变量
  val levelvsparse = Array.range(0, numVars)
  val levelvdense = Array.range(0, numVars)
  val subscription = new Array[ArrayBuffer[LMX_Bit]](numVars)

  for (i <- 0 until numVars) {
    subscription(i) = new ArrayBuffer[LMX_Bit]()
  }

  // 初始化变量
  for (i <- 0 until numVars) {
    val xv: XVar = xm.vars.get(i)
    vars(i) = new BitSetVar_LMX(xv.name, xv.id, numVars, xv.values, helper, parallelism)
  }

  // 初始化约束
  for (i <- 0 until numTabs) {
    val xc: XTab = xm.tabs.get(i)
    val ts: Array[Array[Int]] = xc.tuples
    val scope: Array[BitSetVar_LMX] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
    tabs(i) = new LMX_Bit(xc.id, xc.arity, numVars, scope, ts, helper, parallelism)

    for (v <- scope) {
      subscription(v.id) += tabs(i)
    }
  }

  // 初始化搜索引擎中用到的数据结构
  val Q = new CoarseQueue[Var](numVars)
  var Y_evt: ArrayBuffer[BitSetVar_LMX] = new ArrayBuffer[BitSetVar_LMX](xm.max_arity)
  val I = new AssignedStack[Var](xm.num_vars)
  val L = new LMXSparseSet(parallelism, numVars)

  val M = new mutable.HashMap[MultiLevel, LCSync]()
  // =0 是idle
  // =1 是needstop
  // =2 是running
  // =3 是完成
  val LCState = new mutable.HashMap[MultiLevel, Int]()
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

  def sync(timeLimit: Long): Unit = {
    //    initial propagate
    //    println("initial propagate")
    //    infoShow()
    var finished = false
    start_time = System.nanoTime
    val m = newTmpLevel()
    M += (m -> new LCSync(null, m))
    val lc = M(m)
    //    lc.run()
    lc.start()
    AC(null)
    lc.join()
    M -= m
    deleteTmpLevel(m)

    end_time = System.nanoTime
    helper.propTime += (end_time - prop_start_time)

    if (!helper.isConsistent) {
      finished = false
      end_time = System.nanoTime
      helper.time = end_time - start_time
      return
    }
    //
    var literal: Literal[Var] = null
    //
    while (!finished) {
      end_time = System.nanoTime
      helper.time = end_time - start_time
      if (helper.time > timeLimit) {
        return
      }

      branch_start_time = System.nanoTime
      literal = selectLiteral()
      newLevel()
      helper.nodes += 1
      I.push(literal)
      bind(literal)
      end_time = System.nanoTime
      helper.branchTime += (end_time - branch_start_time)

      //      println("push:" + literal.toString())
      //      infoShow()

      prop_start_time = System.nanoTime
      helper.ACFinished = false
      val m = newTmpLevel()
      M += (m -> new LCSync(literal.v, m))
      val lc = M(m)
      lc.start()
      AC(literal.v)
      lc.join()
      M -= m
      deleteTmpLevel(m)

      end_time = System.nanoTime
      helper.propTime += (end_time - prop_start_time)

      //      println("--------")
      //      infoShow()

      if (helper.isConsistent && I.full()) {
        I.show()
        end_time = System.nanoTime
        helper.time = end_time - start_time
        return
      }

      while (!helper.isConsistent && !I.empty()) {
        back_start_time = System.nanoTime
        literal = I.pop()
        backLevel()
        literal.v.remove(literal.a)
        remove(literal)

        //        println("pop:" + literal.toString())
        //        infoShow()

        if (literal.v.isEmpty()) {

          prop_start_time = System.nanoTime
          helper.ACFinished = false
          val m = newTmpLevel()
          M += (m -> new LCSync(literal.v, m))
          val lc = M(m)
          lc.start()
          AC(literal.v)
          lc.join()
          M -= m
          deleteTmpLevel(m)

          end_time = System.nanoTime
          helper.propTime += (end_time - prop_start_time)
          //          println("--------")
          //          infoShow()
        }
      }

      if (!helper.isConsistent) {
        finished = true
      }
    }
    end_time = System.nanoTime
    helper.time = end_time - start_time
    return
  }

  def async(timeLimit: Long): Unit = {
    //    initial propagate
    //    println("initial propagate")
    //    infoShow()
    var finished = false
    start_time = System.nanoTime
    var fail = false
    var literal: Literal[Var] = null
    AC(null)

    end_time = System.nanoTime
    helper.propTime += (end_time - prop_start_time)

    if (!helper.isConsistent) {
      finished = false
      end_time = System.nanoTime
      helper.time = end_time - start_time
      return
    }
    //
    //    var literal: Literal[Var] = null/null
    //    literal = selectLiteral()

    var v = selectVar()
    var BTLevel = -1

    var ii = 1
    while (ii <= numVars && ii >= 1) {
      breakable {
        while (v.size() >= 1) {
          fail = false
          BTLevel = forceBT()

          if (BTLevel < ii) {
            fail = true
            break()
          }

          literal = new Literal(v, v.minValue())
          newLevel()
          helper.nodes += 1
          I.push(literal)
          bind(literal)

          //          // 线程未满,
          //          if (M.size < parallelism) {
          //            val m = newTmpLevel()
          //            M += (m -> new LCSync(literal.v, m))
          //            val lc = M(m)
          //            lc.start()
          //          }
          AC(v)
          // 失败了回溯一层
          if (!helper.isConsistent) {
            fail = true
            literal = I.pop()
            backLevel()
            literal.v.remove(literal.a)
            remove(literal)
            helper.level -= 1
          }
          else {
            break()
          }


        }
      }

      if (fail) {
        backLevel(BTLevel)
        // 其它线程回溯
        for ((k, v) <- LCState) {
          // 若线程仍在运行且该相容性的搜索层数大于当前层，则中止worker，设置其为idle为负的
          if (v == 2 && k.searchLevel >= helper.level) {
            // 设置其为需要停止
            LCState(k) = 1
            // 等待其停止
            M(k).join()
          }
        }
      }

      ii += 1
    }


  }

  def forceBT(): Int = {
    0
  }

  def initialPropagate(): Boolean = {
    //    return propagate(null)
    return false
  }

  def checkConsistencyAfterAssignment(x: Var): Boolean = {
    //    return propagate(x)
    return false
  }

  def checkConsistencyAfterRefutation(x: Var): Boolean = {
    //    return propagate(x)
    return false
  }

  def LMX(x: Var, m: MultiLevel): Boolean = {
    val LMXQ = new CoarseQueue[Var](numVars)
    var LMXY: ArrayBuffer[BitSetVar_LMX] = new ArrayBuffer[BitSetVar_LMX](xm.max_arity)
    LMXQ.clear()

    // 初始化传播队列
    if (x == null) {
      for (z <- vars) {
        LMXQ.push(z)
        //        println(s"Q << ${z.id}")
      }
    } else {
      LMXQ.push(x)
      //      println(s"Q << ${x.id}")
    }

    while (!LMXQ.empty()) {
      if (helper.ACFinished) {
        return true
      }
      val j = LMXQ.pop().asInstanceOf[BitSetVar_LMX]
      //      println(s"Q >> ${j.id}")
      for (i <- helper.neiVar(j.id)) {
        //        println(s"nei: ${i.id}")
        if (i.unBind(m.searchLevel)) {
          val c = helper.commonCon(i.id)(j.id)(0)
          LMXY.clear()
          LMXY += i
          LMXY += j

          val (res, changed) = c.LMX(LMXY, m)

          if (!res) {
            helper.isConsistent = false
            return false
          }

          if (changed) {
            //            println(s"Q << ${i.id}")
            LMXQ.push(i)
          }
        }
      }
    }

    return true
  }

  def AC(x: Var): Boolean = {
    Q.clear()
    if (x == null) {
      //初始化
      for (z <- vars) {
        insert(z)
      }
    } else {
      insert(x)
    }
    while (!Q.empty()) {
      if (!helper.isConsistent) {
        helper.isConsistent = false
        helper.ACFinished = true
        return false
      }
      val v = Q.pop()
      for (c <- subscription(v.id)) {
        if (helper.varStamp(v.id) > helper.tabStamp(c.id)) {
          //          println("str(" + c.name + ")")
          Y_evt.clear()
          val consistent = c.AC(Y_evt)
          helper.c_sum += 1
          //          print(s"cid: ${c.id} yevt: ")
          //          Y_evt.foreach(p => print(p.id, " "))
          //          println()
          if (!consistent) {
            helper.isConsistent = false
            helper.ACFinished = true
            return false
          } else {
            for (y <- Y_evt) {
              insert(y)
            }
          }
          helper.globalStamp += 1
          helper.tabStamp(c.id) = helper.globalStamp
        }
      }
      helper.p_sum += 1
    }
    helper.ACFinished = true
    return true
  }

  def LMXAsync(x: Var, m: MultiLevel): Boolean = {
    val LMXQ = new CoarseQueue[Var](numVars)
    var LMXY: ArrayBuffer[BitSetVar_LMX] = new ArrayBuffer[BitSetVar_LMX](xm.max_arity)
    LMXQ.clear()

    // 初始化传播队列
    if (x == null) {
      for (z <- vars) {
        LMXQ.push(z)
        //        println(s"Q << ${z.id}")
      }
    } else {
      LMXQ.push(x)
      //      println(s"Q << ${x.id}")
    }

    while (!LMXQ.empty()) {
      if (helper.ACFinished) {
        return true
      }
      val j = LMXQ.pop().asInstanceOf[BitSetVar_LMX]
      //      println(s"Q >> ${j.id}")
      for (i <- helper.neiVar(j.id)) {
        //        println(s"nei: ${i.id}")
        if (i.unBind(m.searchLevel)) {
          val c = helper.commonCon(i.id)(j.id)(0)
          LMXY.clear()
          LMXY += i
          LMXY += j

          val (res, changed) = c.LMX(LMXY, m)

          if (!res) {
            helper.isConsistent = false
            return false
          }

          if (changed) {
            //            println(s"Q << ${i.id}")
            LMXQ.push(i)
          }
        }
      }
    }

    return true
  }

  def ACAsync(x: Var): Boolean = {
    Q.clear()
    if (x == null) {
      //初始化
      for (z <- vars) {
        insert(z)
      }
    } else {
      insert(x)
    }
    while (!Q.empty()) {
      if (!helper.isConsistent) {
        helper.isConsistent = false
        helper.ACFinished = true
        return false
      }
      val v = Q.pop()
      for (c <- subscription(v.id)) {
        if (helper.varStamp(v.id) > helper.tabStamp(c.id)) {
          //          println("str(" + c.name + ")")
          Y_evt.clear()
          val consistent = c.AC(Y_evt)
          helper.c_sum += 1
          //          print(s"cid: ${c.id} yevt: ")
          //          Y_evt.foreach(p => print(p.id, " "))
          //          println()
          if (!consistent) {
            helper.isConsistent = false
            helper.ACFinished = true
            return false
          } else {
            for (y <- Y_evt) {
              insert(y)
            }
          }
          helper.globalStamp += 1
          helper.tabStamp(c.id) = helper.globalStamp
        }
      }
      helper.p_sum += 1
    }
    helper.ACFinished = true
    return true
  }

  def insert(x: Var): Unit = {
    Q.push(x)
    helper.globalStamp += 1
    helper.varStamp(x.id) = helper.globalStamp
  }

  //修改levelvdense
  def selectLiteral(): Literal[Var] = {
    var v = selectVar()
    return new Literal[Var](v, v.minValue())
  }

  def selectVar(): Var = {
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
        return v
      }

      val sizeD: Double = v.size.toDouble
      val dmdd = sizeD / ddeg

      if (dmdd < mindmdd) {
        minvid = vid
        mindmdd = dmdd
      }
      i += 1
    }

    return vars(minvid)
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

  def backLevel(i: Int): Unit = {
    helper.isConsistent = true
    for (v <- vars) {
      v.backLevel(i)
    }
    for (c <- tabs) {
      c.backLevel()
    }
  }

  def newTmpLevel(): MultiLevel = {
    val m = L.add(helper.level)
    for (v <- vars) {
      v.newTmpLevel(m)
    }
    return m
  }

  def deleteTmpLevel(m: MultiLevel): Unit = {
    L.remove(m)
  }

  def backLevel(): Unit = {
    helper.isConsistent = true
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
      //      println(s"     var:${x.id} size:${x.size()}")
      x.show()
    }
  }
}
