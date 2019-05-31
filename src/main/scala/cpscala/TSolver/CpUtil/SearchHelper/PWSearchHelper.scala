package cpscala.TSolver.CpUtil.SearchHelper

import cpscala.TSolver.Model.Constraint.PWConstraint.fPWCPropagator

import scala.collection.mutable.ArrayBuffer

class PWSearchHelper(override val numVars: Int, override val numTabs: Int) extends SearchHelper(numVars,numTabs) {

  var incidentCons:Map[ArrayBuffer[Int],Set[Int]] = Map()     //子集，约束
  var incidentSubscopes:Map[Int,Set[ArrayBuffer[Int]]]=Map()
  var subScopesSet:Set[ArrayBuffer[Int]]=null     //确认此子集已经被执行存在更新
  var tabs = new Array[fPWCPropagator](numTabs)
  var assignVar=0

}
