package cpscala.TSolver.CpUtil

import scala.collection.mutable
import scala.reflect.ClassTag

// 在STR3和STRbit中记录边界信息，用于回溯
class RestoreStack[T1: ClassTag, T2: ClassTag](val size: Int) {
  private val hashArray = Array.fill(size + 1)(new mutable.HashMap[T1, T2])
  private var level = -1

  def push(): Unit = {
    level += 1
    hashArray(level).clear()
  }

  def pop: mutable.HashMap[T1, T2] = {
    val tempHash = hashArray(level)
    level -= 1
    tempHash
  }

  def top: mutable.HashMap[T1, T2] = {
    hashArray(level)
  }
}
