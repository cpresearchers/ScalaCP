package cpscala.XModel

import java.io.File

import cpscala.JModel.JReader

import scala.xml.XML
import com.github.tototoshi.csv._

object JFileConverter {

  def main(args: Array[String]): Unit = {
    //读XML
    val file = XML.loadFile("benchmarks/convert.xml")
    val inputRoot = (file \\ "inputRoot").text
    val outputRoot = (file \\ "outputRoot").text
    val outputFolder = (file \\ "outputFolder").text
    if (args.isEmpty) {
      val inputFolderNodes = file \\ "inputFolder"
      //  //例子格式输出
      //  val infoPath = (file \\ "information").text
      //  val writer = CSVWriter.open(new File(infoPath))

      for (fn <- inputFolderNodes) {
        val fmt = (fn \\ "@format").text.toInt;
        val folderStr = fn.text
        val inputPath = inputRoot + "/" + folderStr;
        val of = new File(outputRoot + "/" + outputFolder + "/" + folderStr);
        if (!of.exists()) of.mkdir()
        val files = getFiles(new File(inputPath));

        for (f <- files) {
          println("Reading Model: " + f.getName)
          val xm = new XModel(f.getPath(), true, fmt)
          println("Building model!")
          JConverter.OriToJModel(xm);
          println("Writing Model!")
          JConverter.toJsonFile(of.getPath())
//          fdem.toJsonFile(of.getPath)


          //          println("Reading Model: " + f.getName)
          //          val fdem = new FDEModel(f.getPath(), fmt);
          //          println("Building model!")
          //          fdem.buildJModel();
          //          println("Writing Model!")
          //          fdem.toJsonFile(of.getPath)
        }
        println("-----" + folderStr + " done!-----")
      }
      println("-----All done!-----")
    } else {
      val folderStr = args(0)
      val fmt = args(1).toInt
      val inputPath = inputRoot + "/" + folderStr;
      val of = new File(outputRoot + "/" + outputFolder + "/" + folderStr);
      if (!of.exists()) of.mkdir()
      val files = getFiles(new File(inputPath));

      for (f <- files) {
        println("Reading Model: " + f.getName)
        val fdem = new FDEModel(f.getPath(), fmt);
        println("Building model!")
        fdem.buildJModel();
        println("Writing Model!")
        fdem.toJsonFile(of.getPath)
      }
      println("-----" + folderStr + " done!-----")
    }
  }

  //获取指定单个目录下所有文件
  def getFiles(dir: File): Array[File] = {
    dir.listFiles.filter(_.isFile) ++
      dir.listFiles.filter(_.isDirectory).flatMap(getFiles)
  }
}


