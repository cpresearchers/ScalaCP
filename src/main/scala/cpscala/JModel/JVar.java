package cpscala.JModel;

public class JVar {
    public int name;
    public boolean additional;
    public int size;

    public JVar(int name, boolean additional, int size) {
        this.name = name;
        this.additional = additional;
        this.size = size;
    }

    public void show() {
        System.out.println("name: " + name + ", additional: " + additional + ", size: " + size);
    }

}
