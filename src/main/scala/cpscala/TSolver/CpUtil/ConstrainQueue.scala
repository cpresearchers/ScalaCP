package cpscala.TSolver.CpUtil

import cpscala.TSolver.Model.Constraint.PWConstraint.fPWCPropagator

import scala.reflect.ClassTag

class ConstrainQueue[VT <: fPWCPropagator :ClassTag](val num_vars: Int) {
  val max_size: Int = num_vars + 1
  val table = new Array[VT](max_size)
  val inStack = new Array[Boolean](max_size)
  var front: Int = 0
  var rear: Int = 0
  var size: Int = 0

  def full(): Boolean = {
    return front == (rear + 1) % max_size;
  }

  def push(v: VT) {
    if (inStack(v.id))
      return
    table(rear) = v
    rear = (rear + 1) % max_size
    inStack(v.id) = true
    size += 1
  }

  def safe_push(v: VT): Unit = this.synchronized {
    //    println("push: " + v.name)
    if (inStack(v.id))
      return
    table(rear) = v
    rear = (rear + 1) % max_size
    inStack(v.id) = true
    size += 1
  }

  def pop(): VT = {
    val tmp = table(front)
    front = (front + 1) % max_size
    inStack(tmp.id) = false
    size -= 1
    return tmp
  }

  def clear() {
    front = 0
    rear = 0
    size = 0

    var i = 0
    while (i < num_vars) {
      inStack(i) = false
      i += 1
    }
  }

  def empty(): Boolean = front == rear
}
