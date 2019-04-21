package cpscala.X3Builder;

import org.w3c.dom.Document;
import org.xcsp.modeler.Compiler;
import org.xcsp.modeler.api.ProblemAPI;
import org.xcsp.modeler.implementation.ProblemIMP;

import java.util.stream.Stream;

import static org.xcsp.modeler.Compiler.IC;
import static org.xcsp.modeler.Compiler.OUTPUT;

public class x3jmain {
    public static void main(String[] args) {

//        X3Convertor xx = new X3Convertor();

//        org.xcsp.modeler.problems
//        String[] argss = {"org.xcsp.modeler.problems.Bibd -data=[6,50,25,3,10] -dataFormat=[%02d,%02d,%02d,%02d,%02d] -dataSaving"};
        String[] argss = {"cpscala.X3Builder.X3Convertor"};
        Compiler.main(argss);
//        c.buildDocument();

//        ProblemAPI api = buildInstanceAPI(args);
//        if (api == null)
//            return;
//        Document document = new Compiler(api).buildDocument();
//        String output = Stream.of(args).filter(s -> s.startsWith(OUTPUT)).map(s -> s.substring(OUTPUT.length() + 1)).findFirst().orElse(null);
//        String fileName = (output != null ? output : api.name()) + ".xml";
//        ProblemAPI.api2imp.get(api).save(document, fileName);
//        if (Stream.of(args).anyMatch(s -> s.equals(IC)))
//            ProblemAPI.api2imp.get(api).indentAndCompressXmlUnderLinux(fileName);
    }
}
