package cpscala.TSolver.Model.Constraint.PWConstraint

import cpscala.TSolver.CpUtil.SearchHelper.PWSearchHelper
import cpscala.TSolver.CpUtil.{Block, Constants, INDEX, PWRSparseBitSet}
import cpscala.TSolver.Model.Variable.Var

import scala.collection.mutable.ArrayBuffer

class TablePWCT1(val id: Int, val arity: Int, val num_vars: Int, val scope: Array[Var], val tuples: Array[Array[Int]], val helper: PWSearchHelper) extends fPWCPropagator {

  val tocheck = new PWRSparseBitSet(id, tuples.length, num_vars)
  living = new PWRSparseBitSet(id, tuples.length, num_vars)
  var tupToCheck = new PWRSparseBitSet(id, tuples.length, num_vars)
  var supports = new Array[Array[PWRSparseBitSet]](arity)
  val support = new Array[Array[Array[Long]]](arity)
  var indices = new Array[Array[PWRSparseBitSet]](arity)
  val indice = new Array[Array[Array[Long]]](arity)
  val num_bit = living.num_bit
  var index = new ArrayBuffer[Int]()
  val residues = new Array[Array[Int]](arity)
  level = 0

  var tupe = new Array[Int](scope.length)
  var b: Block = new Block
  b.sets = new ArrayBuffer[PWRSparseBitSet]()
  b.commonIndices = new PWRSparseBitSet(id, num_bit, num_vars)
  var ind = new ArrayBuffer[PWRSparseBitSet]()

  for (i <- 0 until arity) {
    support(i) = Array.ofDim[Long](scope(i).size, num_bit)
    supports(i) = new Array[PWRSparseBitSet](scope(i).size)
    indices(i) = new Array[PWRSparseBitSet](scope(i).size)
    indice(i) = Array.ofDim[Long](scope(i).size, Math.ceil(num_bit.toDouble / Constants.BITSIZE.asInstanceOf[Double]).toInt)
    residues(i) = Array.fill(scope(i).size)(-1)
  }
  var ii = 0
  while (ii < tuples.length) {
    //  for (i <- 0 until tuples.length) {
    val (x, y) = INDEX.getXY(ii)
    val t = tuples(ii)
    for (j <- 0 until t.length) {
      support(j)(t(j))(x) |= Constants.MASK1(y)
    }
    ii += 1
  }
  for (i <- 0 until arity) {
    for (j <- 0 until scope(i).size) {
      supports(i)(j) = new PWRSparseBitSet(scope(i).id * 10 + j, tuples.length, num_vars)
      supports(i)(j).addToMask(support(i)(j))
      supports(i)(j).intersectWithMask()

    }
  }
  for (i <- 0 until arity) {
    for (j <- 0 until scope(i).size) {
      ii = 0
      for (n <- 0 until indice(i)(j).length)
        indice(i)(j)(n) = 0L
      while (ii < num_bit) {
        val (x, y) = INDEX.getXY(ii)
        val t = support(i)(j)(ii)
        if (t != 0) {
          indice(i)(j)(x) |= Constants.MASK1(y)
        }
        ii += 1
      }
    }
  }
  for (i <- 0 until arity) {
    for (j <- 0 until scope(i).size) {
      indices(i)(j) = new PWRSparseBitSet(scope(i).id * 10 + j, num_bit, num_vars)
      indices(i)(j).addToMask(indice(i)(j))
      indices(i)(j).intersectWithMask()
    }
  }

  //存变量Index
  val Ssup = new ArrayBuffer[Int](arity)
  val Sval = new ArrayBuffer[Int](arity)
  // 变量的比特组个数
  private[this] val varNumBit: Array[Int] = Array.tabulate[Int](arity)(i => scope(i).getNumBit())
  // lastMask与变量Mask不同的值是该约束两次传播之间被过滤的值（delta）
  private[this] val lastMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  // 在约束传播开始时localMask获取变量最新的mask
  private[this] val localMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  // 记录该约束两次传播之间删值的mask
  private[this] val removeMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  // 保存delta或者变量的剩余有效值
  private[this] val values = new ArrayBuffer[Int]()
  // 是否首次传播
  var firstPropagate = true

