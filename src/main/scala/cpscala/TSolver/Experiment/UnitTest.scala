package cpscala.TSolver.Experiment

import cpscala.TSolver.Model.Solver.Others.{LMXSparseSet, MultiLevel}

import scala.collection.mutable.ArrayBuffer

object UnitTest {
  def main(args: Array[String]): Unit = {
    multiLevelTest()
  }

  def multiLevelTest(): Unit = {
    println("xixi")
    val L = new LMXSparseSet(5, 5)
    val ms = new ArrayBuffer[MultiLevel]()
    println(L.size(), L.isFull(), L.nonFull())
    var m = L.add(2)
    println(m.toString(), L.size(), L.isFull(), L.nonFull())
    ms += m
    m = L.add(3)
    println(m.toString(), L.size(), L.isFull(), L.nonFull())
    ms += m
    m = L.add(4)
    println(m.toString(), L.size(), L.isFull(), L.nonFull())
    ms += m
    m = L.add(5)
    println(m.toString(), L.size(), L.isFull(), L.nonFull())
    ms += m
    m = L.add(6)
    println(m.toString(), L.size(), L.isFull(), L.nonFull())
    ms += m

    println(L.size())
    L.remove(ms(2))
    println(L.size())
    L.remove(ms(0))
    println(L.size())
    m = L.add(7)
    println(m.toString(), L.size(), L.isFull(), L.nonFull())
    ms += m
    m = L.add(8)
    println(m.toString(), L.size(), L.isFull(), L.nonFull())
    ms += m

    L.remove(ms(ms.length - 2))
    println(L.size())
    println("xixi")
  }

}
