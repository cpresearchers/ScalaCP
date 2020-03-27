package cpscala.TSolver.CpUtil

object INDEX {
  @inline def getIndex2D(index: Int): index2D = {
    return new index2D(index)
  }

  @inline def getXY(index: Int): (Int, Int) = {
    return (index >> Constants.DIVBIT, index & Constants.MODMASK)
  }


  @inline def getIndex(x: Int, y: Int): Int = {
    (x << Constants.DIVBIT) + y
  }

  @inline def getIndex(in2D: index2D): Int = {
    (in2D.x << Constants.DIVBIT) + in2D.y
  }

  val overflow = -1
}
