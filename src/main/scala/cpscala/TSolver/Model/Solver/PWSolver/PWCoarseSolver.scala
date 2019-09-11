package cpscala.TSolver.Model.Solver.PWSolver

import cpscala.TSolver.CpUtil.PWRSparseBitSet
import cpscala.TSolver.Model.Constraint.PWConstraint.fPWCPropagator
import cpscala.TSolver.Model.Variable.Var
import cpscala.XModel.{XModel, XTab}

import scala.collection.mutable.ArrayBuffer

class PWCoarseSolver(xm: XModel, propagatorName: String, varType: String, heuName: String)extends PWSolver(xm, propagatorName, varType, heuName){

  val livingIndex =new ArrayBuffer[Int]()
  var first=true

  override def initialPropagate(): Boolean = {
    start_time = System.nanoTime
    prop_start_time = System.nanoTime
    return propagate(null)
  }

  override def checkConsistencyAfterAssignment(ix: Var): Boolean = {
    Y_evt+=ix
    return propagate(subscription(ix.id))
  }

  override def checkConsistencyAfterRefutation(ix: Var): Boolean = {
    return propagate(subscription(ix.id))
  }

  def propagate(x: ArrayBuffer[fPWCPropagator]): Boolean = {
    PWC_Q.clear()
    CT_Q.clear()
    var consistent=true
    if(first){
      first=false
      consistent=preProcess(tabs)
    }else{
      for(z<-x)
        CT_Q.push(z)
    }
    while (consistent && !CT_Q.empty()) {
      val c = CT_Q.pop()
//      println("   "+c.id+"   "+c.living.numSet())
      val dual = c.propagateGAC(Y_evt)
      consistent=dual._1
      helper.c_sum += 1

        if(dual._2){
          PWC_Q.push(c)
        }
        if(consistent&&CT_Q.empty()){
          for(x<-Y_evt){
            for(c<-subscription(x.id)){
              CT_Q.push(c)
//              insert(c)
            }
          }
          C_evt.clear()
          helper.subScopesSet=Set()
          while(consistent && !PWC_Q.empty()){
            val pwc = PWC_Q.pop()
            consistent=pwc.propagatePWC(Y_evt,C_evt)
            pwc.clearLivingDelta()
          }
//          println(C_evt.size)
          for(i<-C_evt){
            insert(tabs(i))
          }
          Y_evt.clear()
        }
    }
    return consistent
  }

  def insert(x:fPWCPropagator): Unit = {
    CT_Q.push(x)
    PWC_Q.push(x)
  }

  def preProcess(C:Array[fPWCPropagator]):Boolean={
    var consistent=true
    for (z <- tabs) {
      CT_Q.push(z)
    }
    while (!CT_Q.empty()) {
      val c = CT_Q.pop()
      Y_evt.clear()
      val dual = c.propagateGAC(Y_evt)
      consistent=dual._1
      helper.c_sum += 1
      if (!dual._1) {
        return false
      } else {
        for (y <- Y_evt) {
          for(tab<-subscription(y.id))
            CT_Q.push(tab)
        }
      }
    }
//    for (z <- tabs) {
//      print(z.living.numSet()+"  ")
//    }
//    println()

    if(consistent){
      consistent=initPWC(C)
      if(consistent){
        for(ci<-C)
          ci.clearLivingDelta()
        consistent=initPWC(C)
      }
    }
    return consistent
  }

  def initPWC(C:Array[fPWCPropagator]): Boolean={
    for(map<-helper.incidentCons){        //map._1为元组

      var cs:fPWCPropagator=tabs(map._2.head)
      for( ci <-map._2 ){
        if(tabs(ci).living.numSet()<cs.living.numSet()){
          cs=tabs(ci)
        }
        tabs(ci).living.clearMask()
      }

      val tocheck=new PWRSparseBitSet(cs.id,cs.getTupleLength(),numVars)
      cs.living.toCheck(tocheck)
      tocheck.getIndeces(livingIndex)
      var setindex:Set[Int]=Set()
      var i=0
      while (i < livingIndex.length) {
        if(!setindex.contains(livingIndex(i))) {

          setindex+=livingIndex(i)
          var tuplePWC = true
          for (ci <- map._2) {
            if (tuplePWC) {
              if (tabs(ci).interesectIndex(map._1, cs.getTuple(livingIndex(i)), cs.scope) == -1) {
                tuplePWC = false
              }
            }
          }
          if (tuplePWC) {
            for (ci <- map._2) {
              tabs(ci).addBlockToMask(map._1, cs.getTuple(livingIndex(i)), cs.scope)
            }
          }
          if(tocheck.removeBlock(cs.createBlock(map._1, cs.getTuple(livingIndex(i)))) && i!=livingIndex.length-1){
            i = 0
            tocheck.getIndeces(livingIndex)
          }else{
            i+=1
          }
        }else{
          i+=1
        }
      }

      for (ci <- map._2) {
        if(tabs(ci).living.intersectWithMask()){
          insert(tabs(ci))
        }
        if(tabs(ci).living.numSet()==0)return false
      }
    }
    return true
  }
}
