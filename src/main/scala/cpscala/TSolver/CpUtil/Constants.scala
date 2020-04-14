package cpscala.TSolver.CpUtil

import scala.collection.mutable.ArrayBuffer

object Constants {
  val BITSIZE: Int = 64
  val DIVBIT = 6
  val MODMASK = 0x3f
  @inline val INDEXOVERFLOW: Int = -1
  val kINDEXOVERFLOW: Int = -1
  val ALLONELONG: Long = 0xFFFFFFFFFFFFFFFFL
  val kINTMAXINF: Int = 0x3f3f3f3f
  val kINTMININF: Int = -0x3f3f3f3f
  val kLONGINF: Long = 0x3f3f3f3f3f3f3f3fL

  //高位的index为0
  val MASK1 = Array(0x8000000000000000L, 0x4000000000000000L, 0x2000000000000000L, 0x1000000000000000L, 0x0800000000000000L, 0x0400000000000000L, 0x0200000000000000L, 0x0100000000000000L, 0x0080000000000000L, 0x0040000000000000L, 0x0020000000000000L, 0x0010000000000000L, 0x0008000000000000L, 0x0004000000000000L, 0x0002000000000000L, 0x0001000000000000L, 0x0000800000000000L, 0x0000400000000000L, 0x0000200000000000L, 0x0000100000000000L, 0x0000080000000000L, 0x0000040000000000L, 0x0000020000000000L, 0x0000010000000000L, 0x0000008000000000L, 0x0000004000000000L, 0x0000002000000000L, 0x0000001000000000L, 0x0000000800000000L, 0x0000000400000000L, 0x0000000200000000L, 0x0000000100000000L, 0x0000000080000000L, 0x0000000040000000L, 0x0000000020000000L, 0x0000000010000000L, 0x0000000008000000L, 0x0000000004000000L, 0x0000000002000000L, 0x0000000001000000L, 0x0000000000800000L, 0x0000000000400000L, 0x0000000000200000L, 0x0000000000100000L, 0x0000000000080000L, 0x0000000000040000L, 0x0000000000020000L, 0x0000000000010000L, 0x0000000000008000L, 0x0000000000004000L, 0x0000000000002000L, 0x0000000000001000L, 0x0000000000000800L, 0x0000000000000400L, 0x0000000000000200L, 0x0000000000000100L, 0x0000000000000080L, 0x0000000000000040L, 0x0000000000000020L, 0x0000000000000010L, 0x0000000000000008L, 0x0000000000000004L, 0x0000000000000002L, 0x0000000000000001L)

  val MASK0 = Array(0x7FFFFFFFFFFFFFFFL, 0xBFFFFFFFFFFFFFFFL, 0xDFFFFFFFFFFFFFFFL, 0xEFFFFFFFFFFFFFFFL, 0xF7FFFFFFFFFFFFFFL, 0xFBFFFFFFFFFFFFFFL, 0xFDFFFFFFFFFFFFFFL, 0xFEFFFFFFFFFFFFFFL, 0xFF7FFFFFFFFFFFFFL, 0xFFBFFFFFFFFFFFFFL, 0xFFDFFFFFFFFFFFFFL, 0xFFEFFFFFFFFFFFFFL, 0xFFF7FFFFFFFFFFFFL, 0xFFFBFFFFFFFFFFFFL, 0xFFFDFFFFFFFFFFFFL, 0xFFFEFFFFFFFFFFFFL, 0xFFFF7FFFFFFFFFFFL, 0xFFFFBFFFFFFFFFFFL, 0xFFFFDFFFFFFFFFFFL, 0xFFFFEFFFFFFFFFFFL, 0xFFFFF7FFFFFFFFFFL, 0xFFFFFBFFFFFFFFFFL, 0xFFFFFDFFFFFFFFFFL, 0xFFFFFEFFFFFFFFFFL, 0xFFFFFF7FFFFFFFFFL, 0xFFFFFFBFFFFFFFFFL, 0xFFFFFFDFFFFFFFFFL, 0xFFFFFFEFFFFFFFFFL, 0xFFFFFFF7FFFFFFFFL, 0xFFFFFFFBFFFFFFFFL, 0xFFFFFFFDFFFFFFFFL, 0xFFFFFFFEFFFFFFFFL, 0xFFFFFFFF7FFFFFFFL, 0xFFFFFFFFBFFFFFFFL, 0xFFFFFFFFDFFFFFFFL, 0xFFFFFFFFEFFFFFFFL, 0xFFFFFFFFF7FFFFFFL, 0xFFFFFFFFFBFFFFFFL, 0xFFFFFFFFFDFFFFFFL, 0xFFFFFFFFFEFFFFFFL, 0xFFFFFFFFFF7FFFFFL, 0xFFFFFFFFFFBFFFFFL, 0xFFFFFFFFFFDFFFFFL, 0xFFFFFFFFFFEFFFFFL, 0xFFFFFFFFFFF7FFFFL, 0xFFFFFFFFFFFBFFFFL, 0xFFFFFFFFFFFDFFFFL, 0xFFFFFFFFFFFEFFFFL, 0xFFFFFFFFFFFF7FFFL, 0xFFFFFFFFFFFFBFFFL, 0xFFFFFFFFFFFFDFFFL, 0xFFFFFFFFFFFFEFFFL, 0xFFFFFFFFFFFFF7FFL, 0xFFFFFFFFFFFFFBFFL, 0xFFFFFFFFFFFFFDFFL, 0xFFFFFFFFFFFFFEFFL, 0xFFFFFFFFFFFFFF7FL, 0xFFFFFFFFFFFFFFBFL, 0xFFFFFFFFFFFFFFDFL, 0xFFFFFFFFFFFFFFEFL, 0xFFFFFFFFFFFFFFF7L, 0xFFFFFFFFFFFFFFFBL, 0xFFFFFFFFFFFFFFFDL, 0xFFFFFFFFFFFFFFFEL)

