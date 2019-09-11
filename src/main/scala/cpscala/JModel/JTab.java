package cpscala.JModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JTab {
    public String name=" ";
    public int id;
    public int arity;
    public int size;
    public int[] scope;
    public int[] scopeInt;
    public int[][] tuples;
    public Map<Integer, Float> varWeight;

    public JTab(int name, int arity, int size, int[] scope, int[][] tuples) {
        this.id = name;
        this.arity = arity;
        this.size = size;
        this.scope = scope;
        this.tuples = tuples;
        varWeight = new HashMap<>(arity);
        scopeInt = new int[arity];
        for (var i : scope) {
            varWeight.put(i, -1.0f);
        }
        for (int i = 0; i < arity; ++i) {
            scopeInt[i] = scope[i];
        }
    }

    public void show() {
        System.out.println("name: " + id + " size: " + tuples.length + " arity: " + arity + " scope = " + Arrays.toString(scope));
        for (var t : tuples) {
            System.out.print(Arrays.toString(t) + " ");
        }
        System.out.println();
    }
}
