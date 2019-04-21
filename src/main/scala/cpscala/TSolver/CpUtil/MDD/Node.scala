package cpscala.TSolver.CpUtil.MDD

import scala.collection.mutable.ArrayBuffer

class Node(val name: Long, val level: Int) {
  val incomes: ArrayBuffer[Arc] = new ArrayBuffer[Arc]()
  val outcomes: ArrayBuffer[Arc] = new ArrayBuffer[Arc]()
  var active: Boolean = true

  def addIncome(arc: Arc): Unit = {
    incomes += arc
  }

  def addOutcome(label: Int, destination: Node): Arc = {
    val arc = new Arc(label, this, destination)
    outcomes += arc
    return arc
  }

  def addOutcome(arc: Arc) = {
    outcomes += arc
  }

  def deactivate(): Unit = {
    active = false
  }

  def activate() = {
    active = true
  }

  def isActive() = active

  def merge(other: Node): Unit = {
    //# 1. 将other合并入self中，other应该被删去
    //# 2. 将两个incomes的list合并
    //# 3. 节点deactivate
    //# 4. 节点删除放在MDD方法中
    //# 6. 对于other.incomes所有的弧arc的end都指向self节点
    //# 7. 对于other.outcomes所有的弧arc的end节点的incomes删除这个弧arc
    //        if (!(equal(other)))
    // 两个点是一个点,即name相同,
    // 不合并,只有等价时才合并
    if (name != other.name) {
      // 合并掉other节点的incomes弧
      // other.incomes中每个弧的end节点重定向为当前节点并加入到this.incomes中
      for (arc <- other.incomes) {
        arc.end = this
        incomes += arc
      }

      // 清除other节点的所有income节点,在delete other 节点时不会删除这些income-arc
      other.incomes.clear()
      // 反激活other
      other.deactivate()
    }
  }

  def show(seperator: String): Unit = ???
}
