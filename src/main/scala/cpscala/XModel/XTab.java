package cpscala.XModel;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class XTab {
    public int id;
    public String name;
    public int arity;
    public int[][] tuples;
    public XVar[] scope;
    public int[] scopeInt;
    public boolean STD;
    public boolean semantics;
    public Map<Integer, Integer> scopeIntMap = new LinkedHashMap<>();

    XTab(int id, String name, boolean sem, int[][] ts, XVar[] scp, boolean transform, boolean AsPre) {
        this.id = id;
        this.name = name;
        this.semantics = sem;
        this.arity = scp.length;
        this.scope = scp;
        scopeInt = new int[arity];

        for (int i = 0; i < arity; ++i) {
            var vid = scp[i].id;
            scopeInt[i] = vid;
            scopeIntMap.put(vid, i);
        }

        if (!AsPre) {
            //没转过
            int[] ori_t = new int[arity];
            int[] std_t = new int[arity];
            boolean standard = true;
            for (XVar v : scp) {
                if (!v.isSTD) {
                    standard = false;
                    break;
                }
            }

            if (transform) {
                //转正表，
                //语义转正
                int all_size = 1;
                for (XVar v : scp) {
                    all_size *= v.size;
                }
                int sup_size;

                if (!semantics)
                    sup_size = all_size - ts.length;
                else
                    sup_size = ts.length;

                //原表为负表
                tuples = new int[sup_size][arity];
                if (!semantics) {
                    int i = 0, j = 0;
                    GetTuple(i++, ori_t, std_t);

//                    while (j < ts.length) {
                    for (int[] t : ts) {
//                        int[] t = ts[j];
                        while (!Arrays.equals(ori_t, t)) {
                            System.arraycopy(std_t, 0, tuples[j++], 0, arity);
                            GetTuple(i++, ori_t, std_t);
                        }
                        GetTuple(i++, ori_t, std_t);
                    }

                    while (j < sup_size) {
                        System.arraycopy(std_t, 0, tuples[j++], 0, arity);
                        GetTuple(i++, ori_t, std_t);
                    }
                } else {
                    //原表为正表
                    if (!standard) {
                        int i = 0;
                        //非标准表转标准表
                        for (int[] t : ts) {
                            ToSTDTuple(t, std_t);
                            System.arraycopy(std_t, 0, tuples[i++], 0, arity);
//                            ++i;
                        }
                    } else {
//                    //标准论域
                        for (int i = 0; i < ts.length; ++i) {
                            tuples[i] = Arrays.copyOf(ts[i], arity);
                        }
                    }
                }
                semantics = true;
                STD = true;
            } else {
                //不转需正表，保持原样，但需标准化
                tuples = new int[ts.length][arity];
                if (!standard) {
                    //非标准表转标准表
                    int i = 0;
                    for (int[] t : ts) {
                        ToSTDTuple(t, std_t);
                        System.arraycopy(std_t, 0, tuples[i], 0, arity);
                        ++i;
                    }
                } else {
                    for (int i = 0; i < ts.length; ++i) {
                        tuples[i] = Arrays.copyOf(ts[i], arity);
                    }
                }
            }
        } else {
            for (int i = 0; i < ts.length; ++i) {
                tuples[i] = Arrays.copyOf(ts[i], arity);
            }
        }
    }

    XTab(int id, String name, int[][] ts, XVar[] scp) {
        this.id = id;
        this.name = name;
        this.semantics = true;
        this.scope = scp;
        this.arity = scp.length;
        scopeInt = new int[arity];

        for (int i = 0; i < arity; ++i) {
            var vid = scp[i].id;
            scopeInt[i] = vid;
            scopeIntMap.put(vid, i);
        }

        STD = true;
        semantics = true;
        tuples = ts;
    }

    XTab(XTab t, XVar[] scp) {
        this.id = t.id + 1;
        this.STD = t.STD;
        this.arity = scp.length;
        this.semantics = t.semantics;
        this.scope = scp;
        scopeInt = new int[arity];

        for (int i = 0; i < arity; ++i) {
            scopeInt[i] = scp[i].id;
        }

        tuples = t.tuples;
    }

    void GetTuple(int idx, int[] src_t, int[] std_t) {
        for (int i = (scope.length - 1); i >= 0; --i) {
            XVar v = scope[i];
            std_t[i] = idx % v.size;
            src_t[i] = v.values_ori[std_t[i]];
            idx /= v.size;
        }
    }

    void ToSTDTuple(int[] ori_t, int[] std_t) {
        for (int i = 0; i < ori_t.length; ++i)
            std_t[i] = scope[i].values_map.get(ori_t[i]);
    }

    boolean have(int[] tuple) {
        return Arrays.binarySearch(tuples, tuple) >= 0;
    }

    public void show() {
//        String sem = semantics ? "supports" : "conflicts";
//        System.out.println("name: " + id + " semantics: " + sem + " size: " + tuples.length + " arity: " + arity + " scope = " + Arrays.toString(scopeInt));
        System.out.println("name: " + id + " size: " + tuples.length + " arity: " + arity + " scope = " + Arrays.toString(scopeInt));

//        for (int[] t : tuples) {
//            System.out.print(Arrays.toString(t));
//        }
//        System.out.println();
    }

    public int getVarIndex(int vid) {
        return scopeIntMap.get(vid);
    }

    public int getSTDValue(int index, int vid) {
        return tuples[index][getVarIndex(vid)];
    }

}
