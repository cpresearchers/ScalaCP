package cpscala.XModel;

public class Domain {
    String name = "";
    int num = 0;
    int vals[] = null;
    int me = 0;

    public Domain(int innum, int inme) {
        num = innum;
        vals = new int[num];
        me = inme;
        for (int i = 0; i < num; i++) {
            vals[i] = i;

        }
    }


    public Domain(int innum, String s, int inme, String inname) {
        num = innum;
        vals = new int[num];
        me = inme;
        name = inname;
        String sss = s.replaceAll("\\n", "");
        //
        sss = sss.trim();

        String as[] = sss.split(" ");

        int val = 0;


        for (int i = 0; i < as.length; i++) {
            sss = as[i];

            if (sss.matches("[0-9]*[.][.][0-9]*")) {
                String ss[] = sss.split("[.][.]");

                int start = Integer.parseInt(ss[0]);
                int end = Integer.parseInt(ss[1]);

                for (int j = start; j <= end; j++) {
                    vals[val] = j;
                    val++;
                }
            } else {
                vals[val] = Integer.parseInt(sss);
                val++;
            }
        }

    }

    public int getV(int n) {
        for (int i = 0; i < num; i++) {
            if (vals[i] == n) {
                return i;
            }
        }

        return -1;
    }
}
