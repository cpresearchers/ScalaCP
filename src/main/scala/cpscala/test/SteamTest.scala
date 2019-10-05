package cpscala.test

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.immutable.ParVector
import scala.collection.parallel.mutable.ParArray


object SteamTest extends App {
  val q: ArrayBuffer[Int] = ArrayBuffer.range(0, 10)

  println(q.mkString(","))

  val qp: ParArray[Int] = ParArray[Int](1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
//  qp.scan(0)((x: Int, Yevt: Int) => {
//    println(x, Yevt);
//    x + Yevt
//  })
  //  val qv: ParVector[Int] = ParVector[Int]()
  //  var i = 0
  //  while (i < 10) {
  //    qv :+ i
  //    i += 1
  //  }


  qp.filter(x => x % 2 == 0).foreach(x => println(x))

}
