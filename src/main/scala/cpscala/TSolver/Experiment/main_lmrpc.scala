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

    //    xm.show()

    val lmx = new LMaxRPCSolver(xm,"b")
    lmx.search(Constants.TIME)

    println(lmx.I.toArray().mkString(","))
    val sol = lmx.I.toArray()
//    sol(1) = 0
    println(xm.check(sol))
  }

}
