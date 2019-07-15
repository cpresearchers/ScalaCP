package cpscala.TSolver.Experiment

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.Model.Solver.IPplusSolver.IPbitCoarseSolver
import cpscala.TSolver.Model.Solver.SSolver.SCoarseSolver
import cpscala.XModel.XModel

import scala.collection.mutable.ArrayBuffer
import scala.util.Sorting
import scala.xml.XML

object testIPbitCT {
  var name = ""
  var pType = ""
  var ppType = ""
  var varType = ""
  var heuName = ""

  val parallelisms = Array[Int](1, 2, 3, 4, 5)

  def main(args: Array[String]): Unit = {

    if (args.isEmpty)
      argEmpty()
    else
      withArgs(args)
  }

  def argEmpty(): Unit = {
    println(s"hardware cocurrency: ${Runtime.getRuntime.availableProcessors()}")
    val file = XML.loadFile("benchmarks/IPFolder.xml")
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
      println("exp files:")
      files.foreach(f => println(f.getName))
      val resFile = new File(outputRoot + "/" + outputFolder + folderStr + ".csv")
      val writer = CSVWriter.open(resFile)
      val titleLine = ArrayBuffer[String]()
      titleLine += "name"
      titleLine ++= Array("algorithm", "nodes", "time", "branchTime", "propTime", "backTime", "c_sum")
      for (_ <- 0 until parallelisms.length) {
        titleLine ++= Array("algorithm", "nodes", "time", "branchTime", "propTime", "backTime", "c_sum", "p_sum")
      }

      writer.writeRow(titleLine)
      var dataLine = new ArrayBuffer[String](titleLine.length)

      for (f <- files) {
        println("Build Model: " + f.getName)
        val xm = new XModel(f.getPath, true, fmt)
        dataLine.clear()
        dataLine += f.getName()
        //-------------CT串行算法-------------
        pType = "CT_Bit"
        varType = "BitSet"
        heuName = "Dom/Ddeg"
        println(s"${pType} ${heuName}===============>")
        val ct = new SCoarseSolver(xm, pType, varType, heuName)
        ct.search(Constants.TIME)
        dataLine += pType
        dataLine += ct.helper.nodes.toString()
        dataLine += (ct.helper.time.toDouble * 1e-9).toString()
        dataLine += (ct.helper.branchTime.toDouble * 1e-9).toString()
        dataLine += (ct.helper.propTime.toDouble * 1e-9).toString()
        dataLine += (ct.helper.backTime.toDouble * 1e-9).toString()
        dataLine += ct.helper.c_sum.toString()
        //-------------间隔单独提交并行CT-------------
        ppType = "IPbitCT_SBit"
        varType = "SafeBitSet"
        heuName = "Dom/Ddeg"
        for (parallelism <- parallelisms) {
          name = ppType + "_" + parallelism.toString()
          println(s"Solving ${name} with ${parallelism} threads===============>")
          val pct = new IPbitCoarseSolver(xm, parallelism, ppType, varType, heuName)
          pct.search(Constants.TIME)
          pct.shutdown()
          dataLine += name
          dataLine += pct.helper.nodes.toString()
          dataLine += (pct.helper.time.toDouble * 1e-9).toString()
          dataLine += (pct.helper.branchTime.toDouble * 1e-9).toString()
          dataLine += (pct.helper.propTime.toDouble * 1e-9).toString()
          dataLine += (pct.helper.backTime.toDouble * 1e-9).toString()
          dataLine += pct.helper.c_sum.toString()
          dataLine += pct.helper.p_sum.toString()
        }
        writer.writeRow(dataLine)
        println("end: " + f.getName)
      }
      writer.close()
      println("-----" + folderStr + " done!-----")
    }
    println("-----All done!-----")
  }

  def withArgs(args: Array[String]): Unit = {

  }

  //获取指定单个目录下所有文件
  def getFiles(dir: File): Array[File] = {
    dir.listFiles.filter(_.isFile) ++
      dir.listFiles.filter(_.isDirectory).flatMap(getFiles)
  }

}
