package cpscala.TSolver.CpUtil

class MarkSet(intialSize: Int) {
  val dense = Array.range(0, intialSize)
  val sparse = Array.range(0, intialSize)
  var limits = -1

  def clear(): Unit = {
    limits = -1
  }

  def empty(): Boolean = {
    limits == -1
  }

  //
  def add(a: Int): Unit = {
    if (!has(a)) {
      swap(limits + 1, sparse(a))
      limits += 1
    }
  }

  def size(): Int = {
    return limits + 1
  }

  def has(a: Int): Boolean = {
    sparse(a) <= limits
  }

  def remove(a: Int): Unit = {
    if (has(a)) {
      swap(sparse(a), limits)
      limits -= 1
    }
  }

  def swap(i: Int, j: Int): Unit = {
    val tmp = dense(i)
    dense(i) = dense(j)
    dense(j) = tmp
    sparse(dense(i)) = i
    sparse(dense(j)) = j
  }
}
