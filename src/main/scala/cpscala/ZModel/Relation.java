package cpscala.ZModel;

public class Relation  extends  Instance {

    public String name = "";

    public int rnum = 0;
    public int vnum = 0;
    public int rs[][] = null;

    int change = 0;

    String semantics = "";

    int[] d = null;

    public Relation(String n,String s,int r,int v,int i)
    {
        super();
        name = n;
        semantics = s;
        rnum = r;
        vnum = v;
        id = i;

    }

    public void AddTuple(String content)
    {

        String sss = content.replaceAll("\\n", "");
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

            for (int j = 0; j < vnum; j++) {

                rs[i][j] = Integer.parseInt(t[j]);

            }

        }


    }


    public void print() {
        System.out.println("name:"+name);


        for (int i = 0; i < rnum; i++) {
            for (int j = 0; j < rs[i].length; j++) {
                System.out.print(rs[i][j] + " | ");
            }


        }
        System.out.println();
    }

}