  private[this] val PWlastMask = Array.tabulate(arity)(i => new Array[Long](varNumBit(i)))
  for (i <- 0 until arity) {
    val v = scope(i)
    v.mask(PWlastMask(i))
  }

  //检查变量
  def initial(): Boolean = {
    Ssup.clear()
    Sval.clear()
    // 标记SVal是否为空，为空则跳出propagate
    var snapshotChanged = false
    var i = 0
    while (i < arity) {
      var diff = false
      val v = scope(i)
      v.mask(localMask(i))
      // 本地论域快照与全局论域不同
      // 更新本地论域快照
      // snapshotChanged 即为需要propagate，否则不用propagate
      var j = 0
      while (j < varNumBit(i)) {
        if (lastMask(i)(j) != localMask(i)(j)) {
          diff = true
        }
        j += 1
      }
      if (diff) {
        Sval += i
        snapshotChanged = true
      }
      if (v.unBind()) {
        Ssup += i
      }
      i += 1
    }
    return snapshotChanged
  }

  def updateTable(): (Boolean, Boolean) = {
    //    //println(s"      ut cid: ${id}===========>")
    var changed = false
    var i = 0
    while (i < Sval.length) {
      val vv: Int = Sval(i)
      val v: Var = scope(vv)
      //println(s"cid: ${id}, vid: ${v.id}: localMask ${Constants.toFormatBinaryString(localMask(vv)(0))}")
      // 获得delta更新数据
      var numValid = 0
      var numRemoved = 0
      var j = 0
      while (j < varNumBit(vv)) {
        removeMask(vv)(j) = 0L
        removeMask(vv)(j) = (~localMask(vv)(j)) & lastMask(vv)(j)
        numRemoved += java.lang.Long.bitCount(removeMask(vv)(j))
        numValid += java.lang.Long.bitCount(localMask(vv)(j))
        lastMask(vv)(j) = localMask(vv)(j)
        j += 1
      }
      living.clearMask()
      if (numRemoved >= numValid || firstPropagate) {
        v.getValidValues(values)
        for (a <- values) {
          living.addToMask(supports(vv)(a))
        }
      } else {
        Constants.getValues(removeMask(vv), values)
        for (a <- values) {
          living.addToMask(supports(vv)(a))
        }
        living.reverseMask()
      }
      changed = living.intersectWithMask()
      //传播失败
      if (living.isEmpty()) {
        //println(s"update faild!!: ${Thread.currentThread().getName}, cid: ${id}")
        return (false, changed)
      }
      i += 1
    }
    firstPropagate = false
    return (true, changed)
  }

  def filterDomains(y: ArrayBuffer[Var]): Boolean = {
    for (vv <- Ssup) {
      var deleted: Boolean = false
      val v = scope(vv)
      v.getValidValues(values)
      for (a <- values) {
        var index = residues(vv)(a)
        if (index == Constants.kINDEXOVERFLOW || (living.words(helper.level)(index) & supports(vv)(a).words(0)(index)) == 0L) { //res失效
          index = living.intersectIndex(supports(vv)(a))
          if (index != -1) { //重新找到支持
            residues(vv)(a) = index
          }
          else {
            deleted = true
            //无法找到支持, 删除(v, a)
            //println(s"      cons:${id} var:${v.id} remove new value:${a}")
            v.remove(a)
          }
        }
      }
      if (deleted) { //ct改变的变量
        // 论域删空退出
        if (v.isEmpty()) {
          //println(s"filter faild!!: ${Thread.currentThread().getName}, cid: ${id}, vid: ${v.id}")
          return false
        }
        //更新lastMask
        v.mask(lastMask(vv))
        y += v
      }
    }
    return true
  }

