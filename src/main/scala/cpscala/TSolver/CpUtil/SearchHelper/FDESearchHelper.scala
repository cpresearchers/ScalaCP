package cpscala.TSolver.CpUtil.SearchHelper

import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.Var

class FDESearchHelper(override val numVars: Int, override val numTabs: Int) extends SearchHelper(numVars, numTabs) {
  var num_old = 0;
  var vcMap: Map[Int, Var] = Map() //新变量，新约束
}
