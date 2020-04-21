package cpscala.TSolver.CpUtil.SearchHelper

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.concurrent.locks.ReentrantLock

import cpscala.TSolver.Model.Constraint.DSPConstraint.DSPPropagator
//import cpscala.TSolver.Model.Constraint.DSPFDEConstraint.DSPFDEPropagator
import cpscala.TSolver.Model.Variable.{PVar, Var}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class DSPFDESearchHelper(override val numVars: Int, override val numTabs: Int, override val parallelism: Int) extends DSPSearchHelper(numVars, numTabs, parallelism) {
  var num_old = 0;
  var vcMap: Map[Int, PVar] = Map() //新变量，新约束ß
  override val subscription: Array[ArrayBuffer[DSPPropagator]] = new Array[ArrayBuffer[DSPPropagator]](numVars)
}
