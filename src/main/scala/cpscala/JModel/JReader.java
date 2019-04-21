package cpscala.JModel;

import com.alibaba.fastjson.JSONReader;
import cpscala.TSolver.Experiment.exp;

import java.io.FileReader;

public class JReader {
//    public static JModel buildJModel(String path) throws Exception {
////        JModel m = new JModel();
//        JSONReader reader = new JSONReader(new FileReader(path));
//        reader.startObject();
//        while(reader.hasNext()){
//            System.out.println(reader.readString());
//        }
////        return m;
//    }

    public static JModel buildJModel(String path) throws Exception {
        JSONReader reader = new JSONReader(new FileReader(path));
        JModel m = reader.readObject(JModel.class);
        reader.close();
        return m;
    }


    public static exp buildExp(String path)  throws Exception{
        JSONReader reader = new JSONReader(new FileReader(path));
        exp m = reader.readObject(exp.class);
        reader.close();
        return m;
    }

    //        return m;
//    }
//var str = new FileReader(path);
//str.
//        JModel m = JSONObject.parseObject(str.read(), JModel.class);
}

