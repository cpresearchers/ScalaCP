package cpscala.TSolver

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import cpscala.TSolver.Model.Solver.CPFSolver.{CPFSolverImpl, CPFSolverImpl_with_relation}
import cpscala.TSolver.Model.Solver.PWSolver.PWCoarseSolver
import cpscala.XModel.{XModel, ZModel}
import java.util.Date
import java.text.SimpleDateFormat

import cpscala.TSolver.Experiment.test_ct.{name, pType, varType}
import cpscala.TSolver.Model.Solver.SSolver.SCoarseSolver

import scala.collection.mutable.ArrayBuffer
import scala.util.Sorting
import scala.xml.XML

object CPF_Tester {


  var Time_Limit : Long = 1800000000000L

  def main(args: Array[String]): Unit = {


   // val file = XML.loadFile("benchmarks/Folders" + args(0) + ".xml")
    val file = XML.loadFile("benchmarks/Folders" + ".xml")
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
        "algorithm 1","init_time","search_time","nodes","answer", //CPF
        "algorithm 2","init_time","search_time","nodes","answer",  //CT dom/ddeg
        "algorithm 3","init_time","search_time","nodes","answer",   //CT dom/wdeg
        "algorithm 4","init_time","search_time","nodes","answer",   //PWCT
        "num_var","num_constraint","Looseness","Tightness",
        "max_arity","max_domain_size","max_tuples_size","avg_tuples_size","ave_domain_size","date")
      val writer = CSVWriter.open(resFile)
      writer.writeRow(titleLine)
      writer.close()
      var dataLine = new ArrayBuffer[String](titleLine.length)
      var i = 1
      for (f <- files) {
        dataLine += i.toString()
        dataLine += f.toString()

        println(f)


        var zm = new ZModel(f.getPath, true, fmt)
        dataLine ++= CPF_Test(zm)
        zm = null
        var xm = new XModel(f.getPath, true, fmt)
        dataLine ++= CT_Test_Wdeg(xm)
        dataLine ++= CT_Test_Ddeg(xm)

        dataLine ++= PW_CT_Test(xm)


        dataLine += xm.vars.size().toString()
        dataLine += xm.tabs.size().toString()
        dataLine += xm.Get_Looseness().toString()
        dataLine += xm.Get_Tightness().toString()
        dataLine += xm.max_arity.toString()
        dataLine += xm.max_domain_size.toString()
        dataLine += xm.max_tuples_size.toString()
        dataLine += xm.avg_tuples_size.toString()
        dataLine += xm.Get_Ave_Domain_Size().toString()

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




  def CT_Test_Wdeg(hm : XModel ): ArrayBuffer[String] =
  {

    var line  = new ArrayBuffer[String](5)

    val name:String = "CT_Test_Wdeg"
    line += name.toString()
    val init_time_start = System.nanoTime()
    var ct = new SCoarseSolver(hm, "CT_Bit", "BitSet", "Dom/Wdeg")
    val init_time_end = System.nanoTime()
    line += ((init_time_end-init_time_start).toDouble * 1e-9).toString()
    val search_time_start = System.nanoTime()
    ct.search(Time_Limit)
    val search_time_end = System.nanoTime()
    line += ((search_time_end-search_time_start).toDouble * 1e-9).toString()
    line += ct.helper.nodes.toString()
    line += "-".toString()
    ct = null
    return line




  }


  def CT_Test_Ddeg(hm : XModel ): ArrayBuffer[String]  =
  {



    var line  = new ArrayBuffer[String](5)

    val name:String = "CT_Test_Ddeg"
    line += name.toString()
    val init_time_start = System.nanoTime()
    var ct = new SCoarseSolver(hm, "CT_Bit", "BitSet", "Dom/Ddeg")
    val init_time_end = System.nanoTime()
    line += ((init_time_end-init_time_start).toDouble * 1e-9).toString()
    val search_time_start = System.nanoTime()
    ct.search(Time_Limit)
    val search_time_end = System.nanoTime()
    line += ((search_time_end-search_time_start).toDouble * 1e-9).toString()
    line += ct.helper.nodes.toString()
    line += "-".toString()
    ct = null
    return line




  }


  def CPF_Test(zm : ZModel): ArrayBuffer[String]  =
  {

    var line  = new ArrayBuffer[String](5)
    val name = "CPF"
    line += name.toString()
    val init_time_start = System.nanoTime()

    var CPF = new CPFSolverImpl_with_relation(zm, null, null, null)

    val init_time_end = System.nanoTime()
    line += ((init_time_end-init_time_start).toDouble * 1e-9).toString()
    val search_time_start = System.nanoTime()
    val ans = CPF.Search(Time_Limit)
    val search_time_end = System.nanoTime()
    line += ((search_time_end-search_time_start).toDouble * 1e-9).toString()
    line += CPF.Get_Node().toString()
    line += ans.toString()
    CPF = null
    return line

  }


  def PW_CT_Test(hm : XModel): ArrayBuffer[String]  =
  {

    var line  = new ArrayBuffer[String](5)
//
//    val name:String = "PW-CT"
//    line += name.toString()
//    val init_time_start = System.nanoTime()
//
//    var ct = new PWCoarseSolver(hm, "PW-CT", "BitSet", "Dom/Wdeg")
//
//    val init_time_end = System.nanoTime()
//    line += ((init_time_end-init_time_start).toDouble * 1e-9).toString()
//    val search_time_start = System.nanoTime()
//    ct.search(Time_Limit)
//    val search_time_end = System.nanoTime()
//    line += ((search_time_end-search_time_start).toDouble * 1e-9).toString()
//    line += ct.helper.nodes.toString()
//    line += "-".toString()
//    ct = null
    line += "a"
    line += "b"
    line += "c"
    line += "d"
    line += "e"

    return line





  }


  //获取指定单个目录下所有文件
  def getFiles(dir: File): Array[File] = {
    dir.listFiles.filter(_.isFile) ++
      dir.listFiles.filter(_.isDirectory).flatMap(getFiles)
  }

}
