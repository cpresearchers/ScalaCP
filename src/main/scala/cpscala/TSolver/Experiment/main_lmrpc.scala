package cpscala.TSolver.Experiment

import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.Model.Solver.Others.LMaxRPCSolver
import cpscala.TSolver.Model.Solver.SSolver.SCoarseSolver
import cpscala.XModel.XModel

import scala.xml.XML

object main_lmrpc {

  def main(args: Array[String]): Unit = {
    val xf = XML.loadFile("benchmarks/BMPath.xml")
    val fileNode = xf \\ "BMFile"
    val path = fileNode.text
    val fmt = (fileNode \\ "@format").text.toInt
    println(path)
    val xm = new XModel(path, true, fmt)

    xm.show()

    val ct = new LMaxRPCSolver(xm)
    ct.search(Constants.TIME)
  }

}