  //两个参数CT修改变量，返回添加约束集合
  def enforcePWC(xEvt: ArrayBuffer[Var], evt: ArrayBuffer[Int]): Boolean = {
    living.computeDelta(tupToCheck)
    if (living.numSet() >= tupToCheck.numSet()) {
      //    println(id + "   " + tupToCheck.numSet() + "  " + living.numSet()+"   "+xEvt.length)
      //    for(i <-0 until arity){
      //      println(scope(i).id)
      //    }
      if (helper.incidentSubscopes.contains(id)) {
        val sub = helper.incidentSubscopes(id)

        for (vSub <- sub) {
          tupToCheck.save()
          //              println(vSub + "  " + tupToCheck.numSet())
          for (vv <- vSub) {
            var v: Var = null
            var vposition = 0
            for (i <- 0 until scope.length) {
              if (scope(i).id == vv) {
                v = scope(i)
                vposition = i
              }
            }
            if (xEvt.contains(v)) {
              var numValid = 0
              var numRemoved = 0
              var j = 0
              while (j < varNumBit(vposition)) {
                removeMask(vposition)(j) = 0L
                removeMask(vposition)(j) = (~localMask(vposition)(j)) & PWlastMask(vposition)(j)
                numRemoved += java.lang.Long.bitCount(removeMask(vposition)(j))
                numValid += java.lang.Long.bitCount(localMask(vposition)(j))
                j += 1
              }
              if (numRemoved > numValid) {
                tupToCheck.clearMask()
                v.getValidValues(values)
                for (a <- values) {
                  tupToCheck.addToMask(supports(vposition)(a))
                }
                tupToCheck.intersectWithMask()
                //                            println("tupToCheck  "+tupToCheck.intersectWithMask())
                //              println(tupToCheck.numSet())
              } else {
                Constants.getValues(removeMask(vposition), values)
                for (a <- values) {
                  var b: Block = new Block
                  b.sets = new ArrayBuffer[PWRSparseBitSet]()
                  b.sets += supports(vposition)(a)
                  //                supports(vposition)(a).show()
                  b.commonIndices = indices(vposition)(a)
                  tupToCheck.removeBlock(b)
                  //                                println("tupToCheck block  "+tupToCheck.removeBlock(b))
                  //                println(tupToCheck.numSet())
                }
              }
            }
          }
          //          println(vSub + "  " + tupToCheck.numSet())
          tupToCheck.getIndeces(index)
          //        println(index.length)
          var setindex: Set[Int] = Set()
          var i = 0
          while (i < index.length) {

            if (!setindex.contains(index(i))) {
              setindex += index(i)
              val consistent = reviseBlock(vSub, tuples(index(i)), evt)
              if (!consistent) return false
              if (tupToCheck.removeBlock(createBlock(vSub, tuples(index(i)))) && i != index.length - 1) {
                i = 0
                tupToCheck.getIndeces(index)
              } else {
                i += 1
              }
            } else {
              i += 1
            }
          }
          tupToCheck.restore()
        }
      }
    } else {
//      if (helper.incidentSubscopes.contains(id)) {
//        living.clearDelta()
//        for (sub <- helper.incidentSubscopes(id)) { //约束涉及的子集
//          for (ci <- helper.incidentCons(sub)) { //获取子集涉及的约束
//            helper.tabs(ci).living.clearMask()
//          }
//          living.toCheck(tocheck)
//          tocheck.getIndeces(index)
//          var setindex: Set[Int] = Set()
//          var i = 0
//          while (i < index.length) {
//            if (!setindex.contains(index(i))) {
//              setindex += index(i)
//              var tuplePWC = true
//              for (ci <- helper.incidentCons(sub)) {
//                if (tuplePWC) {
//                  if (helper.tabs(ci).interesectIndex(sub, tuples(index(i)), scope) == -1) {
//                    tuplePWC = false
//                  }
//                }
//              }
//              if (tuplePWC) {
//                for (ci <- helper.incidentCons(sub)) {
//                  helper.tabs(ci).addBlockToMask(sub, tuples(index(i)), scope)
//                }
//              }
//              if (tocheck.removeBlock(createBlock(sub, tuples(index(i)))) && i != index.length - 1) {
//                i = 0
//                tocheck.getIndeces(index)
//              } else {
//                i += 1
//              }
//            } else {
//              i += 1
//            }
//          }
//          for (ci <- helper.incidentCons(sub)) {
//            if (helper.tabs(ci).living.intersectWithMask()) {
//              if (!evt.contains(ci)) {
//                evt += ci
//              }
//            }
//            if (helper.tabs(ci).living.numSet() == 0) return false
//          }
//        }
////        living.computeDelta(tupToCheck)
////        if(tupToCheck.numSet()!=0) {
////          for (sub <- helper.incidentSubscopes(id)) {
////            tupToCheck.save()
////            tupToCheck.getIndeces(index)
////            //        println(index.length)
////            var setindex: Set[Int] = Set()
////            var i = 0
////            while (i < index.length) {
////
////              if (!setindex.contains(index(i))) {
////                setindex += index(i)
////                val consistent = reviseBlock(sub, tuples(index(i)), evt)
////                if (!consistent) return false
////                if (tupToCheck.removeBlock(createBlock(sub, tuples(index(i)))) && i != index.length - 1) {
////                  i = 0
////                  tupToCheck.getIndeces(index)
////                } else {
////                  i += 1
////                }
////              } else {
////                i += 1
////              }
////            }
////            tupToCheck.restore()
////          }
////        }
//
//      }
      if (helper.incidentSubscopes.contains(id)) {
        living.clearDelta()
        for (sub <- helper.incidentSubscopes(id)) { //约束涉及的子集
          if (!helper.subScopesSet.contains(sub)) {
            helper.subScopesSet+=sub
            for (ci <- helper.incidentCons(sub)) { //获取子集涉及的约束
              helper.tabs(ci).living.clearMask()
            }
            living.toCheck(tocheck)
            tocheck.getIndeces(index)
            var setindex: Set[Int] = Set()
            var i = 0
            while (i < index.length) {
              if (!setindex.contains(index(i))) {
                setindex += index(i)
                var tuplePWC = true
                for (ci <- helper.incidentCons(sub)) {
                  if (tuplePWC) {
                    if (helper.tabs(ci).interesectIndex(sub, tuples(index(i)), scope) == -1) {
                      tuplePWC = false
                    }
                  }
                }
                if (tuplePWC) {
                  for (ci <- helper.incidentCons(sub)) {
                    helper.tabs(ci).addBlockToMask(sub, tuples(index(i)), scope)
                  }
                }
                if (tocheck.removeBlock(createBlock(sub, tuples(index(i)))) && i != index.length - 1) {
                  i = 0
                  tocheck.getIndeces(index)
                } else {
                  i += 1
                }
              } else {
                i += 1
              }
            }
            for (ci <- helper.incidentCons(sub)) {
              if (helper.tabs(ci).living.intersectWithMask()) {
                if (!evt.contains(ci)) {
                  evt += ci
                }
              }
              if (helper.tabs(ci).living.numSet() == 0) return false
            }
          }
          //        living.computeDelta(tupToCheck)
          //        if(tupToCheck.numSet()!=0) {
          //          for (sub <- helper.incidentSubscopes(id)) {
          //            tupToCheck.save()
          //            tupToCheck.getIndeces(index)
          //            //        println(index.length)
          //            var setindex: Set[Int] = Set()
          //            var i = 0
          //            while (i < index.length) {
          //
          //              if (!setindex.contains(index(i))) {
          //                setindex += index(i)
          //                val consistent = reviseBlock(sub, tuples(index(i)), evt)
          //                if (!consistent) return false
          //                if (tupToCheck.removeBlock(createBlock(sub, tuples(index(i)))) && i != index.length - 1) {
          //                  i = 0
          //                  tupToCheck.getIndeces(index)
          //                } else {
          //                  i += 1
          //                }
          //              } else {
          //                i += 1
          //              }
          //            }
          //            tupToCheck.restore()
          //          }
          //        }
        }
      }
    }
    for (i <- 0 until arity) {
      val v = scope(i)
      v.mask(PWlastMask(i))
    }
    return true
  }

