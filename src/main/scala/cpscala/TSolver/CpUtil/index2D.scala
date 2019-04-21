package cpscala.TSolver.CpUtil

class index2D(ix: Int, iy: Int) {
  var x = ix
  var y = iy

  def this(index: Int) {
    this(0, 0)
    x = index >> Constants.DIVBIT
    y = index & Constants.MODMASK
  }

  def getValue(): Int = (x << Constants.DIVBIT) + y
}
