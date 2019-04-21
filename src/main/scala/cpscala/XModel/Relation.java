package cpscala.XModel;

public class Relation {
    String name = "";
    int rnum = 0;
    int vnum = 0;
    int rs[][] = null;
    int rvs[][] = null;
    int change = 0;
    int me = 0;
    String type = "";
    int[] d = null;


    boolean chtype = false;

    public Relation(Relation r) {
        rnum = r.rnum;
        vnum = r.vnum;
        rs = r.rs;
        rvs = r.rvs;
        me = r.me;
        type = r.type;
        d = new int[vnum];
    }

    public Relation(int inrnum, int invnum, int inme, int d) {
        vnum = invnum;
        rnum = inrnum;
        rs = new int[vnum][rnum];
        rvs = new int[rnum][vnum];
        me = inme;
    }

    public Relation(int inrnum, int invnum, String s, String intype, int inme) {
        vnum = invnum;
        rnum = inrnum;
        type = intype;
        rs = new int[vnum][rnum];
        rvs = new int[rnum][vnum];
        me = inme;
        String sss = s.replaceAll("\\n", "");
        sss = sss.trim();
        String[] ss = sss.split("[|]");

        for (int i = 0; i < rnum; i++) {
            //System.out.println(ss[i]);
            String[] t = null;
            if (ss[i].startsWith(" ")) {
                t = ss[i].replaceFirst(" ", "").split("[ ]");
            } else {
                t = ss[i].split("[ ]");
            }
            for (int j = 0; j < t.length; j++) {
                rs[j][i] = Integer.parseInt(t[j]);
                rvs[i][j] = rs[j][i];
            }
        }

        d = new int[vnum];

    }

    public Relation() {

    }

    public void changeR(Variable[] ds) {

        int rinit[][] = new int[vnum][rnum];

        for (int i = 0; i < vnum; i++) {
            d[i] = ds[i].d.me;
        }

        int rn = 0;
        for (int i = 0; i < rnum; i++) {
            boolean f = false;
            for (int j = 0; j < vnum; j++) {
                rinit[j][rn] = ds[j].d.getV(rs[j][i]);
                if (rinit[j][rn] == -1) {
                    f = true;
                }
            }
            rn++;
            if (f) {
                rn--;
            }
        }

        rnum = rn;

        rs = new int[vnum][rn];
        rvs = new int[rn][vnum];

        for (int i = 0; i < rnum; i++) {
            for (int j = 0; j < vnum; j++) {
                rs[j][i] = rinit[j][i];
                rvs[i][j] = rs[j][i];
            }
        }


        if (type.equals("supports")) {

        } else {

            int n = 1;

            for (int i = 0; i < vnum; i++) {
                n *= ds[i].d.num;
            }

            int r[][] = new int[vnum][n - rnum];
            int rv[][] = new int[n - rnum][vnum];

            rnum = n - rnum;
            //if(me==1)System.out.println(rnum);
            int curr = 0;
            int cur = 0;


            for (int i = 0; i < vnum; i++) {
                r[i][cur] = 0;
            }

            while (true) {
                if (curr < n - rnum && equal(r, cur, rs, curr)) {
                    curr++;
                    int z = 1;

                    for (int i = vnum - 1; i >= 0; i--) {
                        if (z == 1 && r[i][cur] == ds[i].d.num - 1) {
                            r[i][cur] = 0;
                            rv[cur][i] = r[i][cur];
                        } else {
                            r[i][cur] += z;
                            rv[cur][i] = r[i][cur];
                            z = 0;
                        }
                    }
                } else {

                    cur++;
                    int z = 1;
                    if (cur == rnum) {
                        break;
                    }
                    for (int i = vnum - 1; i >= 0; i--) {

                        if (z == 1 && r[i][cur - 1] == ds[i].d.num - 1) {

                            r[i][cur] = 0;
                            rv[cur][i] = r[i][cur];
                        } else {
                            r[i][cur] = r[i][cur - 1] + z;
                            rv[cur][i] = r[i][cur];
                            z = 0;
                        }
                    }


                }
            }

            rs = r;
            rvs = rv;
        }

    }


    void ch(Variable[] ds) {

        int n = 1;
        if (chtype) return;

        chtype = true;
        for (int i = 0; i < vnum; i++) {
            n *= ds[i].d.num;
        }
        System.out.println("aaa" + rnum);
        rnum = n - rnum;
        System.out.println(rnum);
        int r[][] = new int[rnum][vnum];


        int curr = 0;
        int cur = 0;


        for (int i = 0; i < vnum; i++) {
            r[cur][i] = 0;
        }

        while (true) {
            if (curr < n - rnum && equal(r, cur, rs, curr)) {

                curr++;
                int z = 1;

                for (int i = vnum - 1; i >= 0; i--) {
                    if (z == 1 && r[cur][i] == ds[i].d.num - 1) {
                        r[cur][i] = 0;
                    } else {
                        r[cur][i] += z;
                        z = 0;
                    }
                }
            } else {

                cur++;
                int z = 1;
                if (cur == rnum) {
                    break;
                }
                for (int i = vnum - 1; i >= 0; i--) {

                    if (z == 1 && r[cur - 1][i] == ds[i].d.num - 1) {

                        r[cur][i] = 0;
                    } else {
                        r[cur][i] = r[cur - 1][i] + z;
                        z = 0;
                    }
                }


            }

        }
        // rs=r;
    }

    boolean equal(int[][] r1, int r1n, int[][] r2, int r2n) {
        for (int i = 0; i < vnum; i++) {
            if (r1[i][r1n] != r2[i][r2n]) {
                return false;
            }
        }

        return true;
    }


    public void print() {
        for (int i = 0; i < rnum; i++) {
            for (int j = 0; j < vnum; j++) {
                System.out.print(rs[i][j] + " ");
            }
            System.out.println();
        }
    }
}
