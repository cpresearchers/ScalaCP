package cpscala.JModel;

import com.alibaba.fastjson.JSONReader;

import java.io.FileReader;

public class JModel {
    public String fileName;
    public int numVars;
    public int numOriVars;
    public int numTabs;
    public int numOriTabs;
    public int numFactors;
    public int maxArity;
    public int maxDomSize;
    public int maxTuplesSize;
    //0: OriModel
    //1: FDEModel
    public int modelType;


    public JVar[] vars;
    public JTab[] tabs;

    public void show() {
        System.out.println("numVars: " + numVars + "numTabs: " + numTabs + "num_OriVars: " + numOriVars + "maxArity: " + maxArity + "maxDomSize: " + maxDomSize);

        for (var v : vars) {
            v.show();
        }

        for (var t : tabs) {
            t.show();
        }
    }
}

