package cpscala.TSolver.Experiment

import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.Model.Solver.Others.{LMXPSolver, LMaxRPCSolver}
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


    //    val ct = new SCoarseSolver(xm, "CT_Bit", "BitSet", "")
    //    ct.search(Constants.TIME)
    //
    //
    //    val lmx = new LMaxRPCSolver(xm, "b")
    //    lmx.search(Constants.TIME)
    //
    //    println(lmx.I.toArray().mkString(","))
    //    val sol = lmx.I.toArray()
    //    //    sol(1) = 0
    //    println(xm.check(sol))


    //    val lmx2 = new LMXPSolver(xm, 4)
    //    lmx2.sync(Constants.TIME)
    //    val sol2 = lmx2.I.toArray()
    //    println(xm.check(sol2))
    //    println(lmx2.helper.time * 1e-9)

//    var ii = 0
//    while (ii < 100) {
      val lmx3 = new LMXPSolver(xm, 3)
      lmx3.async(Constants.TIME)
      if (!lmx3.helper.timeout && lmx3.helper.hasSolution) {
        val sol3 = lmx3.I.toArray()
        println(lmx3.helper.nodes)
        println(lmx3.helper.time * 1e-9)
        println(xm.check(sol3))
      }

    val lmx4 = new LMXPSolver(xm, 3)
    lmx4.hyper(Constants.TIME)
    if (!lmx4.helper.timeout && lmx4.helper.hasSolution) {
      val sol3 = lmx4.I.toArray()
      println(lmx4.helper.nodes)
      println(lmx4.helper.time * 1e-9)
      println(xm.check(sol3))
    }
//    }
//    ii += 1
  }


}
