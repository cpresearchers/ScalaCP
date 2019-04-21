package cpscala.XModel;

public class Variable {
    String name = "";
    Domain d = null;
    int me = 0;
    int cnum = 0;
    Constraint cs[] = null;

    public Variable(Domain ind, int inme, int incnum, String inname) {
        d = ind;
        me = inme;
        cs = new Constraint[incnum];
        cnum = 0;
        name = inname;
    }

    public Variable(Domain ind, int inme, int incnum) {
        d = ind;
        me = inme;
        cs = new Constraint[incnum];
        cnum = 0;
    }

    public void addConstraint(Constraint C) {
        cs[cnum] = C;

        //System.out.println(cs[cnum].me);
        cnum++;
    }

}
