package cpscala.TSolver.CpUtil

import cpscala.TSolver.Model.Variable.Var

import scala.reflect.ClassTag

class Literal[VT <: Var : ClassTag](val v: VT, val a: Int) {
  override def toString(): String = "(" + v.id + "," + a + ")"

  def invalid() = a == INDEX.kOVERFLOW
}

class AssignedStack[VT <: Var : ClassTag](num_vars: Int) {
  val table = new Array[Literal[VT]](num_vars)
  val inStack = Array.fill[Int](num_vars)(-1)
  var index = -1

  def push(literal: Literal[VT]): Unit = {
    index += 1
    table(index) = literal
    inStack(literal.v.id) = literal.a
  }

  def pop(): Literal[VT] = {
    if (index == -1) sys.error("Stack empty")
    val literal = table(index)
    table(index) = null
    index -= 1
    inStack(literal.v.id) = -1
    literal
  }

  def size() = index + 1

  def backToLevel(i: Int): Unit = {
    if (size() > i) {
      pop()
    }
  }

  def contain(literal: Literal[VT]): Boolean = {
    return inStack(literal.v.id) == literal.a
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