  val pos = Array(0, 1, 28, 2, 29, 14, 24, 3,
    30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19,
    16, 7, 26, 12, 18, 6, 11, 5, 10, 9)

  val TIME: Long = 1800000000000L
  //  val TIME: Long = 900000000000L

  val WARMUP: Long = 30000000000L

  def field_set(input: Long): Int = {
    val field = input * 0x20406080a0c0e1L
    return ((field >> 60) & 15L).toInt
  }

  def FindLeftOld(a: Int): Int = {
    var v = a; // store the total here
    v = v - ((v >> 1) & 0x55555555); // reuse input as temporary
    v = (v & 0x33333333) + ((v >> 2) & 0x33333333); // temp
    return ((v + (v >> 4) & 0xF0F0F0F) * 0x1010101) >> 24; // count
  }

  def FirstLeftOld(a: Long): Int = {
    var i = a
    i |= i >> 1
    i |= i >> 2
    i |= i >> 4
    i |= i >> 8
    i |= i >> 16
    i |= i >> 32

    i = i - ((i >>> 1) & 0x5555555555555555L)
    i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L)
    i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL
    i = i + (i >>> 8)
    i = i + (i >>> 16)
    i = i + (i >>> 32)

    //得到位数,如果为64则表示全0
    return BITSIZE - (i & 0x7f).toInt // Increment n by 1 so that
  }

  def FirstRightOld(UseMask: Long): Int = {
    var index = UseMask
    //将第一个为1位的低位都置1，其它位都置0
    index = (index - 1) & ~index
    //得到有多少为1的位
    index = (index & 0x5555555555555555L) + ((index >> 1) & 0x5555555555555555L)
    index = (index & 0x3333333333333333L) + ((index >> 2) & 0x3333333333333333L)
    index = (index & 0x0F0F0F0F0F0F0F0FL) + ((index >> 4) & 0x0F0F0F0F0F0F0F0FL)
    index = (index & 0x00FF00FF00FF00FFL) + ((index >> 8) & 0x00FF00FF00FF00FFL)
    index = (index & 0x0000ffff0000ffffL) + ((index >> 16) & 0x0000ffff0000ffffL)
    index = (index & 0xFFFFFFFF) + ((index & 0xFFFFFFFF00000000L) >> 32)
    //得到位数,如果为-1则表示全0
    return BITSIZE - index.toInt - 1
  }

  @inline def FirstLeft(i: Long): Int = {
    if (i == 0) return INDEXOVERFLOW
    java.lang.Long.numberOfLeadingZeros(i)
  }

  @inline def FirstRight(i: Long): Int = {
    //    if (i == 0) return INDEXOVERFLOW
    63 - java.lang.Long.numberOfTrailingZeros(i)
  }

  @inline def toFormatBinaryString(mask: Long): String = {
    val binaryMask = mask.toBinaryString
    val binaryLength = binaryMask.length
    val highBit = "0" * (64 - binaryLength)
    return highBit + binaryMask
  }

  //通过mask表示获取值存入values
  @inline def getValues(b: Array[Long], values: ArrayBuffer[Int]): Unit = {
    values.clear()
    var i = 0
    var base = 0
    var j = 0
    var end = 0

    while (i < b.length) {
      val a = b(i)
      base = i * BITSIZE
      if (a != 0) {
        j = FirstLeft(a)
        end = FirstRight(a)
        while (j <= end) {
          if ((a & MASK1(j)) != 0) {
            values += (j + base)
          }
          j += 1
        }
      }
      i += 1
    }
  }

  //通过mask表示获取值存入values
  @inline def addValues(a: Long, base: Int, values: ArrayBuffer[Int]): Unit = {
    if (a != 0L) {
      var i = FirstLeft(a)
      val end = FirstRight(a)
      while (i <= end) {
        if ((a & MASK1(i)) != 0) {
          values += (i + base)
        }
        i += 1
      }
    }
  }


  //  // 记录最大最小值
  //  val IntMax = 0x3f3f3f3f
  //  val IntMin = -IntMax


  // 分别记录保留值，删除值，未记录值
  object ValType extends Enumeration {
    type ExecutorState = Value
    val Reserve, Delete, Unknown = Value
  }

  // 十亿级前缀标识常量
  // 30位值部，2位标识部
  val KVALUEPARTBITLENGTH = 30
  val kPERFIXMASK: Int = 0x40000000 // 十亿级
  val kSUFFIXMASK: Int = 0x3fffffff // 十亿级

  // 给数据添加前缀
  def markValue(a: Int) = a | kPERFIXMASK

  // 去掉数据据前缀
  def demarkValue(a: Int) = a & kSUFFIXMASK

  // 解析数据
  def resolveMark(a: Int) = (a >> 30, a & kSUFFIXMASK)

  // 解析数据
  def resolveBoolean(a: Int) = (a >= kPERFIXMASK, a & kSUFFIXMASK)


  // 原子版本
}
