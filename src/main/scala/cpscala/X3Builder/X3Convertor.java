//package cpscala.X3Builder;
//
//import org.xcsp.common.IVariable;
//import org.xcsp.modeler.api.ProblemAPI;
//
//import java.io.File;
//import java.util.ArrayList;
//
//public class X3Convertor implements ProblemAPI {
//    // 文件夹id
//    int n;
//    // 文件id
//    int r;
//
//    public void model() {
//        IVariable.Var x[] = array("x", size(4), dom(range(15)), "x[i] is the ith integer of the sequence");
//        equal(add(x[0], 1), x[1]);
//        equal(add(x[1], 1), x[2]);
//        equal(add(x[2], 1), x[3]);
//        equal(add(x[0], x[1], x[2], x[3]), 14);
//    }
//
//    public static ArrayList<String> getFiles(String path) {
//        ArrayList<String> files = new ArrayList<String>();
//        File file = new File(path);
//        File[] tempList = file.listFiles();
//
//        for (int i = 0; i < tempList.length; i++) {
//            if (tempList[i].isFile()) {
//
//                files.add(tempList[i].toString());
//            }
//            if (tempList[i].isDirectory()) {
//
//            }
//        }
//        return files
//    }
//}
