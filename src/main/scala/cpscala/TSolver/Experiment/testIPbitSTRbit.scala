package cpscala.TSolver.Experiment

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import cpscala.TSolver.CpUtil.Constants
import cpscala.TSolver.Model.Solver.IPplusSolver.{IPbitCoarseSolver, IPbitFineSolver}
import cpscala.TSolver.Model.Solver.SSolver.SFineSolver
import cpscala.XModel.XModel

import scala.collection.mutable.ArrayBuffer
import scala.util.Sorting
import scala.xml.XML

object testIPbitSTRbit {
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
        //-------------STRbit串行算法-------------
        pType = "STRbit_Bit"
        varType = "BitSet"
        heuName = "Dom/Ddeg"
        println(s"${pType} ${heuName}===============>")
        val strbit = new SFineSolver(xm, pType, varType, heuName)
        strbit.search(Constants.TIME)
        dataLine += pType
        dataLine += strbit.helper.nodes.toString()
        dataLine += (strbit.helper.time.toDouble * 1e-9).toString()
        dataLine += (strbit.helper.branchTime.toDouble * 1e-9).toString()
        dataLine += (strbit.helper.propTime.toDouble * 1e-9).toString()
        dataLine += (strbit.helper.backTime.toDouble * 1e-9).toString()
        dataLine += strbit.helper.c_sum.toString()
        //-------------间隔单独提交并行STRbit-------------
        ppType = "IPbitSTRbit_SBit"
        varType = "SafeBitSet"
        heuName = "Dom/Ddeg"
        for (parallelism <- parallelisms) {
          name = ppType + "_" + parallelism.toString()
          println(s"Solving ${name} with ${parallelism} threads===============>")
          val pstrbit = new IPbitFineSolver(xm, parallelism, ppType, varType, heuName)
          pstrbit.search(Constants.TIME)
          pstrbit.shutdown()
          dataLine += name
          dataLine += pstrbit.helper.nodes.toString()
          dataLine += (pstrbit.helper.time.toDouble * 1e-9).toString()
          dataLine += (pstrbit.helper.branchTime.toDouble * 1e-9).toString()
          dataLine += (pstrbit.helper.propTime.toDouble * 1e-9).toString()
          dataLine += (pstrbit.helper.backTime.toDouble * 1e-9).toString()
          dataLine += pstrbit.helper.c_sum.toString()
          dataLine += pstrbit.helper.p_sum.toString()
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
