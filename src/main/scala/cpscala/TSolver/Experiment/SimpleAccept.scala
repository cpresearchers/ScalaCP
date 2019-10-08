package cpscala.TSolver.Experiment

import cpscala.TSolver.Model.Solver.CPFSolver.{CPFSolverImpl, CPF_MultiThread_S}
import cpscala.XModel.XModel

import scala.xml.XML

object SimpleAccept {
  def main(args: Array[String]): Unit = {

    val xf = XML.loadFile("benchmarks/BMPath.xml")
    val fileNode = xf \\ "BMFile"
    val path = fileNode.text
    val fmt = (fileNode \\ "@format").text.toInt;
   // println(path)

    val xm = new XModel(path, true, fmt)
    //xm.show()

    val CMS = new CPF_MultiThread_S(xm,null, null, null)
    //CMS.Show()
    CMS.Solve(4,1800000000000L)
    CMS.Answer()
    print(CMS.status.get())
//    val CPF = new CPFSolverImpl(xm,null,null,null)
//        //CMS.Show()
//       CPF.Search(1800000000000L)
//       CPF.Answer()


    //val ad = All_Different()
  }

}
