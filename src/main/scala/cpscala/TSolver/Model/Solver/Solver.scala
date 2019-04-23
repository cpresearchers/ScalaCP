package cpscala.TSolver.Model.Solver

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper
import cpscala.TSolver.CpUtil.{AssignedStack, CoarseQueue, Literal}
import cpscala.TSolver.Model.Constraint.DSPConstraint.DSPPropagator
import cpscala.TSolver.Model.Constraint.IPConstraint.IPPropagator
import cpscala.TSolver.Model.Constraint.IPbitConstraint.IPbitPropagator
import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.Var
import cpscala.XModel.{XModel, XTab, XVar}

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

abstract class Solver[VT <: Var :ClassTag, PT <: Propagator with IPPropagator with IPbitPropagator with DSPPropagator :ClassTag](xm: XModel, propagatorName: String, varType: String, heuName: String) {
  val numVars: Int = xm.num_vars
  val numTabs: Int = xm.num_tabs
  val vars = new Array[VT](numVars)
  val tabs = new Array[PT](numTabs)
  val helper = new SearchHelper(numVars, numTabs)

  //记录已赋值的变量
  val levelvsparse = Array.range(0, numVars)
  val levelvdense = Array.range(0, numVars)
  //记录已entail 的约束
  //  val levelcsparse = Array.range(0, numTabs)
  //  val levelcdense = Array.range(0, numTabs)
  //  val clevel = Array.fill(numVars + 1)(-1)

  val subscription = new Array[ArrayBuffer[PT]](numVars)
  for (i <- 0 until numVars) {
    subscription(i) = new ArrayBuffer[PT]()
  }

  val Q = new CoarseQueue[VT](numVars)
  var Y_evt: ArrayBuffer[VT] = new ArrayBuffer[VT](xm.max_arity)

  val I = new AssignedStack[VT](xm.num_vars)

  var start_time = 0L
  var branch_start_time = 0L
  var prop_start_time = 0L
  var back_start_time = 0L
  var end_time = 0L

  def search(time_limit: Long): Unit = {
    var finished = false

    //infoShow()
    //    //println("initial propagate")
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

    var literal: Literal[VT] = null

    while (!finished) {
      end_time = System.nanoTime
      helper.time = end_time - start_time
      if (helper.time > time_limit) {
        return
      }

      //            if (helper.nodes == 7) {
      //              infoShow()
      //              return
      //            }
      branch_start_time = System.nanoTime
      literal = selectLiteral()
      //println("new level --------------------------------------------")
      //infoShow()
      newLevel()
      helper.nodes += 1
      //println("nodes: " + helper.nodes)

      I.push(literal)
      //println("push: " + literal.toString())
      bind(literal)
      end_time = System.nanoTime
      helper.branchTime += (end_time - branch_start_time)

      prop_start_time = System.nanoTime
      consistent = checkConsistencyAfterAssignment(literal.v)
      end_time = System.nanoTime
      helper.propTime += (end_time - prop_start_time)
      //      infoShow()

      if (consistent && I.full()) {
        I.show()
        // 若想求出所有解，则将consistent置为false，且不返回
        end_time = System.nanoTime
        helper.time = end_time - start_time
        return
      }

      while (!consistent && !I.empty()) {
        back_start_time = System.nanoTime
        literal = I.pop()
        //println("pop:  " + literal.toString())
        backLevel()
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

  def initialPropagate(): Boolean

  def checkConsistencyAfterAssignment(x: VT): Boolean

  def checkConsistencyAfterRefutation(x: VT): Boolean

  def remove(v_a: Literal[VT]): Unit = {
    //约束的已实例化变量个数减1
    for (c <- subscription(v_a.v.id)) {
      //      if (c.assignedCount.toInt != c.assignedCount)
      //        c.assignedCount -= 0.5
      //      else
      c.assignedCount -= 1
    }
    v_a.v.remove(v_a.a)
  }

  //修改levelvdense
  def selectLiteral(): Literal[VT] = {
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
        return new Literal(v, v.minValue)
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

    return new Literal[VT](vars(minvid), vars(minvid).minValue)
  }

  def bind(literal: Literal[VT]): Unit = {
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
    literal.v.bind(literal.a)
  }

  def infoShow(): Unit

}

