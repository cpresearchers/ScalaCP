package cpscala.TSolver.Experiment

import java.io.File

import com.github.tototoshi.csv.CSVWriter

import cpscala.XModel.XModel

import scala.collection.mutable.ArrayBuffer
import scala.util.Sorting
import scala.xml.XML

class CPF_Test {

  def main(args: Array[String]): Unit = {

    val file = XML.loadFile("benchmarks/Folders.xml")
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
      titleLine ++= Array("algorithm", "nodes", "time", "branchTime", "propTime", "backTime", "c_sum", "p_sum")


      writer.writeRow(titleLine)
      var dataLine = new ArrayBuffer[String](titleLine.length)

      for (f <- files) {
        println(f)
        val xm = new XModel(f.getPath, true, fmt)

      }

    }
  }

    //获取指定单个目录下所有文件
    def getFiles(dir: File): Array[File] = {
      dir.listFiles.filter(_.isFile) ++
        dir.listFiles.filter(_.isDirectory).flatMap(getFiles)
    }




}
