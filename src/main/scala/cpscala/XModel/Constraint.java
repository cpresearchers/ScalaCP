package cpscala.XModel;

public class Constraint {
    int rnum = 0;
    int vnum = 0;
    int me = 0;
    double p = 1;
    Variable vs[] = null;
    int vmark[] = null;
    int deg = 0;
    int mind = 0;
    String name = null;

    Relation r = null;


    Constraint() {
    }

    public Constraint(int invnum, Relation inr, Variable invs[], int inme, String n, Model m) {
        vnum = invnum;
        vs = invs;
        r = inr;
        rnum = r.rnum;
        me = inme;
        name = n;
        vmark = new int[vnum];

        int s[] = new int[vs.length + 1];

        for (int i = 1; i <= vs.length; i++) {
            s[i] = vs[i - 1].d.me;
        }
        s[0] = r.me;

        r = new Relation(r);
        r.changeR(vs);
        rnum = r.rnum;
        m.rcur[m.rnumcur] = r;
        r.me = m.rnumcur;
        m.rnumcur++;
        //int a= m.inrcur.add(s);

        mind = vs[0].d.num;
        for (int i = 0; i < vnum; i++) {
            vmark[i] = vs[i].me;
            vs[i].addConstraint(this);
            p *= vs[i].d.num;
            if (vs[i].d.num < mind) {
                mind = vs[i].d.num;
            }
        }
        p = rnum / p;
           
    	   /*
    	   for(int i=0;i<vnum;i++)
    	   {
    		   System.out.print(vs[i].me+" ");
    	   }
		   System.out.println("");*/
    }

}
