package cpscala.XModel;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ZTab {

    public int id;
    public String name;
    public int arity;
   // public int[][] tuples;
    public Relation R;
    public XVar[] scope;
    public int[] scopeInt;
    public boolean STD;
    public boolean semantics;
    public Map<Integer, Integer> scopeIntMap = new LinkedHashMap<>();

    public ZTab(int id, String name, boolean sem, Relation r, XVar[] scp, boolean transform, boolean AsPre)
    {
        this.id = id;
        this.name = name;
        this.semantics = sem;
        this.arity = scp.length;
        this.scope = scp;
        scopeInt = new int[arity];
        R = r;

        for (int i = 0; i < arity; ++i) {
            var vid = scp[i].id;
            scopeInt[i] = vid;
            scopeIntMap.put(vid, i);
        }


       /* if(AsPre) {
            tuples = new int[ts.length][arity];
            for (int i = 0; i < ts.length; ++i) {
                tuples[i] = Arrays.copyOf(ts[i], arity);
            }
        }*/
    }

    public void show() {
        String sem = semantics ? "supports" : "conflicts";
        System.out.println("name: " + id + " semantics: " + sem + " size: " + R.rvs.length + " arity: " + arity + " scope = " + Arrays.toString(scopeInt) + "R = " + R.id);
        for (int[] t : R.rvs) {
            System.out.print(Arrays.toString(t));
        }
        System.out.println();
    }



    public double Looseness() {
        int p = 1;
        for (var t : scope
        ) {
            p *= t.size;
        }

        return (double) R.rvs.length / (double) p;
    }

    public double Tightness() {

        return 1 - Looseness();
    }

}
