package cpscala.TSolver.CpUtil

import cpscala.TSolver.Model.Variable.{PVar, Var}

class Val(iv: Var, ia: Int) {
  val v = iv
  val a = ia

  override def toString(): String = "(" + v.id + "," + a + ")"
}

class PVal(iv: PVar, ia: Int) {
  val v = iv
  val a = ia

  override def toString(): String = "(" + v.id + "," + a + ")"
}

class AssignedStack(num_vars: Int) {
  val table = new Array[Val](num_vars)
  val inStack = Array.fill[Int](num_vars)(-1)
  var index = -1

  def push(va: Val): Unit = {
    index += 1
    table(index) = va
    inStack(va.v.id) = va.a
  }

  def pop(): Val = {
    if (index == -1) sys.error("Stack empty")
    val x = table(index)
    table(index) = null
    index -= 1
    inStack(x.v.id) = -1
    x
  }

  def full(): Boolean = index + 1 == num_vars

  def empty(): Boolean = index == -1

  def show(): Unit = {
    println(inStack.mkString(" "))
  }
}

class PAssignedStack(num_vars: Int) {
  val table = new Array[PVal](num_vars)
  val inStack = Array.fill[Int](num_vars)(-1)
  var index = -1

  def push(va: PVal): Unit = {
    index += 1
    table(index) = va
    inStack(va.v.id) = va.a
  }

  def pop(): PVal = {
    if (index == -1) sys.error("Stack empty")
    val x = table(index)
    table(index) = null
    index -= 1
    inStack(x.v.id) = -1
    x
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
