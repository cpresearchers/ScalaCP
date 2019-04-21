package cpscala.XModel;

public class FDEVar extends XVar {
    boolean addtional;

    FDEVar(int id, String name, int[] vals, boolean isAdd) {
        super(id, name, vals);
        addtional = isAdd;
    }

    FDEVar(XVar v, boolean isAdd) {
        super(v.id, v.name, v.values);
        addtional = isAdd;
    }

    FDEVar(int id, String name, int minValues, int maxValues, boolean isAdd) {
        super(id, name, minValues, maxValues);
        addtional = isAdd;
    }
}
