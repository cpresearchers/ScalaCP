package cpscala.TSolver.CpUtil

import scala.collection.mutable.ArrayBuffer

class MyBitSet(inlength: Int) {
  var length = inlength
  var num_bit = Math.ceil(length.toDouble / Constants.BITSIZE.asInstanceOf[Double]).toInt
  val word = Array.fill[Long](num_bit)(0L)

  def set(): Unit = {
    var i = 0
    while (i < word.length) {
      word(i) = Constants.ALLONELONG
      i += 1
    }
  }

  def reset(): Unit = {
    var i = 0
    while (i < word.length) {
      word(i) = 0L
      i += 1
    }
  }

  def set(index: Int): Unit = {
    val idx2 = new index2D(index)
    word(idx2.x) |= Constants.MASK1(idx2.y)
  }

  def reset(index: Int): Unit = {
    val idx2 = new index2D(index)
    word(idx2.x) &= Constants.MASK0(idx2.y)
  }

  def test(index: Int): Boolean = {
    val idx2 = new index2D(index)
    (word(idx2.x) & Constants.MASK1(idx2.y)) != 0L
  }

  def commons(myBitSet: MyBitSet): ArrayBuffer[Int] = {
    val min_bit = math.min(num_bit, myBitSet.num_bit);
    val common = new ArrayBuffer[Int]()

    var i = 0
    while (i < min_bit) {
      val w = word(i) & myBitSet.word(i)
      if (w != 0) {
        //说明有交集,64个bit位置挨个查找
        var j = 0
        while (j < Constants.BITSIZE) {
          if ((w & Constants.MASK1(j)) != 0L) {
            common += INDEX.getIndex(i, j)
          }
          j += 1
        }
      }
      i += 1
    }

    return common
  }

}
