//package cpscala.TSolver.Experiment
//
//import java.io.File
//
//import com.github.tototoshi.csv.CSVWriter
//import cpscala.JModel.JReader
//
//import scala.xml.XML
//
//object test_fde {
//  val TIME: Long = 1800000000000L
//  val WARMUP: Long = 30000000000L
//
//  def main(args: Array[String]): Unit = {
//    val file = XML.loadFile("benchmarks/Folders.xml")
//    val inputRoot = (file \\ "inputRoot").text
//    val outputRoot = (file \\ "outputRoot").text
//    val inputFolderNodes = file \\ "folder"
//    val pThdNum = Array[Int](8, 4, 2, 1)
//
//    for (fn <- inputFolderNodes) {
////      val fmt = (fn \\ "@format").text.toInt;
//      val folderStr = fn.text
//      val inputPath = inputRoot + "/" + folderStr;
//      val files = getFiles(new File(inputPath))
//
//      val resSerFile = new File(outputRoot +"/" + folderStr + ".csv")
//      val writer = CSVWriter.open(resSerFile)
//      writer.writeRow(List("name", "STR2", "FDE-STR2p-8", "FDE-STR2p-4", "FDE-STR2p-2", "FDE-STR2p-1", "nodes", "mds", "ma", "mts"))
//
//      for (f <- files) {
//        println("Build Model: " + f.getName)
//        val m = JReader.buildJModel(f.getPath)
//        println("Solve model! -singleton")
//
//        val s2 = new FDESTR2(m, TIME)
//        s2.search()
//        val name = f.getName
//
//        val s = String.valueOf(s2.search_time.toDouble * 1e-6)
////        println("Solve model! -warmup")
////
////        {
////          val sps = new FDEPSTR2(m, -1, WARMUP)
////          sps.search()
////          sps.shutdown()
////        }
//
//        val ps = new Array[String](4)
//
//        var i = 0
//        while (i < 4) {
//
//          var j =0
//          while(j<2){
//            val pn = pThdNum(i)
//            println("Solve model! -"+pn)
//            val sps = new FDEPSTR2(m, pn, TIME)
//            sps.search()
//            sps.shutdown()
//            ps(i) = String.valueOf(sps.search_time.toDouble * 1e-6)
//            j+=1
//          }
//
//          i += 1
//        }
//
//        val nodes = String.valueOf(s2.nodes)
//        val mds = String.valueOf(s2.mds)
//        val ma = String.valueOf(s2.ma)
//        val mts = String.valueOf(s2.mts)
//
//        writer.writeRow(List(name, s, ps(3), ps(2), ps(1), ps(0), nodes, mds, ma, mts))
//      }
//    }
//  }
//
//  //获取指定单个目录下所有文件
//  def getFiles(dir: File): Array[File] = {
//    dir.listFiles.filter(_.isFile) ++
//      dir.listFiles.filter(_.isDirectory).flatMap(getFiles)
//  }
//}
