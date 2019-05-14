//package cpscala.TSolver.CpUtil
//
//import cpscala.TSolver.Model.Variable.Var
//
//import scala.reflect.ClassTag
//
///**
//  * 细粒度队列，其中的元素是（变量_值）对。
//  */
//
//class FineQueue[VT <: Var :ClassTag](val numVars: Int, val max_domin_size: Int) {
//  val max_size: Int = numVars * max_domin_size + 1
//  val table = new Array[Literal[VT]](max_size)
//  val inStack = Array.ofDim[Boolean](numVars, max_domin_size)
//  var front: Int = 0
//  var rear: Int = 0
//  var size: Int = 0
//
//  def full(): Boolean = {
//    return front == (rear + 1) % max_size;
//  }
//
//  def push(literal: Literal[VT]) {
//    val v = literal.v
//    val a = literal.a
//    if (inStack(v.id)(a))
//      return
//    table(rear) = literal
//    rear = (rear + 1) % max_size
//    inStack(v.id)(a) = true
//    size += 1
//  }
//
//  def safe_push(literal: Literal[VT]): Unit = this.synchronized {
//    //    println("push: " + literal.name)
//    val v = literal.v
//    val a = literal.a
//    if (inStack(v.id)(a))
//      return
//    table(rear) = literal
//    rear = (rear + 1) % max_size
//    inStack(v.id)(a) = true
//    size += 1
//  }
//
//  def pop(): Literal[VT] = {
//    val tmp = table(front)
//    val v = tmp.v
//    val a = tmp.a
//    front = (front + 1) % max_size
//    inStack(v.id)(a) = false
//    size -= 1
//    return tmp
//  }
//
//  def clear() {
//    front = 0
//    rear = 0
//    size = 0
//
//    var i = 0
//    var j = 0
//    while (i < numVars) {
//      while (j < max_domin_size) {
//        inStack(i)(j) = false
//        j += 1
//      }
//      i += 1
//    }
//  }
//
//  def empty(): Boolean = front == rear
//}
