package cpscala.TSolver.CpUtil

import cpscala.TSolver.Model.Variable.Var

import scala.reflect.ClassTag

class AssignedStack[VT <: Var : ClassTag](num_vars: Int) {
  // table为变量集，按入栈顺序依次存放变量
  val table = new Array[VT](num_vars)
  // inStack为值集，第i位存放i号变量被赋的值
  val inStack = Array.fill[Int](num_vars)(-1)
  var index = -1

  def push(v: VT, a: Int): Unit = {
    index += 1
    table(index) = v
    inStack(v.id) = a
  }

  def pop(): (VT, Int) = {
    if (index == -1) sys.error("Stack empty")
    val v = table(index)
    val a = inStack(v.id)
    index -= 1
    return (v, a)
  }

  def full(): Boolean = index + 1 == num_vars

  def empty(): Boolean = index == -1

  def show(): Unit = {
    println(inStack.mkString(" "))
  }

  def toArray(): Array[Int] = {
    return inStack.clone()
  }
}
