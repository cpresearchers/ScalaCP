package cpscala.TSolver.CpUtil.MDD

import scala.collection.mutable.HashSet

class Pack(val S:HashSet[Node],var pos: Int) {
//  var S: HashSet[Node]

  //  var pos = poss
//  def this(SS: HashSet[Node]) = {
//    S = SS
//  }


  def size(): Unit = {
    return S.size
  }

}
