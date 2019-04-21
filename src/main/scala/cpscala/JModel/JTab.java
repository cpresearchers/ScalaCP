package cpscala.JModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JTab {
    public int name;
    public int arity;
    public int size;
    public int[] scope;
    public int[][] tuples;
    public Map<Integer, Float> varWeight;

    public JTab(int name, int arity, int size, int[] scope, int[][] tuples) {
        this.name = name;
        this.arity = arity;
        this.size = size;
        this.scope = scope;
        this.tuples = tuples;
        varWeight = new HashMap<>(arity);
        for (var i : scope) {
            varWeight.put(i, -1.0f);
        }
    }

    public void show() {
        System.out.println("name: " + name + " size: " + tuples.length + " arity: " + arity + " scope = " + Arrays.toString(scope));
        for (var t : tuples) {
            System.out.print(Arrays.toString(t) + " ");
        }
        System.out.println();
    }
}
