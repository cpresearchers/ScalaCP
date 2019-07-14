package cpscala.XModel;

import java.util.LinkedHashMap;
import java.util.Map;

public class XVar {
    public int id;
    public String name;
    public int[] values;
    public int[] values_ori;
    public Map<Integer, Integer> values_map = new LinkedHashMap<>();
    public int size;
    public boolean isSTD;

    XVar(int id, String name, int[] vals) {
        this.id = id;
        this.name = name;
        size = vals.length;
        values_ori = vals;
        values = new int[size];
        isSTD = true;

        for (int i = 0; i < size; ++i) {
            values[i] = i;
            values_map.put(vals[i], i);
            if (vals[i] != i) {
                isSTD = false;
            }
        }
    }

    XVar(int id, String name, int minValues, int maxValues) {
        this.id = id;
        this.name = name;
        size = maxValues - minValues + 1;
        values = new int[size];
        values_ori = new int[size];

        for (int i = minValues, j = 0; i <= maxValues; ++i, ++j) {
            values_ori[j] = i;
            values[j] = j;
            values_map.put(i, j);
        }

        isSTD = (minValues == 0);
    }

    void show() {
        System.out.println("name: " + id + " name: " + name);
        for (int i = 0; i < size; ++i) {
            System.out.print(values[i] + "[" + values_ori[i] + "] ");
        }
        System.out.println();
    }

}
