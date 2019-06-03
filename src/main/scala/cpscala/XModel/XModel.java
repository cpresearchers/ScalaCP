package cpscala.XModel;

import org.xcsp.common.Types;
//import org.xcsp.parser.XCallbacks2;
//import org.xcsp.parser.callbacks.XCallbacks;
import org.xcsp.parser.callbacks.XCallbacks2;
import org.xcsp.parser.entries.XVariables;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class XModel implements XCallbacks2 {


    private Implem implem = new Implem(this);
    public String filePath;
    public String fileName;
    public int num_vars = 0;
    public int num_tabs = 0;
    public Map<String, XVar> vars_map = new LinkedHashMap<>();
    public ArrayList<XVar> vars = new ArrayList<>();
    public ArrayList<XTab> tabs = new ArrayList<>();
    public boolean need_positive;
    public int max_arity = Integer.MIN_VALUE;
    public int max_domain_size = Integer.MIN_VALUE;
    public int max_tuples_size = Integer.MIN_VALUE;
    public float avg_tuples_size = Float.MIN_VALUE;
    public String name;

    @Override
    public Implem implem() {
        return implem;
    }

    public XModel(String fileName, boolean transform, int format) throws Exception {
        filePath = fileName;
        name = getFileName(fileName);
        if (format == 3) {
            need_positive = transform;
//        Document doc = Utilities.loadDocument(fileName);
//        XParser parser = new XParser(doc);
            loadInstance(fileName);
//        parser.vEntries.stream().forEach(e -> System.out.println(e.toString()));
//        parser.cEntries.stream().forEach(e -> System.out.println(e.toString()));
//        parser.oEntries.stream().forEach(e -> System.out.println(e.toString()));
        } else if (format == 2) {
            build(filePath);
        }
    }

    @Override
    public void buildVarInteger(XVariables.XVarInteger x, int[] values) {
        XVar var = new XVar(num_vars++, x.id, values);
        vars_map.put(x.id, var);
        vars.add(var);
        max_domain_size = Math.max(max_arity, var.size);
    }

    @Override
    public void buildVarInteger(XVariables.XVarInteger x, int minValue, int maxValue) {
        XVar var = new XVar(num_vars++, x.id, minValue, maxValue);
        vars_map.put(x.id, var);
        vars.add(var);
        max_domain_size = Math.max(max_arity, var.size);
    }

    @Override
    public void buildCtrExtension(String id, XVariables.XVarInteger[] list, int[][] tuples, boolean positive, Set<Types.TypeFlag> flags) {
//        if (flags.contains(Types.TypeFlag.STARRED_TUPLES)) {
//// Can you manage short tables ? i.e., tables with tuples containing symbol * ?
//// If not, throw an exception.
////...
//        }
//        if (flags.contains(Types.TypeFlag.UNCLEAN_TUPLES)) {
//// You have possibly to clean tuples here, in order to remove invalid tuples.
//// A tuple is invalid if it contains a value a for a variable x, not present in dom(x)
//// Note that most of the time, tuples are already cleaned by the parser
////...
//        }

        XVar[] v = new XVar[list.length];

        for (int i = 0; i < list.length; ++i) {
            v[i] = vars_map.get(list[i].id);
        }
        XTab t = new XTab(num_tabs++, id, positive, tuples, v, need_positive, false);
        tabs.add(t);
        max_arity = Math.max(max_arity, t.arity);
        max_tuples_size = Math.max(max_tuples_size, t.tuples.length);
        avg_tuples_size = (avg_tuples_size * (tabs.size() - 1) + t.tuples.length) / tabs.size();
    }

    boolean check(int[] vals) {
        boolean res = false;
        for (XTab t : tabs) {
            int[] tuple = new int[t.arity];
            for (int i = 0; i < t.arity; ++i) {
                tuple[i] = vals[t.scope[i].id];
                if (!t.have(tuple)) {
                    return false;
                }
            }
        }
        return true;
    }

    void build(String filename) {
        Model m = new Model(filename);
//        DATArv d = new DATArv(m);

        for (int i = 0; i < m.vnum; ++i) {
            var v = m.vs[i];
            var d = v.d;
            XVar var = new XVar(num_vars++, "", 0, d.num - 1);
            vars.add(var);
            max_domain_size = Math.max(max_arity, var.size);
        }

        for (int i = 0; i < m.cnum; ++i) {
            var c = m.cs[i];
            var r = c.r;
            XVar[] v = new XVar[c.vnum];
            for (int j = 0; j < c.vnum; ++j) {
                v[j] = vars.get(c.vs[j].me);
            }
            XTab t = new XTab(num_tabs++, "", true, r.rvs, v, need_positive, false);
            tabs.add(t);
            max_arity = Math.max(max_arity, t.arity);
        }
    }

    String getFileName(String path) {
        var fileName = path.substring(path.lastIndexOf("/") + 1, path.indexOf("."));
        this.fileName = fileName;
        return fileName;
    }


    public void show() {
//        for (XVar x:vars){
//            x.show();
//        }
        System.out.println("show model: numVars = " + vars.size());
        vars.forEach(e -> e.show());
        System.out.println("show model: numTabs = " + tabs.size());
        tabs.forEach(e -> e.show());
//        tabs.get(0).show();
    }
}
