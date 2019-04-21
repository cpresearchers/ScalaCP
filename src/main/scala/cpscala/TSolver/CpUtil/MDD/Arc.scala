package cpscala.TSolver.CpUtil.MDD

class Arc(val label: Int, var start: Node, var end: Node) {
  def show(): Unit = ???
  def equal(right: Arc): Boolean = ???
}

object Arc {
  def equivalent(arc1: Arc, arc2: Arc): Boolean = {
    if ((arc1.label == arc2.label) && (arc1.end == arc2.end)) {
      return true
    } else {
      return false
    }
  }
}