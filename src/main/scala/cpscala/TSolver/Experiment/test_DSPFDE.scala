package cpscala.TSolver.Experiment

import java.io.File

import cpscala.XModel.{FDEModel1, XModel}

import scala.xml.XML

object test_DSPFDE {
  def main(args: Array[String]): Unit = {


  }

  def singleFile(): Unit = {
    val xf = XML.loadFile("benchmarks/BMPath.xml")
    val fileNode = xf \\ "BMFile"
    val path = fileNode.text
    val fmt = (fileNode \\ "@format").text.toInt
    println("Build Model: " + path)
    val xm = new XModel(path, true, fmt)
    val fdem = new FDEModel1(path, fmt)


  }

  def groupFiles(): Unit = {

  }

  //获取指定单个目录下所有文件
  def getFiles(dir: File): Array[File] = {
    dir.listFiles.filter(_.isFile) ++
      dir.listFiles.filter(_.isDirectory).flatMap(getFiles)
  }
}
