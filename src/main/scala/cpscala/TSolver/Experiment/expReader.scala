package cpscala.TSolver.Experiment

import java.io.FileReader

import com.alibaba.fastjson.JSONReader

object expReader {

  def build(path: String): exp = {
    val reader = new JSONReader(new FileReader(path))
    val m: exp = reader.readObject(classOf[exp])
    reader.close()
    return m
  }
}
