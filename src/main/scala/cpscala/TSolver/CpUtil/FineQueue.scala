package cpscala.TSolver.CpUtil

/**
  * 细粒度队列，其中的元素是（变量_值）对。
  */

class FineQueue(val num_vars: Int, val max_domin_size: Int) {
  val max_size: Int = num_vars * max_domin_size + 1
  val table = new Array[Val](max_size)
  val inStack = Array.ofDim[Boolean](num_vars, max_domin_size)
  var front: Int = 0
  var rear: Int = 0
  var size: Int = 0

  def full(): Boolean = {
    return front == (rear + 1) % max_size;
  }

  def push(v_a: Val) {
    val v = v_a.v
    val a = v_a.a
    if (inStack(v.id)(a))
      return
    table(rear) = v_a
    rear = (rear + 1) % max_size
    inStack(v.id)(a) = true
    size += 1
  }

  def safe_push(v_a: Val): Unit = this.synchronized {
    //    println("push: " + v_a.name)
    val v = v_a.v
    val a = v_a.a
    if (inStack(v.id)(a))
      return
    table(rear) = v_a
    rear = (rear + 1) % max_size
    inStack(v.id)(a) = true
    size += 1
  }

  def pop(): Val = {
    val tmp = table(front)
    val v = tmp.v
    val a = tmp.a
    front = (front + 1) % max_size
    inStack(v.id)(a) = false
    size -= 1
    return tmp
  }

  def clear() {
    front = 0
    rear = 0
    size = 0

    var i = 0
    var j = 0
    while (i < num_vars) {
      while (j < max_domin_size) {
        inStack(i)(j) = false
        j += 1
      }
      i += 1
    }
  }

  def empty(): Boolean = front == rear
}
