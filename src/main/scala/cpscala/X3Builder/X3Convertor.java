package cpscala.X3Builder;

import org.xcsp.common.IVar;
import org.xcsp.modeler.api.ProblemAPI;

import java.io.File;
import java.util.ArrayList;

public class X3Convertor implements ProblemAPI {
    // 文件夹id
    int n;
    // 文件id
    int r;

    public void model() {
        IVar.Var x[] = array("x", size(4), dom(range(15)), "x[i] is the ith integer of the sequence");
        equal(add(x[0], 1), x[1]);
        equal(add(x[1], 1), x[2]);
        equal(add(x[2], 1), x[3]);
        equal(add(x[0], x[1], x[2], x[3]), 14);
    }

    public static ArrayList<String> getFiles(String path) {
        ArrayList<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
//              System.out.println("文     件：" + tempList[i]);
                files.add(tempList[i].toString());
            }
            if (tempList[i].isDirectory()) {
//              System.out.println("文件夹：" + tempList[i]);
            }
        }
        return files;
    }

//    public String getFileName(int j, int i){
//
//    }


}
