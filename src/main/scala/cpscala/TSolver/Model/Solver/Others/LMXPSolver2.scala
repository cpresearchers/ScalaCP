//package cpscala.TSolver.Model.Solver.Others
//
//import cpscala.TSolver.CpUtil.{AssignedStack, CoarseQueue, Literal}
//import cpscala.TSolver.Model.Variable.Var
//import cpscala.XModel.{XModel, XTab, XVar}
//
//import scala.collection.mutable
//import scala.collection.mutable._
//import scala.util.control.Breaks.{break, breakable}
//
//
//class LMXPSolver2(xm: XModel, parallelism: Int) {
//
//  class LCSync(val x: Var, val m: MultiLevel) extends Thread {
//    override def run(): Unit = {
//      LMX(x, m)
//    }
//  }
//
//  class LCAsync(val x: Var, val m: MultiLevel) extends Thread {
//    override def run(): Unit = {
//      helper.States(m) = LCState.Running
//      try {
//        LMXAsync(x, m)
//      } finally {
//        return
//      }
//
//      //      println(Thread.currentThread().getId() + " ending ")
//    }
//  }
//
//  val numVars: Int = xm.num_vars
//  val numTabs: Int = xm.num_tabs
//  val vars = new Array[BitSetVar_LMX](numVars)
//  val tabs = new Array[LMX_Bit2](numTabs)
//  val helper = new LMXSearchHelper(numVars, numTabs, xm, parallelism)
//
//  // 记录已赋值的变量
//  val levelvsparse = Array.range(0, numVars)
//  val levelvdense = Array.range(0, numVars)
//  val subscription = new Array[ArrayBuffer[LMX_Bit2]](numVars)
//
//  for (i <- 0 until numVars) {
//    subscription(i) = new ArrayBuffer[LMX_Bit2]()
//  }
//
//  // 初始化变量
//  for (i <- 0 until numVars) {
//    val xv: XVar = xm.vars.get(i)
//    vars(i) = new BitSetVar_LMX(xv.name, xv.id, numVars, xv.values, helper, parallelism)
//  }
//
//  // 初始化约束
//  for (i <- 0 until numTabs) {
//    val xc: XTab = xm.tabs.get(i)
//    val ts: Array[Array[Int]] = xc.tuples
//    val scope: Array[BitSetVar_LMX] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
//    tabs(i) = new LMX_Bit2(xc.id, xc.arity, numVars, scope, ts, helper, parallelism)
//
//    for (v <- scope) {
//      subscription(v.id) += tabs(i)
//    }
//  }
//
//  // 初始化搜索引擎中用到的数据结构
//  val Q = new CoarseQueue[Var](numVars)
//  var Y_evt: ArrayBuffer[BitSetVar_LMX] = new ArrayBuffer[BitSetVar_LMX](xm.max_arity)
//  val I = new AssignedStack[Var](xm.num_vars)
//  val baseLevel = numVars + 3
//  val L = new LMXSparseSet(parallelism, baseLevel)
//  val singleTmpLevel = parallelism + baseLevel
//
//  val M = new mutable.HashMap[MultiLevel, LCSync]()
//  //  val M = new Array[LCState](parallelism)
//  val LCAThreads = new Array[LCAsync](parallelism)
//
//  // =0 是idle 就是空闲 为空
//  // =1 是needstop
//  // =2 是running
//  // =3 是完成
//  //  val LCState = new mutable.HashMap[MultiLevel, Int]()
//  val LCAThreadss = new mutable.HashMap[MultiLevel, LCAsync]()
//  var numActive = 0
//
//  // 初始化helper中的部分数据结构
//  for (c <- tabs) {
//    helper.commonCon(c.scope(0).id)(c.scope(1).id) += c
//    helper.commonCon(c.scope(1).id)(c.scope(0).id) += c
//  }
//
//  for (x <- vars) {
//    for (y <- vars) {
//      if ((x != y) && helper.commonCon(x.id)(y.id).nonEmpty) {
//        helper.neiVar(x.id) += y
//      }
//    }
//  }
//
//  for (x <- vars) {
//    for (y <- vars) {
//      if ((x != y)) {
//        for (z <- vars) {
//          if (x != z && y != z && helper.commonCon(x.id)(z.id).nonEmpty && helper.commonCon(y.id)(z.id).nonEmpty) {
//            helper.commonVar(x.id)(y.id) += z
//          }
//        }
//      }
//    }
//  }
//
//  var start_time = 0L
//  var branch_start_time = 0L
//  var prop_start_time = 0L
//  var back_start_time = 0L
//  var end_time = 0L
//
////  def sync(timeLimit: Long): Unit = {
////    //    initial propagate
////    //    println("initial propagate")
////    //    infoShow()
////    var finished = false
////    start_time = System.nanoTime
////    val m = newTmpLevel()
////    M += (m -> new LCSync(null, m))
////    val lc = M(m)
////    //    lc.run()
////    lc.start()
////    AC(null)
////    lc.join()
////    M -= m
////    deleteTmpLevel(m)
////
////    end_time = System.nanoTime
////    helper.propTime += (end_time - prop_start_time)
////
////    if (!helper.isConsistent) {
////      finished = false
////      end_time = System.nanoTime
////      helper.time = end_time - start_time
////      return
////    }
////    //
////    var literal: Literal[Var] = null
////    //
////    while (!finished) {
////      end_time = System.nanoTime
////      helper.time = end_time - start_time
////      if (helper.time > timeLimit) {
////        return
////      }
////
////      branch_start_time = System.nanoTime
////      literal = selectLiteral()
////      newLevel()
////      helper.nodes += 1
////      I.push(literal)
////      bind(literal)
////      end_time = System.nanoTime
////      helper.branchTime += (end_time - branch_start_time)
////
////      //      println("push:" + literal.toString())
////      //      infoShow()
////
////      prop_start_time = System.nanoTime
////      helper.ACFinished = false
////      val m = newTmpLevel()
////      M += (m -> new LCSync(literal.v, m))
////      val lc = M(m)
////      lc.start()
////      AC(literal.v)
////      lc.join()
////      M -= m
////      deleteTmpLevel(m)
////
////      end_time = System.nanoTime
////      helper.propTime += (end_time - prop_start_time)
////
////      //      println("--------")
////      //      infoShow()
////
////      if (helper.isConsistent && I.full()) {
////        //        I.show()
////        helper.hasSolution = true
////        end_time = System.nanoTime
////        helper.time = end_time - start_time
////        return
////      }
////
////      while (!helper.isConsistent && !I.empty()) {
////        back_start_time = System.nanoTime
////        literal = I.pop()
////        backLevel()
////        //        literal.v.remove(literal.a)
////        remove(literal)
////
////        //        println("pop:" + literal.toString())
////        //        infoShow()
////
////        if (literal.v.isEmpty()) {
////
////          prop_start_time = System.nanoTime
////          helper.ACFinished = false
////          val m = newTmpLevel()
////          M += (m -> new LCSync(literal.v, m))
////          val lc = M(m)
////          lc.start()
////          AC(literal.v)
////          lc.join()
////          M -= m
////          deleteTmpLevel(m)
////
////          end_time = System.nanoTime
////          helper.propTime += (end_time - prop_start_time)
////          //          println("--------")
////          //          infoShow()
////        }
////      }
////
////      if (!helper.isConsistent) {
////        finished = true
////      }
////    }
////    end_time = System.nanoTime
////    helper.time = end_time - start_time
////    return
////  }
//
//  def async(timeLimit: Long): Unit = {
//    helper.searchFinished = false
//    var literal: Literal[Var] = null
//
//    //initial propagate
//    //    println("initial propagate")
//    //    infoShow()
//    start_time = System.nanoTime
//    //    var fail = false
//    var BTLevel = 0
//
//    val m = newTmpLevel()
//    LCAThreads(m.tIdx) = new LCAsync(null, m)
//    helper.State(m.tIdx) = LCState.Running
//    numActive += 1
//    LCAThreads(m.tIdx).start()
//    AC(null)
//
//    end_time = System.nanoTime
//    helper.propTime += (end_time - prop_start_time)
//
//    // AC失败了,直接退出
//    if (!helper.isConsistent) {
//      helper.State(m.tIdx) = LCState.NeedStop
//      LCAThreads(m.tIdx).join()
//      LCAThreads(m.tIdx) = null
//      helper.State(m.tIdx) = LCState.Idle
//      numActive -= 1
//      deleteTmpLevel(m)
//
//      helper.searchFinished = true
//      end_time = System.nanoTime
//      helper.time = end_time - start_time
//      return
//    }
//
//    //搜索阶段
//    while (!helper.searchFinished) {
//      // 超时检测
//      end_time = System.nanoTime
//      helper.time = end_time - start_time
//      if (helper.time > timeLimit) {
//        helper.timeout = true
//        //        for ((m, s) <- helper.States) {
//        //          helper.States(m) = LCState.NeedStop
//        //          LCAThreads(m).join()
//        //          LCAThreads -= m
//        //          helper.States -= m
//        //          deleteTmpLevel(m)
//        //        }
//
//        var ii = 0
//        while (ii < parallelism) {
//          val mm = L.getMultiLevel(ii)
//          if (helper.State(ii) != LCState.Idle) {
//            helper.State(mm.tIdx) = LCState.NeedStop
//            LCAThreads(mm.tIdx).join()
//            LCAThreads(mm.tIdx) = null
//            helper.State(mm.tIdx) = LCState.Idle
//            numActive -= 1
//            deleteTmpLevel(mm)
//          }
//          ii += 1
//        }
//        return
//      }
//
//
//      //扫描所有的 删除失败的线程确定回溯行数，其中BTLevel也是错的
//      BTLevel = forceBT()
//      //      println("forceBT", BTLevel)
//      //      for ((m, s) <- helper.States) {
//      //        println("state:", m.toString(), s)
//      //      }
//      // 扫描所有的线程，删除未运行的线程
//      //      for ((m, s) <- helper.States) {
//      //        if (s != LCState.Running) {
//      //          //          println("delete:", m.toString())
//      //          //          helper.States(m) = LCState.NeedStop
//      //          LCAThreads(m).join()
//      //
//      //          LCAThreads -= m
//      //          //          println("removing", m.toString())
//      //          helper.States -= m
//      //          deleteTmpLevel(m)
//      //        }
//      //      }
//
//      var ii = 0
//      while (ii < parallelism) {
//        // 线程没运行
//        if (helper.State(ii) != LCState.Idle && helper.State(ii) != LCState.Running) {
//          val mm = L.getMultiLevel(ii)
//          helper.State(mm.tIdx) = LCState.NeedStop
//          LCAThreads(mm.tIdx).join()
//          LCAThreads(mm.tIdx) = null
//          helper.State(mm.tIdx) = LCState.Idle
//          numActive -= 1
//          deleteTmpLevel(mm)
//        }
//        ii += 1
//      }
//
//      // 网络回溯到BTLevel层，BTLevel也不要了
//      //      println(BTLevel, helper.level)
//      if (BTLevel < helper.level) {
//        //        // 子线程先回溯
//        //        for ((m, s) <- helper.States) {
//        //          if (m.searchLevel >= BTLevel) {
//        //            //            println("backtrack threads: ", m, s, helper.States.size)
//        //            helper.States(m) = LCState.NeedStop
//        //            LCAThreads(m).join()
//        //            LCAThreads -= m
//        //            //            println("removing", m.toString())
//        //            helper.States -= m
//        //            deleteTmpLevel(m)
//        //          }
//        //        }
//
//        var ii = 0
//        while (ii < parallelism) {
//          val mm = L.getMultiLevel(ii)
//          if (mm.searchLevel >= BTLevel) {
//            //            println("backtrack threads: ", m, s, helper.States.size)
//            helper.State(mm.tIdx) = LCState.NeedStop
//            LCAThreads(mm.tIdx).join()
//            LCAThreads(mm.tIdx) = null
//            helper.State(mm.tIdx) = LCState.Idle
//            numActive -= 1
//            deleteTmpLevel(mm)
//          }
//          ii += 1
//        }
//
//        do {
//          literal = I.pop()
//          //          println(s"level: ${helper.level}, backtrack main, pop: ", literal.toString())
//          backLevel()
//          remove(literal)
//        } while (helper.level > BTLevel || (helper.level > 0 && literal.v.isEmpty()))
//
//        if (helper.level == 0 && literal.v.isEmpty()) {
//          end_time = System.nanoTime
//          helper.time = end_time - start_time
//          return
//        } else {
//          helper.isConsistent = true
//        }
//      }
//
//      // 选新值赋值
//      branch_start_time = System.nanoTime
//      literal = selectLiteral()
//
//      //      infoShow()
//      prop_start_time = System.nanoTime
//      helper.ACFinished = false
//
//      if (!(literal.v.isEmpty() || literal.invalid())) {
//        newLevel()
//        //        println(s"level: ${helper.level}, push:" + literal.toString())
//        I.push(literal)
//        bind(literal)
//        helper.nodes += 1
//        //        println("nodes:", helper.nodes)
//        end_time = System.nanoTime
//        helper.branchTime += (end_time - branch_start_time)
//
//        // 线程未满，新建线程
//        if (numActive < parallelism) {
//          //          println(s"new tmp: ${LCAThreads.size}, ${parallelism}")
//          val mm = newTmpLevel()
//          LCAThreads(mm.tIdx) = new LCAsync(literal.v, mm)
//          helper.State(mm.tIdx) = LCState.Running
//          numActive += 1
//          LCAThreads(mm.tIdx).start()
//        }
//
//        AC(literal.v)
//      } else {
//        helper.isConsistent = false
//      }
//
//      end_time = System.nanoTime
//      helper.propTime += (end_time - prop_start_time)
//
//      if (helper.isConsistent && I.full()) {
//
//        end_time = System.nanoTime
//        helper.time = end_time - start_time
//        helper.hasSolution = true
//        // 完成了 所有有LC都退出
//        //        for ((m, v) <- helper.States) {
//        //          helper.States(m) = LCState.NeedStop
//        //          LCAThreads(m).join()
//        //          LCAThreads -= m
//        //          helper.States -= m
//        //          deleteTmpLevel(m)
//        //        }
//
//        var ii = 0
//        while (ii < parallelism) {
//          val mm = L.getMultiLevel(ii)
//          if (helper.State(ii) != LCState.Idle) {
//            helper.State(mm.tIdx) = LCState.NeedStop
//            LCAThreads(mm.tIdx).join()
//            LCAThreads(mm.tIdx) = null
//            helper.State(mm.tIdx) = LCState.Idle
//            numActive -= 1
//            deleteTmpLevel(mm)
//          }
//          ii += 1
//        }
//        //        I.show()
//        return
//      }
//
//      // AC失败了
//      while (!helper.isConsistent && !I.empty()) {
//        //        back_start_time = System.nanoTime
//        do {
//
//          var ii = 0
//          while (ii < parallelism) {
//            val mm = L.getMultiLevel(ii)
//            if (helper.State(ii) != LCState.Idle) {
//              if (m.searchLevel >= helper.level) {
//                helper.State(mm.tIdx) = LCState.NeedStop
//                LCAThreads(mm.tIdx).join()
//                LCAThreads(mm.tIdx) = null
//                helper.State(mm.tIdx) = LCState.Idle
//                numActive -= 1
//                deleteTmpLevel(mm)
//              }
//            }
//            ii += 1
//          }
//
//          //          for ((m, v) <- helper.States) {
//          //            if (m.searchLevel >= helper.level) {
//          //              helper.States(m) = LCState.NeedStop
//          //              LCAThreads(m).join()
//          //              LCAThreads -= m
//          //              helper.States -= m
//          //              deleteTmpLevel(m)
//          //            }
//          //          }
//
//          literal = I.pop()
//          //          println(s"level: ${helper.level}, pop:" + literal.toString(), literal.v.size())
//          backLevel()
//          remove(literal)
//
//          //          infoShow()
//        } while (literal.v.isEmpty() && helper.level > 0)
//
//        if (literal.v.isEmpty() && helper.level == 0) {
//          end_time = System.nanoTime
//          helper.time = end_time - start_time
//          return
//        } else {
//          helper.isConsistent = true
//        }
//
//        //        infoShow()
//
//        //        if (!literal.v.isEmpty()) {
//        //          prop_start_time = System.nanoTime
//        //          //          helper.ACFinished = false
//        //          //          val m = newTmpLevel()
//        //          //          M += (m -> new LCSync(literal.v, m))
//        //          //          val lc = M(m)
//        //          //          lc.start()
//        //          //          AC(literal.v)
//        //          //          lc.join()
//        //          //          M -= m
//        //          //          deleteTmpLevel(m)
//        //          helper.isConsistent = true
//        //          end_time = System.nanoTime
//        //          helper.propTime += (end_time - prop_start_time)
//        //          //          println("--------")
//        //          //          infoShow()
//        //        }
//      }
//
//      if (!helper.isConsistent) {
//        helper.searchFinished = true
//      }
//    }
//
//    // 扫描所有的线程，删除所有运行层数大于当前层的线程
//    //    for ((m, s) <- helper.States) {
//    //      helper.States(m) = LCState.NeedStop
//    //      LCAThreads(m).join()
//    //      LCAThreads -= m
//    //      helper.States -= m
//    //      deleteTmpLevel(m)
//    //    }
//
//    var ii = 0
//    while (ii < parallelism) {
//      val mm = L.getMultiLevel(ii)
//      if (helper.State(ii) != LCState.Idle) {
//
//        helper.State(mm.tIdx) = LCState.NeedStop
//        LCAThreads(mm.tIdx).join()
//        LCAThreads(mm.tIdx) = null
//        helper.State(mm.tIdx) = LCState.Idle
//        numActive -= 1
//        deleteTmpLevel(mm)
//
//      }
//      ii += 1
//    }
//
//    end_time = System.nanoTime
//    helper.time = end_time - start_time
//    return
//
//  }
//
////  def hyper(timeLimit: Long): Unit = {
////    helper.searchFinished = false
////    var literal: Literal[Var] = null
////
////    //initial propagate
////    //    println("initial propagate")
////    //    infoShow()
////    start_time = System.nanoTime
////    //    var fail = false
////    var BTLevel = 0
////    val sm = new MultiLevel(helper.level, singleTmpLevel, parallelism)
////    //    println(sm.toString())
////
////    val m = newTmpLevel()
////
////    //    LCAThreads += (m -> new LCAsync(null, m))
////    //    helper.States += (m -> LCState.Running)
////    //    val cc = LCAThreads(m)
////    //    cc.start()
////
////    LCAThreads(m.tIdx) = new LCAsync(null, m)
////    helper.State(m.tIdx) = LCState.Running
////    numActive += 1
////    LCAThreads(m.tIdx).start()
////
////    newSyncTmpLevel(sm)
////    val tm = new LCSync(null, sm)
////    tm.start()
////    AC(null)
////    tm.join()
////
////
////    end_time = System.nanoTime
////    helper.propTime += (end_time - prop_start_time)
////
////    // AC失败了,直接退出
////    if (!helper.isConsistent) {
////      helper.State(m.tIdx) = LCState.NeedStop
////      LCAThreads(m.tIdx).join()
////      LCAThreads(m.tIdx) = null
////      helper.State(m.tIdx) = LCState.Idle
////      numActive -= 1
////      deleteTmpLevel(m)
////
////      helper.searchFinished = true
////      end_time = System.nanoTime
////      helper.time = end_time - start_time
////      return
////    }
////
////    //搜索阶段
////    while (!helper.searchFinished) {
////      // 超时检测
////      end_time = System.nanoTime
////      helper.time = end_time - start_time
////      if (helper.time > timeLimit) {
////        helper.timeout = true
//////        for ((m, s) <- helper.States) {
//////          helper.States(m) = LCState.NeedStop
//////          LCAThreads(m).join()
//////          LCAThreads -= m
//////          helper.States -= m
//////          deleteTmpLevel(m)
//////        }
////
////        var ii=0
////        while (ii<parallelism){
////          val mm = L.getMultiLevel(ii)
////          if (helper.State(ii) != LCState.Idle) {
////            helper.State(mm.tIdx) = LCState.NeedStop
////            LCAThreads(mm.tIdx).join()
////            LCAThreads(mm.tIdx) = null
////            helper.State(mm.tIdx) = LCState.Idle
////            numActive -= 1
////            deleteTmpLevel(mm)
////          }
////          ii+=1
////        }
////        return
////      }
////
////
////      //扫描所有的 删除失败的线程确定回溯行数，其中BTLevel也是错的
////      BTLevel = forceBT()
////      //      println("forceBT", BTLevel)
////      for ((m, s) <- helper.States) {
////        //        println("state:", m.toString(), s)
////      }
////      // 扫描所有的线程，删除未运行的线程
////      for ((m, s) <- helper.States) {
////        if (s != LCState.Running) {
////          //          println("delete:", m.toString())
////          //          helper.States(m) = LCState.NeedStop
////          LCAThreads(m).join()
////          LCAThreads -= m
////          helper.States -= m
////          deleteTmpLevel(m)
////        }
////      }
////
////      // 网络回溯到BTLevel层，BTLevel也不要了
////      if (BTLevel < helper.level) {
////        // 子线程先回溯
////        for ((m, s) <- helper.States) {
////          if (m.searchLevel >= BTLevel) {
////            //            println("backtrack threads: ", m, s, helper.States.size)
////            helper.States(m) = LCState.NeedStop
////            LCAThreads(m).join()
////            LCAThreads -= m
////            helper.States -= m
////            deleteTmpLevel(m)
////          }
////        }
////
////        do {
////          literal = I.pop()
////          //print("pop", BTLevel, I.size(), literal.toString())
////          //          println(s"level: ${helper.level}, backtrack main, pop: ", literal.toString())
////          backLevel()
////          remove(literal)
////        } while (helper.level > BTLevel || (helper.level > 0 && literal.v.isEmpty()))
////
////        if (helper.level == 0 && literal.v.isEmpty()) {
////          end_time = System.nanoTime
////          helper.time = end_time - start_time
////          return
////        } else {
////          helper.isConsistent = true
////        }
////      }
////
////      // 选新值赋值
////      branch_start_time = System.nanoTime
////      literal = selectLiteral()
////
////      //      infoShow()
////      prop_start_time = System.nanoTime
////      helper.ACFinished = false
////
////      if (!(literal.v.isEmpty() || literal.invalid())) {
////        newLevel()
////        //        println(s"level: ${helper.level}, push:" + literal.toString())
////        I.push(literal)
////        bind(literal)
////        helper.nodes += 1
////        //        println("nodes:", helper.nodes)
////        end_time = System.nanoTime
////        helper.branchTime += (end_time - branch_start_time)
////
////        // 线程未满，新建线程
////        if (LCAThreads.size < parallelism) {
////          //          println(s"new tmp: ${LCAThreads.size}, ${parallelism}")
////          val m = newTmpLevel()
////          LCAThreads += (m -> new LCAsync(literal.v, m))
////          helper.States += (m -> LCState.Running)
////          LCAThreads(m).start()
////        }
////
////        newSyncTmpLevel(sm)
////        val tm = new LCSync(literal.v, sm)
////        tm.start()
////        AC(literal.v)
////        tm.join()
////
////      } else {
////        helper.isConsistent = false
////      }
////
////      end_time = System.nanoTime
////      helper.propTime += (end_time - prop_start_time)
////
////      if (helper.isConsistent && I.full()) {
////
////        end_time = System.nanoTime
////        helper.time = end_time - start_time
////        helper.hasSolution = true
////        // 完成了 所有有LC都退出
////        for ((m, v) <- helper.States) {
////          helper.States(m) = LCState.NeedStop
////          LCAThreads(m).join()
////          LCAThreads -= m
////          helper.States -= m
////          deleteTmpLevel(m)
////        }
////        //        I.show()
////        return
////      }
////
////      // AC失败了
////      while (!helper.isConsistent && !I.empty()) {
////        //        back_start_time = System.nanoTime
////        do {
////          for ((m, v) <- helper.States) {
////            if (m.searchLevel >= helper.level) {
////              helper.States(m) = LCState.NeedStop
////              LCAThreads(m).join()
////              LCAThreads -= m
////              helper.States -= m
////              deleteTmpLevel(m)
////            }
////          }
////
////          //println("pop", I.size())
////          literal = I.pop()
////          //          println(s"level: ${helper.level}, pop:" + literal.toString(), literal.v.size())
////          backLevel()
////          remove(literal)
////        } while (literal.v.isEmpty() && helper.level > 0)
////
////        if (literal.v.isEmpty() && helper.level == 0) {
////          end_time = System.nanoTime
////          helper.time = end_time - start_time
////          return
////        } else {
////          helper.isConsistent = true
////        }
////      }
////
////      if (!helper.isConsistent) {
////        helper.searchFinished = true
////      }
////    }
////
////    // 扫描所有的线程，删除所有运行层数大于当前层的线程
////    for ((m, s) <- helper.States) {
////      helper.States(m) = LCState.NeedStop
////      LCAThreads(m).join()
////      LCAThreads -= m
////      helper.States -= m
////      deleteTmpLevel(m)
////    }
////
////    end_time = System.nanoTime
////    helper.time = end_time - start_time
////    return
////
////  }
//
//  def forceBT(): Int = {
//    //    println("forceBT")
//    var minLevel = helper.level + 1
//
//    var ii = 0
//    while (ii < parallelism) {
//      if (helper.State(ii) != LCState.Idle) {
//        // 所有检测到失败的线程
//        val searchLevel = L.getSearchLevel(ii)
//        val k = L.getMultiLevel(ii)
//        if (LCState(ii) == LCState.Fail && searchLevel < minLevel) {
//          minLevel = L.getSearchLevel(ii)
//        } else {
//          var jj = searchLevel
//          while (jj < minLevel && jj < I.size()) {
//            // 从I中拿到值 从search level 到 当前层，若I存储的赋值已被线程删去，则需要回溯，更新minLevel
//            val va = I.table(jj)
//            if (!va.v.asInstanceOf[BitSetVar_LMX].contains(va.a, k)) {
//              minLevel = jj
//            }
//            jj += 1
//          }
//        }
//      }
//      ii += 1
//    }
//
//    return minLevel
//  }
//
//  //  def initialPropagate(): Boolean = {
//  //    //    return propagate(null)
//  //    return false
//  //  }
//  //
//  //  def checkConsistencyAfterAssignment(x: Var): Boolean = {
//  //    //    return propagate(x)
//  //    return false
//  //  }
//  //
//  //  def checkConsistencyAfterRefutation(x: Var): Boolean = {
//  //    //    return propagate(x)
//  //    return false
//  //  }
//
//  def LMX(x: Var, m: MultiLevel): Boolean = {
//    val LMXQ = new CoarseQueue[Var](numVars)
//    var LMXY: ArrayBuffer[BitSetVar_LMX] = new ArrayBuffer[BitSetVar_LMX](xm.max_arity)
//    LMXQ.clear()
//
//    // 初始化传播队列
//    if (x == null) {
//      for (z <- vars) {
//        LMXQ.push(z)
//        //        println(s"Q << ${z.id}")
//      }
//    } else {
//      LMXQ.push(x)
//      //      println(s"Q << ${x.id}")
//    }
//
//    while (!LMXQ.empty()) {
//      if (helper.ACFinished) {
//        return true
//      }
//      if (!helper.isConsistent) {
//        return false
//      }
//      val j = LMXQ.pop().asInstanceOf[BitSetVar_LMX]
//      //      println(s"Q >> ${j.id}")
//      for (i <- helper.neiVar(j.id)) {
//        //        println(s"nei: ${i.id}")
//        if (i.unBind(m.searchLevel)) {
//          val c = helper.commonCon(i.id)(j.id)(0)
//          LMXY.clear()
//          LMXY += i
//          LMXY += j
//
//          val (res, changed) = c.LMX(LMXY, m)
//
//          if (!res) {
//            helper.isConsistent = false
//            return false
//          }
//
//          if (changed) {
//            //            println(s"Q << ${i.id}")
//            LMXQ.push(i)
//          }
//        }
//      }
//    }
//
//    return true
//  }
//
//  def AC(x: Var): Boolean = {
//    Q.clear()
//    if (x == null) {
//      //初始化
//      for (z <- vars) {
//        insert(z)
//      }
//    } else {
//      insert(x)
//    }
//    while (!Q.empty()) {
//      if (!helper.isConsistent) {
//        helper.isConsistent = false
//        helper.ACFinished = true
//        return false
//      }
//      val v = Q.pop()
//      for (c <- subscription(v.id)) {
//        if (helper.varStamp(v.id) > helper.tabStamp(c.id)) {
//          //          println("str(" + c.name + ")")
//          Y_evt.clear()
//          val consistent = c.AC(Y_evt)
//          helper.c_sum += 1
//          //          print(s"cid: ${c.id} yevt: ")
//          //          Y_evt.foreach(p => print(p.id, " "))
//          //          println()
//          if (!consistent) {
//            helper.isConsistent = false
//            helper.ACFinished = true
//            return false
//          } else {
//            for (y <- Y_evt) {
//              insert(y)
//            }
//          }
//          helper.globalStamp += 1
//          helper.tabStamp(c.id) = helper.globalStamp
//        }
//      }
//      helper.p_sum += 1
//    }
//    helper.ACFinished = true
//    return true
//  }
//
//  def LMXAsync(x: Var, m: MultiLevel): Unit = {
//    val LMXQ = new CoarseQueue[Var](numVars)
//    var LMXY: ArrayBuffer[BitSetVar_LMX] = new ArrayBuffer[BitSetVar_LMX](xm.max_arity)
//    LMXQ.clear()
//
//    // 初始化传播队列
//    if (x == null) {
//      for (z <- vars) {
//        LMXQ.push(z)
//        //        println(s"Q << ${z.id}")
//      }
//    } else {
//      LMXQ.push(x)
//      //      println(s"Q << ${x.id}")
//    }
//
//    while (!LMXQ.empty()) {
//      val j = LMXQ.pop().asInstanceOf[BitSetVar_LMX]
//      //      println(s"Q >> ${j.id}")
//      for (i <- helper.neiVar(j.id)) {
//        // 需要停下来了，一般是由外部通知
//        if (helper.States(m) == LCState.NeedStop) {
//          helper.States(m) = LCState.Stopped
//          return
//        }
//        //        println(s"nei: ${i.id}")
//        if (i.unBind(m.searchLevel)) {
//          val c = helper.commonCon(i.id)(j.id)(0)
//          LMXY.clear()
//          LMXY += i
//          LMXY += j
//
//          val (state, changed) = c.LMXAsync(LMXY, m)
//
//          if (state == LCState.Fail) {
//            helper.States(m) = LCState.Fail
//            helper.isConsistent = false
//            return
//          }
//          else if (state == LCState.Stopped) {
//            helper.States(m) = LCState.Stopped
//            return
//          }
//
//          if (changed) {
//            //            println(s"Q << ${i.id}")
//            LMXQ.push(i)
//          }
//        }
//      }
//    }
//
//    helper.States(m) = LCState.Success
//    return
//  }
//
//  def ACAsync(x: Var): Boolean = {
//    Q.clear()
//    if (x == null) {
//      //初始化
//      for (z <- vars) {
//        insert(z)
//      }
//    } else {
//      insert(x)
//    }
//    while (!Q.empty()) {
//      if (!helper.isConsistent) {
//        helper.isConsistent = false
//        helper.ACFinished = true
//        return false
//      }
//      val v = Q.pop()
//      for (c <- subscription(v.id)) {
//        if (helper.varStamp(v.id) > helper.tabStamp(c.id)) {
//          //          println("str(" + c.name + ")")
//          Y_evt.clear()
//          val consistent = c.AC(Y_evt)
//          helper.c_sum += 1
//          //          print(s"cid: ${c.id} yevt: ")
//          //          Y_evt.foreach(p => print(p.id, " "))
//          //          println()
//          if (!consistent) {
//            helper.isConsistent = false
//            helper.ACFinished = true
//            return false
//          } else {
//            for (y <- Y_evt) {
//              insert(y)
//            }
//          }
//          helper.globalStamp += 1
//          helper.tabStamp(c.id) = helper.globalStamp
//        }
//      }
//      helper.p_sum += 1
//    }
//    helper.ACFinished = true
//    return true
//  }
//
//  def insert(x: Var): Unit = {
//    Q.push(x)
//    helper.globalStamp += 1
//    helper.varStamp(x.id) = helper.globalStamp
//  }
//
//  //修改levelvdense
//  def selectLiteral(): Literal[Var] = {
//    var v = selectVar()
//    return new Literal[Var](v, v.minValue())
//  }
//
//  def selectVar(): Var = {
//    var mindmdd = Double.MaxValue
//    var minvid = 0
//
//    var i = helper.level
//    while (i < numVars) {
//      val vid = levelvdense(i)
//      val v = vars(vid)
//      var ddeg: Double = 0L
//
//      for (c <- subscription(vid)) {
//        if (c.assignedCount + 1 < c.arity) {
//          ddeg += 1
//        }
//      }
//
//      if (ddeg == 0) {
//        return v
//      }
//
//      val sizeD: Double = v.size.toDouble
//      val dmdd = sizeD / ddeg
//      //      println(s"${v.id}： size = ${sizeD}, ddeg = ${ddeg}, score = ${dmdd}")
//      if (dmdd < mindmdd) {
//        minvid = vid
//        mindmdd = dmdd
//      }
//      i += 1
//    }
//    return vars(minvid)
//  }
//
//  def newLevel(): Unit = {
//    helper.domainLock.lock()
//    helper.level += 1
//    for (v <- vars) {
//      v.newLevel()
//    }
//    helper.domainLock.unlock()
//    for (c <- tabs) {
//      c.newLevel()
//    }
//  }
//
//  def backLevel(i: Int): Unit = {
//    helper.domainLock.lock()
//    helper.isConsistent = true
//    helper.level = i
//    for (v <- vars) {
//      v.backLevel(i)
//    }
//    helper.domainLock.unlock()
//    for (c <- tabs) {
//      c.backLevel(i)
//    }
//  }
//
//  def newTmpLevel(): MultiLevel = {
//    val m = L.add(helper.level)
//    //    println("new tmplevel", m.toString())
//    for (v <- vars) {
//      v.newTmpLevel(m)
//    }
//    return m
//  }
//
//  def newSyncTmpLevel(m: MultiLevel) = {
//    for (v <- vars) {
//      v.newTmpLevel(m)
//    }
//  }
//
//  def deleteTmpLevel(m: MultiLevel): Unit = {
//    //    println("remove tmplevel", m.toString())
//    L.remove(m)
//  }
//
//  def backLevel(): Unit = {
//    helper.domainLock.lock()
//    helper.isConsistent = true
//    helper.level -= 1
//    for (v <- vars) {
//      v.backLevel()
//    }
//    helper.domainLock.unlock()
//    for (c <- tabs) {
//      c.backLevel()
//    }
//  }
//
//  def remove(literal: Literal[Var]): Unit = {
//    //约束的已实例化变量个数减1
//    for (c <- subscription(literal.v.id)) {
//      c.assignedCount -= 1
//    }
//    literal.v.remove(literal.a)
//    helper.globalStamp += 1
//    helper.varStamp(literal.v.id) = helper.globalStamp
//  }
//
//  def bind(literal: Literal[Var]): Unit = {
//    //在稀疏集上交换变量
//    val minvi = levelvsparse(literal.v.id)
//    val a = levelvdense(helper.level - 1)
//    levelvdense(helper.level - 1) = levelvdense(minvi)
//
//    levelvsparse(a) = minvi
//    levelvsparse(levelvdense(minvi)) = helper.level - 1
//
//    levelvdense(minvi) = a
//
//    for (c <- subscription(literal.v.id)) {
//      c.assignedCount += 1
//    }
//    //    //println(s"bind literal is ${literal.a}")
//    literal.v.bind(literal.a)
//    helper.globalStamp += 1
//    helper.varStamp(literal.v.id) = helper.globalStamp
//  }
//
//  def infoShow(): Unit = {
//    println("---------")
//    for (x <- vars) {
//      //      println(s"     var:${x.id} size:${x.size()}")
//      x.show()
//    }
//  }
//}
