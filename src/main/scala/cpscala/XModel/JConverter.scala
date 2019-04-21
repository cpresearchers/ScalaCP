package cpscala.XModel

import java.io.FileWriter

import com.alibaba.fastjson.JSONWriter
import cpscala.JModel.JModel
import cpscala.JModel.JVar
import cpscala.JModel.JTab

object JConverter {
  val jm = new JModel()

  def OriToJModel(m: XModel): JModel = {
    jm.fileName = m.fileName
    jm.maxArity = m.max_arity
    jm.maxDomSize = m.max_domain_size
    jm.maxTuplesSize = m.max_tuples_size

    jm.modelType = 0
    jm.numOriVars = -1
    jm.numOriTabs = -1
    jm.numFactors = -1
    jm.numVars = m.num_vars
    jm.numTabs = m.num_tabs
    jm.vars = new Array[JVar](jm.numVars)
    jm.tabs = new Array[JTab](jm.numTabs)

    var i = 0
    while (i < jm.numVars) {
      val v: XVar = m.vars.get(i)
      jm.vars(i) = new JVar(i, false, v.size)
      i += 1
    }

    i = 0
    while (i < jm.numTabs) {
      val t = m.tabs.get(i)
      jm.tabs(i) = new JTab(i, t.arity, t.tuples.length, t.scopeInt, t.tuples)
      i += 1
    }

    return jm
  }

  //生成JSON File
  @throws[Exception]
  def toJsonFile(outputPath: String): Unit = {
    val filePath = outputPath + "/" + jm.fileName + ".json"
    val writer = new JSONWriter(new FileWriter(filePath))
    writer.writeObject(jm)
    writer.close()
  }

}
