package cpscala.TSolver.Experiment

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import cpscala.TSolver.Model.Solver.CPFSolver.{CPFSolverImpl, CPFSolverImpl_with_relation, CPF_MultiThread_S}
import cpscala.TSolver.Model.Solver.PWSolver.PWCoarseSolver
import cpscala.XModel.{XModel, ZModel}
import java.util.Date
import java.text.SimpleDateFormat

import cpscala.TSolver.Experiment.test_ct.{name, pType, varType}
import cpscala.TSolver.Model.Solver.SSolver.SCoarseSolver

import scala.collection.mutable.ArrayBuffer
import scala.util.Sorting
import scala.xml.XML

object CPF_MultiThread_Tester {


  var Time_Limit : Long = 1800000000000L

  def main(args: Array[String]): Unit = {


    //val file = XML.loadFile("benchmarks/Folders" + args(0) + ".xml")
    val file = XML.loadFile("benchmarks/Folders1.xml")
    val inputRoot = (file \\ "inputRoot").text
    val outputRoot = (file \\ "outputRoot").text
    val outputFolder = (file \\ "outputFolder").text
    val inputFolderNodes = file \\ "folder"

    for (fn <- inputFolderNodes) {
      val fmt = (fn \\ "@format").text.toInt
      val folderStr = fn.text
      val inputPath = inputRoot + "/" + folderStr
      val files = getFiles(new File(inputPath))
      Sorting.quickSort(files)
      //println("exp files:")
      // files.foreach(f => println(f.getName))
      val resFile = new File(outputRoot + "/" + outputFolder + folderStr + ".csv")

      val titleLine = ArrayBuffer[String]()

      titleLine ++= Array(
        "id","instance                                           ",
        "algorithm 1","init_time","search_time","nodes","status", //CPF  8
        "algorithm 2","init_time","search_time","nodes","status",  //CPF 16
        "algorithm 3","init_time","search_time","nodes","status",   //CPF 24
        "algorithm 4","init_time","search_time","nodes","status",   //CPF 28
        "date                   ")
      val writer = CSVWriter.open(resFile)
      writer.writeRow(titleLine)
      writer.close()
      var dataLine = new ArrayBuffer[String](titleLine.length)
      var i = 1
      for (f <- files) {
        dataLine += i.toString()
        dataLine += f.toString()

        println(f)

        var xm = new XModel(f.getPath, true, fmt)
        dataLine ++= CPF_Test_N(xm,8)
        dataLine ++= CPF_Test_N(xm,16)
        dataLine ++= CPF_Test_N(xm,24)
        dataLine ++= CPF_Test_N(xm,28)


        dataLine += xm.vars.size().toString()
        dataLine += xm.tabs.size().toString()

        xm = null

        val day=new Date()               //时间戳
        val df = new SimpleDateFormat("MM-dd HH:mm:ss")

        dataLine += df.format(day).toString()

        i += 1
        // println(dataLine)
        val inner_writer = CSVWriter.open(resFile,true)
        inner_writer.writeRow(dataLine)
        inner_writer.close()
        dataLine.clear()
        System.gc()

      }

    }
  }

  def CPF_Test_N(xm : XModel, n : Int): ArrayBuffer[String]  =
    {
      var line  = new ArrayBuffer[String](5)
      val name = "CPF-" + n.toString()
      line += name.toString()
      val init_time_start = System.nanoTime()

      var CMS = new CPF_MultiThread_S(xm,null, null, null)
      val init_time_end = System.nanoTime()
      line += ((init_time_end-init_time_start).toDouble * 1e-9).toString()
      //CMS.Show()
      val search_time_start = System.nanoTime()
      CMS.Solve(n,Time_Limit)
      val search_time_end = System.nanoTime()
      line += ((search_time_end-search_time_start).toDouble * 1e-9).toString()
      line += CMS.node_m.get().toString()
      line += CMS.status.get().toString()
      CMS = null

      return line
    }


  //获取指定单个目录下所有文件
  def getFiles(dir: File): Array[File] = {
    dir.listFiles.filter(_.isFile) ++
      dir.listFiles.filter(_.isDirectory).flatMap(getFiles)
  }

}
