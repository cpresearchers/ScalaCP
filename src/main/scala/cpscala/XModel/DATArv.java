package cpscala.XModel;

public class DATArv {
    int varN = 0;
    int domn[] = null;

    int constraintN = 0;
    int[][] relations[] = null;
    int[] ccur = null;
    int[][] cdense = null;

    int[][] cscope = null;
    int[] cscopeN = null;
    int[] hvtocN = null;
    int[][] hvtoc = null;

    int[] scopes[] = null;
    int[] scopeN = null;
    int cinsectN[][] = null;

    int[] Dscopes[] = null;
    int[] DscopeN = null;
    int[][] Drelations[] = null;

    int[] vcur = null;
    int[] vdense[] = null;
    int[][] vsparse = null;

    int[][] vinc = null;
    int[] tsN;


    public DATArv(Model m) {
        varN = m.vnum;
        domn = new int[m.vnum];
        constraintN = m.cnum;
        relations = new int[m.cnum][][];
        ccur = new int[m.cnum];
        scopes = new int[m.cnum][];
        scopeN = new int[m.cnum];

        cdense = new int[m.cnum][];

        cscope = new int[m.cnum][];
        cscopeN = new int[m.cnum];
        tsN = new int[m.cnum];
        for (int i = 0; i < varN; i++) {
            domn[i] = m.vs[i].d.num;
        }

        for (int i = 0; i < m.cnum; i++) {
            relations[i] = m.cs[i].r.rvs;
            scopes[i] = new int[m.cs[i].vnum];
            scopeN[i] = m.cs[i].vnum;
            ccur[i] = m.cs[i].rnum - 1;
            cscope[i] = new int[scopes[i].length];
            cscopeN[i] = scopeN[i];
            tsN[i] = m.cs[i].rnum;

            cdense[i] = new int[m.cs[i].rnum];
            for (int j = 0; j < m.cs[i].rnum; j++) {
                cdense[i][j] = j;
            }

            for (int j = 0; j < scopeN[i]; j++) {
                scopes[i][j] = m.cs[i].vs[j].me;
                cscope[i][j] = scopes[i][j];
            }

        }

        this.createVC();
    }

    void createVC() {
        hvtoc = new int[varN][];
        hvtocN = new int[varN];

        for (int i = 0; i < varN; i++) {
            hvtocN[i] = 0;
        }

        for (int i = 0; i < constraintN; i++) {
            for (int j = 0; j < cscopeN[i]; j++) {
                int v = cscope[i][j];
                hvtocN[v]++;
            }
        }

        for (int i = 0; i < varN; i++) {
            hvtoc[i] = new int[hvtocN[i]];
            hvtocN[i] = 0;
        }

        for (int i = 0; i < constraintN; i++) {
            for (int j = 0; j < cscopeN[i]; j++) {
                int v = cscope[i][j];
                hvtoc[v][hvtocN[v]] = i;
                hvtocN[v]++;
            }
        }
    }

    public DATArv() {

    }

    public int getVn() {
        int n = 0;

        for (int i = 0; i < constraintN; i++) {
            n += scopeN[i];
        }

        return n;
    }
}
