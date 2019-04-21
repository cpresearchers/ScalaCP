//package cpscala.TSolver.Experiment
//
//import java.io.File
//
//import com.github.tototoshi.csv.CSVWriter
//import cpscala.XModel.XModel
//
//import scala.xml.XML
//
//object test_ct {
//
//  def main(args: Array[String]): Unit = {
//
//    if (args.isEmpty)
//      argEmpty()
//    else
//      withArgs(args)
//  }
//
//  def argEmpty(): Unit = {
//    val file = XML.loadFile("benchmarks/Folders.xml")
//    val inputRoot = (file \\ "inputRoot").text
//    val outputRoot = (file \\ "outputRoot").text
//    val outputFolder = (file \\ "outputFolder").text
//    val inputFolderNodes = file \\ "folder"
//
//    for (fn <- inputFolderNodes) {
//      val fmt = (fn \\ "@format").text.toInt
//      val folderStr = fn.text
//      val inputPath = inputRoot + "/" + folderStr
//      val files = getFiles(new File(inputPath))
//
//      val resFile = new File(outputRoot + "/" + outputFolder + "/CT/" + folderStr + ".csv")
//      val writer = CSVWriter.open(resFile)
//      writer.writeRow(List("name", "time", "nodes", "mds", "ma", "mts", "ats"))
//      for (f <- files) {
//        println("Build Model: " + f.getName)
//        val xm = new XModel(f.getPath, true, fmt)
//        println("Solve model!")
//
//        val ps = new PSolver2(xm)
//        ps.search()
//        val name = ps.name
//        val time = String.valueOf(ps.search_time.toDouble * 1e-6)
//        val nodes = String.valueOf(ps.nodes)
//        val mds = String.valueOf(ps.mds)
//        val ma = String.valueOf(ps.ma)
//        val mts = String.valueOf(ps.mts)
//        val ats = String.valueOf(ps.ats)
//
//        writer.writeRow(List(name, time, nodes, mds, ma, mts, ats))
//      }
//      writer.close()
//      println("-----" + folderStr + " done!-----")
//    }
//    println("-----All done!-----")
//  }
//
//  def withArgs(args: Array[String]): Unit = {
//
//  }
//
//  //获取指定单个目录下所有文件
//  def getFiles(dir: File): Array[File] = {
//    dir.listFiles.filter(_.isFile) ++
//      dir.listFiles.filter(_.isDirectory).flatMap(getFiles)
//  }
//
//}