  def reviseBlock(vars: ArrayBuffer[Int], t: Array[Int], evt: ArrayBuffer[Int]): Boolean = {
    if (living.intersectIndex(createBlock(vars, t)) == -1) {
      for (c <- helper.incidentCons(vars)) {
        if (c != id) {
          //          println(c+":   "+helper.tabs(c).living.numSet())
          var modified = helper.tabs(c).removeBlock(vars, t, scope)
          if (modified) {
            if (helper.tabs(c).living.isEmpty()) {
              return false
            }
            if (!evt.contains(c)) {
              evt += c
            }
          }
        }
      }
    }
    return true
  }

  override def addBlockToMask(vars: ArrayBuffer[Int], t: Array[Int], cscope: Array[Var]): Unit = {
    tupe.drop(scope.length)
    for (x <- vars) {
      for (i <- 0 until cscope.length) {
        if (cscope(i).id == x) {
          for (j <- 0 until scope.length) {
            if (scope(j).id == x) {
              tupe(j) = t(i)
            }
          }
        }
      }
    }
    val b = createBlock(vars, tupe)
    living.addBlockToMask(b)
  }

  override def interesectIndex(vars: ArrayBuffer[Int], t: Array[Int], cscope: Array[Var]): Int = {
    tupe.drop(scope.length)
    for (x <- vars) {
      for (i <- 0 until cscope.length) {
        if (cscope(i).id == x) {
          for (j <- 0 until scope.length) {
            if (scope(j).id == x) {
              tupe(j) = t(i)
            }
          }
        }
      }
    }
    val b = createBlock(vars, tupe)
    return living.intersectIndex(b)
  }

