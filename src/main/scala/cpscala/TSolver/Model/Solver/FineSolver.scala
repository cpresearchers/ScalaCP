package cpscala.TSolver.Model.Solver

import cpscala.TSolver.Model.Variable.{Var}
import cpscala.XModel.{XModel}

/**
  * 细粒度求解器，适用于STR3和STRbit。
  */

class FineSolver(xm: XModel, propagator_name: String, var_type: String, heu_name: String) extends Solver(xm, propagator_name, var_type, heu_name) {

  override def initialPropagate(): Boolean = {

    // 表约束初始化
    for (c <- tabs) {
      c.setup()
    }

    start_time = System.nanoTime
    prop_start_time = System.nanoTime
    helper.globalStamp += 1
    // 初始删值
    for (c <- tabs) {
      if(!c.setup()){
        return false
      }
    }

    // 初始传播
    Q.clear()
    var i = 0
    for (i <- 0 until numVars) {
      //println(s"       var:${i} stamp: ${helper.varStamp(i)}")
      if (helper.varStamp(i) != 0) {
        //println(s"       var:${i} changed")
        insert(vars(i))
      }
    }

    while (!Q.empty()) {
      val v = Q.pop()
      //println(s"       var:${v.id} =========>")
      for (c <- subscription(v.id)) {
        if (helper.varStamp(v.id) > helper.tabStamp(c.id)) {
                    //println("str(" + c.id + ")")
          Y_evt.clear()
          val consistent = c.propagate(Y_evt)
          helper.c_sum += 1
          //          print(s"cid: ${c.id} yevt: ")
          //          Y_evt.foreach(p => print(p.id, " "))
          //          //println()
          if (!consistent) {
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
    }
    return true
  }

  override def checkConsistencyAfterAssignment(x: Var): Boolean = {

    Q.clear()
    insert(x)
    while (!Q.empty()) {
      val v = Q.pop()
      for (c <- subscription(v.id)) {
        if (helper.varStamp(v.id) > helper.tabStamp(c.id)) {
          //          //println("str(" + c.id + ")")
          Y_evt.clear()
          val consistent = c.propagate(Y_evt)
          helper.c_sum += 1
          //          print(s"cid: ${c.id} yevt: ")
          //          Y_evt.foreach(p => print(p.id, " "))
          //          //println()
          if (!consistent) {
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
    }
    return true
  }

  override def checkConsistencyAfterRefutation(x: Var): Boolean = {

    Q.clear()
    insert(x)
    while (!Q.empty()) {
      val v = Q.pop()
      for (c <- subscription(v.id)) {
        if (helper.varStamp(v.id) > helper.tabStamp(c.id)) {
          //          //println("str(" + c.id + ")")
          Y_evt.clear()
          val consistent = c.propagate(Y_evt)
          helper.c_sum += 1
          //          print(s"cid: ${c.id} yevt: ")
          //          Y_evt.foreach(p => print(p.id, " "))
          //          //println()
          if (!consistent) {
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
    }
    return true
  }

  def insert(x: Var): Unit = {
    Q.push(x)
    helper.globalStamp += 1
    helper.varStamp(x.id) = helper.globalStamp
  }

}
