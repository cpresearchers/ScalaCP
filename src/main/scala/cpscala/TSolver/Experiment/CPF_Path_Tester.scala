
package cpscala.TSolver


import java.io.File

import com.github.tototoshi.csv.CSVWriter
import cpscala.TSolver.Model.Solver.CPFSolver.CPF_Path_Imp
import cpscala.TSolver.Model.Solver.PWSolver.PWCoarseSolver
import cpscala.XModel.XModel
import java.util.Date
import java.text.SimpleDateFormat

import scala.collection.mutable.ArrayBuffer
import scala.util.Sorting
import scala.xml.XML

object CPF_Path_Tester {


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
        "path_size","path_rate", //CPF

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



        var xm = new XModel(f.getPath, true, fmt)
        var cpf  = new CPF_Path_Imp(xm)

        dataLine += cpf.Get_P().size().toString()
        dataLine += cpf.Get_Rate().toString()



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

  //获取指定单个目录下所有文件
  def getFiles(dir: File): Array[File] = {
    dir.listFiles.filter(_.isFile) ++
      dir.listFiles.filter(_.isDirectory).flatMap(getFiles)
  }

  }
