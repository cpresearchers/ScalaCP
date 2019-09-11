package cpscala.JModel;

import java.lang.reflect.Array;

public class JVar {
    public String name=" ";
    public int id;
    public boolean additional;
    public int size;
    public int[] values;

    public JVar(int name, boolean additional, int size) {
        this.id = name;
        this.additional = additional;
        this.size = size;
        values = new int[size];
        for (int i = 0; i < size; ++i) {
            values[i] = i;
        }
    }

    public void show() {
        System.out.println("name: " + id + ", additional: " + additional + ", size: " + size);
    }

}
