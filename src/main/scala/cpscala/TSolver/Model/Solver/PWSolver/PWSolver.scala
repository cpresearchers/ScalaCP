package cpscala.TSolver.Model.Solver.PWSolver

import cpscala.TSolver.CpUtil.{AssignedStack, ConstrainQueue}
import cpscala.TSolver.CpUtil.SearchHelper.PWSearchHelper
import cpscala.TSolver.Model.Constraint.DSPConstraint.DSPPropagator
import cpscala.TSolver.Model.Constraint.PWConstraint.{TablePWCT, TablePWCT1, fPWCPropagator}
import cpscala.TSolver.Model.Heuristic.{HeuDomDdeg, HeuDomWdeg, Heuristic}
import cpscala.TSolver.Model.Variable._
import cpscala.XModel.{XModel, XTab, XVar}

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

abstract class PWSolver(xm: XModel, propagatorName: String, varType: String, heuName: String) {
  val numVars: Int = xm.num_vars
  val numTabs: Int = xm.num_tabs
  val vars = new Array[Var](numVars)
  val tabs = new Array[fPWCPropagator](numTabs)
  val helper = new PWSearchHelper(numVars, numTabs)

  // 启发式对象
  var heuristic: Heuristic[Var] = null

  //记录已赋值的变量
  val levelvsparse = Array.range(0, numVars)
  val levelvdense = Array.range(0, numVars)
  //记录已entail 的约束
  //  val levelcsparse = Array.range(0, numTabs)
  //  val levelcdense = Array.range(0, numTabs)
  //  val clevel = Array.fill(numVars + 1)(-1)

  val subscription = new Array[ArrayBuffer[fPWCPropagator]](numVars)
  for (i <- 0 until numVars) {
    subscription(i) = new ArrayBuffer[fPWCPropagator]()
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
    case "PW-CT" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TablePWCT(xc.id, xc.arity, numVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }
    case "PW-CT1" => {
      for (i <- 0 until numTabs) {
        val xc: XTab = xm.tabs.get(i)
        val ts: Array[Array[Int]] = xc.tuples
        val scope: Array[Var] = for (i <- (0 until xc.arity).toArray) yield vars(xc.scopeInt(i))
        tabs(i) = new TablePWCT1(xc.id, xc.arity, numVars, scope, ts, helper)
        for (v <- scope) {
          subscription(v.id) += tabs(i)
        }
      }
    }
  }
  helper.tabs = tabs

  // 初始化启发式对象
  heuName match {
    case "Dom/Ddeg" => {
      heuristic = new HeuDomDdeg[Var, fPWCPropagator](numVars, vars, subscription)
    }

    case "Dom/Wdeg" => {
      heuristic = new HeuDomWdeg[Var, fPWCPropagator](numVars, vars, subscription)
    }
  }

  //获取公共变量（可实现minidual）
  val mini:minidual=new minidual(xm,true)
  val tabsScopeMatrixlist=mini.tabsScopeMatrix
  var tabsScopeMatrix=new Array[Array[ArrayBuffer[Int]]](numTabs)
  var i = 0
  while (i < numTabs - 1) {
    var t0 = xm.tabs.get(i);
    var j = i + 1
    tabsScopeMatrix(i) = new Array[ArrayBuffer[Int]](numTabs)
    while (j < numTabs) {
      var t1 = xm.tabs.get(j);
      tabsScopeMatrix(i)(j) = new ArrayBuffer[Int]();
      var nn=0
      if(tabsScopeMatrixlist(i)(j)!=null) {
        for (nn <- tabsScopeMatrixlist(i)(j)) {
          tabsScopeMatrix(i)(j) += nn
        }
      }
      j += 1
    }
    i += 1
  }
  //根据tabsScopeMatrix构建help数据结构
  var incidentCon: Map[ArrayBuffer[Int], Set[Int]] = Map() //子集，约束
  var incidentSubscope: Map[Int, Set[ArrayBuffer[Int]]] = Map()
  i = 0
  var con: Set[Int] = Set()
  while (i < numTabs - 1) {
    var j = i + 1
    while (j < numTabs) {
      var aca = tabsScopeMatrix(i)(j)
      if (aca != null) {
        aca.sorted
        if(aca.size>1) {
          if (incidentCon.contains(aca)) {
            var setCon = incidentCon(aca)
            setCon += i
            setCon += j
            incidentCon += (aca -> setCon)
            if (con.contains(i)) {
              var setSubscope = incidentSubscope(i)
              setSubscope += aca
              incidentSubscope += (i -> setSubscope)
            } else {
              var setSubscope: Set[ArrayBuffer[Int]] = Set()
              setSubscope += aca
              incidentSubscope += (i -> setSubscope)
              con += i
            }
            if (con.contains(j)) {
              var setSubscope = incidentSubscope(j)
              setSubscope += aca
              incidentSubscope += (j -> setSubscope)
            } else {
              var setSubscope: Set[ArrayBuffer[Int]] = Set()
              setSubscope += aca
              incidentSubscope += (j -> setSubscope)
              con += j
            }
          } else {
            var setCon: Set[Int] = Set()
            setCon += i
            setCon += j
            incidentCon += (aca -> setCon)
            if (con.contains(i)) {
              var setSubscope = incidentSubscope(i)
              setSubscope += aca
              incidentSubscope += (i -> setSubscope)
            } else {
              var setSubscope: Set[ArrayBuffer[Int]] = Set()
              setSubscope += aca
              incidentSubscope += (i -> setSubscope)
              con += i
            }
            if (con.contains(j)) {
              var setSubscope = incidentSubscope(j)
              setSubscope += aca
              incidentSubscope += (j -> setSubscope)
            } else {
              var setSubscope: Set[ArrayBuffer[Int]] = Set()
              setSubscope += aca
              incidentSubscope += (j -> setSubscope)
              con += j
            }
          }
        }
      }
      j += 1
    }
    i += 1
  }
//  println(incidentSubscope.size)
//  println(incidentCon.size)
  helper.incidentCons = incidentCon
  helper.incidentSubscopes = incidentSubscope
//  for(i<-incidentSubscope){
//    print(i._1+"  ")
//    print(i._2)
//    println()
//  }

  val CT_Q = new ConstrainQueue[fPWCPropagator](numTabs)
  val PWC_Q = new ConstrainQueue[fPWCPropagator](numTabs)
  var Y_evt: ArrayBuffer[Var] = new ArrayBuffer[Var](numVars)
  var C_evt: ArrayBuffer[Int] = new ArrayBuffer[Int](numTabs)

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
