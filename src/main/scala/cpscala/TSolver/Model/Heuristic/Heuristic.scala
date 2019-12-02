package cpscala.TSolver.Model.Heuristic

import cpscala.TSolver.Model.Variable.Var

import scala.reflect.ClassTag


abstract class Heuristic[VT <: Var :ClassTag]() {


  def selectLiteral(level: Int, levelvdense: Array[Int]): (VT, Int)

//  def onFail(): Unit

}
