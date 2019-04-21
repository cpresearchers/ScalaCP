package cpscala.TSolver.CpUtil.MDD;

public class JArc {
    public int label;
    public JNode start;
    public JNode end;

    public JArc(int label, JNode start, JNode end) {
        this.label=label;
        this.start = start;
        this.end  = end;
    }


}
