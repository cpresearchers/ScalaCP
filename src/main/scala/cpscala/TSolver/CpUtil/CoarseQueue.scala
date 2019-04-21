package cpscala.TSolver.CpUtil

import cpscala.TSolver.Model.Variable.Var
import scala.reflect.ClassTag

/**
  *粗粒度队列，其中的元素是变量。
  */

  class CoarseQueue[T <: Var :ClassTag](val num_vars: Int) {
  val max_size: Int = num_vars + 1
  val table = new Array[T](max_size)
  val inStack = new Array[Boolean](max_size)
  var front: Int = 0
  var rear: Int = 0
  var size: Int = 0

  def full(): Boolean = {
    return front == (rear + 1) % max_size;
  }

  def push(v: T) {
    if (inStack(v.id))
      return
    table(rear) = v
    rear = (rear + 1) % max_size
    inStack(v.id) = true
    size += 1
  }

  def safe_push(v: T): Unit = this.synchronized {
    //    println("push: " + v.name)
    if (inStack(v.id))
      return
    table(rear) = v
    rear = (rear + 1) % max_size
    inStack(v.id) = true
    size += 1
  }

  def pop(): T = {
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