  override def removeBlock(vars: ArrayBuffer[Int], t: Array[Int], cscope: Array[Var]): Boolean = {
    tupe.drop(scope.length)
    for (x <- vars) {
      for (i <- 0 until cscope.length) {
        if (cscope(i).id == x) {
          for (j <- 0 until scope.length) {
            if (scope(j).id == x) {
              tupe(j) = t(i)
            }
          }
        }
      }
    }
    val b = createBlock(vars, tupe)
    return living.removeBlock(b)
  }

  // !!new移除出去
  override def createBlock(vars: ArrayBuffer[Int], t: Array[Int]): Block = {
    b.sets.clear()
    var j = 0;
    ind.clear()
    for (x <- vars) {
      for (i <- 0 until scope.length) {
        if (scope(i).id == x) {
          b.sets += supports(i)(t(i))
          ind += indices(i)(t(i))
          if (ind(j).limit(0) < ind(0).limit(0)) {
            val R = ind(j)
            ind(j) = ind(0)
            ind(0) = R
          }
          j += 1
        }
      }
    }
    b.commonIndices.initIntersection(ind)
    return b
  }

  override def newLevel(): Unit = {
    level += 1
    living.newLevel(level)
  }

  override def backLevel(): Unit = {
    living.deleteLevel(level)
    level -= 1
    var i = 0
    while (i < arity) {
      scope(i).mask(lastMask(i))
      scope(i).mask(PWlastMask(i))
      i += 1
    }
  }

  override def propagateGAC(evt: ArrayBuffer[Var]): (Boolean, Boolean) = {
    //L32~L33
    initial()
    //    val utStart = System.nanoTime
    val res = updateTable()
    //    val utEnd = System.nanoTime
    //    helper.updateTableTime += utEnd - utStart
    if (!res._1) {
      return (false, false)
    }
    //    currTab.show()
    //    val fiStart = System.nanoTime
    val fi = filterDomains(evt)
    //    val fiEnd = System.nanoTime
    //    helper.filterDomainTime += fiEnd - fiStart
    return (fi, res._2)
  }

  override def propagatePWC(xEvt: ArrayBuffer[Var], cEvt: ArrayBuffer[Int]): Boolean = {
    for (i <- 0 until arity) {
      scope(i).mask(localMask(i))
    }
    return enforcePWC(xEvt, cEvt)
  }

  override def clearLivingDelta(): Unit = {
    living.clearDelta()
  }

  override def propagate(evt: ArrayBuffer[Var]): Boolean = ???

  override def stepUp(num_vars: Int): Unit = ???

  override def isEntailed(): Boolean = ???

  override def isSatisfied(): Unit = ???

  override def getTuple(i: Int): Array[Int] = {
    return tuples(i)
  }

  override def getTupleLength(): Int = {
    return tuples.length
  }
}
