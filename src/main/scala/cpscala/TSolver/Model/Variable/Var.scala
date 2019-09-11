package cpscala.TSolver.Model.Variable

import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper

import scala.collection.mutable.ArrayBuffer

abstract class Var {
  val id: Int
  var stamp: Long = 0L
  val helper: SearchHelper
  val capacity = 0
  // !!尚未全部改动
  var bindLevel = Constants.kINTINF

  var level = 0

  var activity = 0L

  def newLevel(): Int

  def backLevel(): Int

  def size(): Int

  // 获取第index个取值
  def get(index: Int): Int

  def getNumBit(): Int = ???

  def bind(a: Int)

  def isBind(): Boolean = bindLevel <= level

  def unBind(): Boolean = bindLevel > level

  def isLastBind(): Boolean = bindLevel == level

  def isLastBindOrUnBind(): Boolean = bindLevel >= level

  def remove(a: Int)

  def isEmpty(): Boolean

  def restrict()

  def clearMark()

  def mark(a: Int): Unit

  def mask(m: Array[Long]): Unit

  def fullMark(): Boolean

  def contains(a: Int): Boolean

  def minValue(): Int

  def nextValue(a: Int): Int

  def lastValue(): Int

  def preValue(a: Int): Int

  def maxValue(a: Int): Int

  def getLastRemovedValues(oldSize: Long, vals: ArrayBuffer[Int]): Int = ???

  def getValidValues(vals: ArrayBuffer[Int]): Int = ???

  def show(): Unit = ???

  def removeValues(word:Array[Long]):Boolean = ???

  def getBitDom():Array[Long] = ???
}
