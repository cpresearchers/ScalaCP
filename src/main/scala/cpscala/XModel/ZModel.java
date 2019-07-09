package cpscala.XModel;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/*

    镇路晗 补充了一部分代码。考虑到|R| <= |C| 为了节约更多空间，把约束只会存储关系的id，而不是每个都拷贝一次
    本次更新不会影响之前的代码

 */
public class ZModel extends XModel{


    public String filePath;
   // public String fileName;
   // public int num_vars = 0;
    //public int num_tabs = 0;
    public int num_rela = 0;
   // public Map<String, XVar> vars_map = new LinkedHashMap<>();
   // public ArrayList<XVar> vars = new ArrayList<>();
    public ArrayList<ZTab> tabs = new ArrayList<>();
    public Model m = null;
   // public boolean need_positive;
   // public int max_arity = Integer.MIN_VALUE;
   // public int max_domain_size = Integer.MIN_VALUE;
   // public int max_tuples_size = Integer.MIN_VALUE;
   // public float avg_tuples_size = Float.MIN_VALUE;
   // public String name;


    public ZModel(String fileName, boolean transform, int format) throws Exception
    {

        filePath = fileName;
        //System.out.println(filePath);
        if(format == 2) {
            do_build();
        }



    }


    private void do_build() {

         m = new Model(filePath);

        for (int i = 0; i < m.vnum; ++i) {
            var v = m.vs[i];
            var d = v.d;
            XVar var = new XVar(num_vars++, v.name, d.vals);
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
            ZTab t = new ZTab(num_tabs++, c.name, true, r, v, need_positive, true);
            tabs.add(t);
            max_arity = Math.max(max_arity, t.arity);
            max_tuples_size = Math.max(max_tuples_size, r.rvs.length);
            avg_tuples_size = (avg_tuples_size * (tabs.size() - 1) + r.rvs.length) / tabs.size();
        }
        num_rela = m.rs.length;

    }


    public void show_Relation()
    {
        System.out.println(num_rela);

        for(int i  = 0; i <  num_rela;i++)
        {
            System.out.print(i+" : ");
            var r = tabs.get(i).R;
            r.print();

        }

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
/*
    public double Get_Looseness()  //by zhenluhan 6.3
    {
        double s = 0;
        for(var i : tabs)
        {
            s += i.Looseness();
        }
        return s / (double)tabs.size();
    }

    public double Get_Tightness() //by zhenluhan 6.3
    {
        return 1- Get_Looseness();
    }

    public double Get_Ave_Domain_Size() //by zhenluhan 6.4
    {
        int sum = 0;
        for (var a:vars) {
            sum += a.values.length;

        }
        return sum / (double)vars.size();
    }*/

}
