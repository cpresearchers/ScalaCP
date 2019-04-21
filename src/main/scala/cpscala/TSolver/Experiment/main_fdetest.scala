package cpscala.TSolver.Experiment

import cpscala.XModel.FDEModel

import scala.xml.XML

object main_fdetest extends App {
  val xf = XML.loadFile("benchmarks/BMPath.xml")
  val fileNode = xf \\ "BMFile"
  val path = fileNode.text
  val fmt = (fileNode \\ "@format").text.toInt;
  println(path)
  val fdem = new FDEModel(path, fmt);
  fdem.buildJModel()
  val jm = fdem.jm
  jm.show()
//  val str = fdem.toJsonStr()
//  println(str)
}
