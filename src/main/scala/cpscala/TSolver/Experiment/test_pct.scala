//package cpscala.TSolver.Experiment
//
//import java.io.File
//
//import com.github.tototoshi.csv.CSVWriter
//import cpscala.XModel.XModel
//
//import scala.xml.XML
//
//object test_pct {
//  val TIME: Long = 1800000000000L
//  val WARMUP: Long = 30000000000L
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
//      val fmt = (fn \\ "@format").text.toInt;
//      val folderStr = fn.text
//      val inputPath = inputRoot + "/" + folderStr;
//      val files = getFiles(new File(inputPath));
//
//      val resFile = new File(outputRoot + "/" + outputFolder + "/PCT/" + folderStr + "_-1" + ".csv")
//      val writer = CSVWriter.open(resFile)
//      writer.writeRow(List("name", "time", "nodes", "mds", "ma", "mts", "ats", "prlm"))
//
//      println("Warming Up");
//      {
//        val xm = new XModel(files(0).getPath, true, fmt)
//        val ps = new FJSolver5(xm, WARMUP, -1)
//        ps.search()
//        ps.shutdown()
//      }
//
//      for (f <- files) {
//        println("Build Model: " + f.getName)
//        val xm = new XModel(f.getPath, true, fmt)
//        println("Solve model!")
//
//        {
//          val ps = new FJSolver5(xm, TIME, -1)
//          ps.search()
//          ps.shutdown()
//        }
//
//        {
//          val ps = new FJSolver5(xm, TIME, -1)
//          ps.search()
//          ps.shutdown()
//          val name = ps.name
//          val time = String.valueOf(ps.search_time.toDouble * 1e-6)
//          val nodes = String.valueOf(ps.nodes)
//          val mds = String.valueOf(ps.mds)
//          val ma = String.valueOf(ps.ma)
//          val mts = String.valueOf(ps.mts)
//          val ats = String.valueOf(ps.ats)
//          val prlm = String.valueOf(-1)
//          writer.writeRow(List(name, time, nodes, mds, ma, mts, ats, prlm))
//        }
//      }
//      writer.close()
//      println("-----" + folderStr + " done!-----")
//    }
//    println("-----All done!-----")
//  }
//
//  def withArgs(args: Array[String]): Unit = {
//    if (args.length == 1) {
//      val plm = args(0).toInt
//      withParallelism(plm)
//    }
//
//  }
//
//  def withParallelism(plm: Int) = {
//    val file = XML.loadFile("benchmarks/Folders.xml")
//    val inputRoot = (file \\ "inputRoot").text
//    val outputRoot = (file \\ "outputRoot").text
//    val outputFolder = (file \\ "outputFolder").text
//    val inputFolderNodes = file \\ "folder"
//
//    for (fn <- inputFolderNodes) {
//      val fmt = (fn \\ "@format").text.toInt;
//      val folderStr = fn.text
//      val inputPath = inputRoot + "/" + folderStr;
//      val files = getFiles(new File(inputPath));
//
//      val resFile = new File(outputRoot + "/" + outputFolder + "/PCT/" + folderStr + "_" + plm + ".csv")
//      val writer = CSVWriter.open(resFile)
//      writer.writeRow(List("name", "time", "nodes", "mds", "ma", "mts", "ats", "prlm"))
//
//      println("Warming Up");
//      {
//        val xm = new XModel(files(0).getPath, true, fmt)
//        val ps = new FJSolver5(xm, WARMUP, plm)
//        ps.search()
//        ps.shutdown()
//      }
//
//      for (f <- files) {
//        println("Build Model: " + f.getName)
//        val xm = new XModel(f.getPath, true, fmt)
//        println("Solve model!")
//
//        {
//          val ps = new FJSolver5(xm, TIME, plm)
//          ps.search()
//          ps.shutdown()
//        }
//
//        {
//          val ps = new FJSolver5(xm, TIME, plm)
//          ps.search()
//          ps.shutdown()
//          val name = ps.name
//          val time = String.valueOf(ps.search_time.toDouble * 1e-6)
//          val nodes = String.valueOf(ps.nodes)
//          val mds = String.valueOf(ps.mds)
//          val ma = String.valueOf(ps.ma)
//          val mts = String.valueOf(ps.mts)
//          val ats = String.valueOf(ps.ats)
//          val prlm = String.valueOf(plm)
//          writer.writeRow(List(name, time, nodes, mds, ma, mts, ats, prlm))
//        }
//      }
//      writer.close()
//      println("-----" + folderStr + " done!-----")
//    }
//    println("-----All done!-----")
//  }
//
//
//  //获取指定单个目录下所有文件
//  def getFiles(dir: File): Array[File] = {
//    dir.listFiles.filter(_.isFile) ++
//      dir.listFiles.filter(_.isDirectory).flatMap(getFiles)
//  }
//
//}
