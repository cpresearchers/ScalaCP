package cpscala.TSolver.Experiment

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import cpscala.TSolver.CPF_Tester.getFiles
import cpscala.XModel.XModel

import scala.collection.mutable.ArrayBuffer
import scala.util.Sorting
import scala.xml.XML

object SeriesReader {

  def main(args: Array[String]): Unit = {
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
      val resFile = new File(outputRoot + "/" + outputFolder + folderStr + ".csv")
      val titleLine = ArrayBuffer[String]()
      titleLine ++= Array(
        "instance                                           ",
        "num_var", "num_constraint", "Looseness", "Tightness",
        "max_arity", "max_domain_size", "max_tuples_size", "avg_tuples_size", "ave_domain_size")
      val writer = CSVWriter.open(resFile)
      writer.writeRow(titleLine)
      writer.close()
      var dataLine = new ArrayBuffer[String](titleLine.length)
      for (f <- files) {
        dataLine += f.getName
        println(f.getName)
        val xm = new XModel(f.getPath, true, fmt)
        dataLine += xm.vars.size().toString()
        dataLine += xm.tabs.size().toString()
        dataLine += xm.Get_Looseness().toString()
        dataLine += xm.Get_Tightness().toString()
        dataLine += xm.max_arity.toString()
        dataLine += xm.max_domain_size.toString()
        dataLine += xm.max_tuples_size.toString()
        dataLine += xm.avg_tuples_size.toString()
        dataLine += xm.Get_Ave_Domain_Size().toString()

        val inner_writer = CSVWriter.open(resFile, true)
        inner_writer.writeRow(dataLine)
        inner_writer.close()
        dataLine.clear()
        System.gc()
      }

    }
  }

}
