package cpscala.TSolver.CpUtil.SearchHelper

import java.util.concurrent.{ForkJoinPool, TimeUnit}
import java.util.concurrent.atomic.AtomicLong

class SearchHelper(val numVars: Int, val numTabs: Int) {
  // 初始化各时间戳
  var globalStamp: Long = 0L
  val tabStamp: Array[Long] = Array.fill(numTabs)(0L)
  val varStamp: Array[Long] = Array.fill(numVars)(0L)
  //  val sizeLevel: Array[Int] = Array.fill(numLevel)(0)

  // 搜索时间
  var time: Long = 0L
  var branchTime = 0L
  var propTime = 0L
  var initialTime = 0L
  var updateTableTime = 0L
  var filterDomainTime = 0L
  var backTime = 0L
  var joinTime = 0L
  var lockTime = new AtomicLong(0L)
  var inconsistentTime = 0L
  var stopTime = 0L
//  var lockTime = 0L
  var nodes: Long = 0L
//  @volatile var isConsistent: Boolean = true
  var isConsistent: Boolean = true
  // 搜索上限
  var timeLimit = 0L
  var nodeLimit = 0L
  var failureLimit = 0L

  var level: Int = 0

  //线程启动次数
  var p_sum = 0L
  //约束传播次数
  var c_sum = 0L
  //不变约束
  var notChangedTabs = new AtomicLong(0L)


}
