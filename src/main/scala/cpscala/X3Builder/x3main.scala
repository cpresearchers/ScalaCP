package cpscala.X3Builder

import java.util.stream.Stream

import org.w3c.dom.Document
import org.xcsp.modeler.Compiler
import org.xcsp.modeler.api.ProblemAPI
import org.xcsp.modeler.implementation.ProblemIMP

object x3main extends App {
  println("Experiment")
//  val xx = new X3Convertor();
//  org.xcsp.modeler.Compiler xx;
//  val api = buildInstanceAPI(args)
//  if (api != null) {
//    val document = new Compiler(api).buildDocument
//    val output = Stream.of(args).filter((s: String) => {
//      def foo(s: String) = s.startsWith("-output")
//
//      foo(s)
//    }).map((s: String) => {
//      def foo(s: String) = s.substring("-output".length + 1)
//
//      foo(s)
//    }).findFirst.orElse(null.asInstanceOf[Any]).asInstanceOf[String]
//    val fileName = (if (output != null) output
//    else api.name) + ".xml"
//    ProblemAPI.api2imp.get(api).asInstanceOf[ProblemIMP].save(document, fileName)
//    if (Stream.of(args).anyMatch((s: String) => {
//      def foo(s: String) = s == "-ic"
//
//      foo(s)
//    })) ProblemAPI.api2imp.get(api).asInstanceOf[ProblemIMP].indentAndCompressXmlUnderLinux(fileName)
//  }
}
