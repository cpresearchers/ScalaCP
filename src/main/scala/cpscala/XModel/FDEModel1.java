package cpscala.XModel;

import cpscala.JModel.JModel;
import scala.Tuple2;
import java.util.*;

//元组未排序
public class FDEModel1 {
    public int num_vars = 0;
    public int num_tabs = 0;
    public int num_OriVars;
    public int num_OriTabs;
    public int num_OriMA;
    public int num_tmp;
    public String fileName;

    public JModel jm;

    public FDEVar[] vars;
    public FDETab[] tabs;

    public int max_arity = Integer.MIN_VALUE;
    public int max_domain_size = Integer.MIN_VALUE;
    public int max_tuples_size = Integer.MIN_VALUE;

    //约束scope矩阵
    public ArrayList<Integer>[][] tabsScopeMatrix;
    //附加约束id矩阵
    public int[][] tabsIDMatrix;

    //针对于老变量的标记
    //第一层是约束ID第二层是变量ID
    public boolean[][] commonVarsBoolean;
    private ArrayList<ArrayList<Integer>> addtionTabsVarScopeArray = new ArrayList<>();
    private ArrayList<ArrayList<Tuple2<Integer, Integer>>> addtionTabsTabScopeArray = new ArrayList<>();
    //为旧变量
    private ArrayList<Integer>[] newScopesInt;

    XModel xm;

    public FDEModel1(XModel xm) {
        this.xm = xm;
        initial();
        xm = null;
    }

    public FDEModel1(String path, int fmt) throws Exception {
        this.xm = new XModel(path, true, fmt);
        String name = getFileName(path);
        long starttime = System.currentTimeMillis();

        initial();

        long endtime = System.currentTimeMillis();
        var search_time = endtime - starttime;
        System.out.println("FDEtime:" + search_time);

        xm = null;
    }

    String getFileName(String path) {
        fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
        return fileName;
    }

    void initial() {
        this.num_OriVars = xm.num_vars;
        this.num_OriTabs = xm.num_tabs;
        this.num_OriMA = xm.max_arity;
        tabsScopeMatrix = new ArrayList[num_OriTabs][num_OriTabs];
        commonVarsBoolean = new boolean[num_OriTabs][];
        newScopesInt = new ArrayList[xm.num_tabs];

        for (int i = 0; i < newScopesInt.length; i++) {
            newScopesInt[i] = new ArrayList<>();
        }

        for (var i = 0; i < num_OriTabs; ++i) {
            var arity = xm.tabs.get(i).arity;
            commonVarsBoolean[i] = new boolean[arity];
            for (var j = 0; j < arity; ++j) {
                commonVarsBoolean[i][j] = true;
            }
        }

        //老约束addtionalTabsCache对应id
        tabsIDMatrix = new int[num_OriTabs][num_OriTabs];
        for (var i = 0; i < num_OriTabs; ++i) {
            for (var j = 0; j < num_OriTabs; ++j) {
                tabsIDMatrix[i][j] = -1;
            }
        }
        buildMatrix(true);
        build1DScope();

        //先生成原变量
        vars = new FDEVar[num_vars];
        tabs = new FDETab[num_tabs];

        for (var i = 0; i < num_OriVars; ++i) {
            var v = xm.vars.get(i);
            vars[i] = new FDEVar(v, false);
            max_domain_size = Math.max(max_domain_size, vars[i].size);
        }
        buildNewScope();
        System.out.println(tabs.length);
    }

    //生成公共变量
    private void buildMatrix(boolean mini) {
        if (mini) {
            int[][][] numcc = new int[num_OriVars][num_OriTabs * (num_OriTabs - 1) / 2][3];
            //hash排序约束，[相交变量个数][相交约束组数]约束1约束2标志位3
            int[] numccnum = new int[num_OriVars];            //存储数目
            int maxcc = 0;
            ArrayList<Integer>[][] tabsScopeMatrixMini = new ArrayList[num_OriTabs][num_OriTabs];

            for (int i = 0; i < num_OriVars; i++) {
                numccnum[i] = 0;
            }
            int num = 0;
            for (int i = 0; i < num_OriTabs - 1; i++) {
                var t0 = xm.tabs.get(i);
                for (int j = i + 1; j < num_OriTabs; j++) {
                    num = 0;
                    var t1 = xm.tabs.get(j);
                    tabsScopeMatrix[i][j] = new ArrayList<>();
                    for (int u = 0; u < t0.arity; u++) {
                        for (int v = 0; v < t1.arity; v++) {
                            if (t0.scopeInt[u] == t1.scopeInt[v]) {
                                tabsScopeMatrix[i][j].add(t0.scopeInt[u]);
                                num++;
                                break;
                            }
                        }
                    }
//                    System.out.println(i+"  "+j+"  "+tabsScopeMatrix[i][j]);
                    numcc[num][numccnum[num]][0] = i;
                    numcc[num][numccnum[num]][1] = j;
                    numcc[num][numccnum[num]][2] = 0;
                    numccnum[num]++;
                    if (num > maxcc) {
                        maxcc = num;
                    }
                }
            }
            int c[] = new int[num_OriTabs];       //生成minidual
            int sub = 1;
            for (int i = 0; i < num_OriTabs; i++) {
                c[i] = 0;
            }
            for (int i = maxcc; i > 0; i--) {
                for (int j = 0; j < numccnum[i]; j++) {
                    if (c[numcc[i][j][0]] == 0 && c[numcc[i][j][1]] == 0) {
                        tabsScopeMatrixMini[numcc[i][j][0]][numcc[i][j][1]] = tabsScopeMatrix[numcc[i][j][0]][numcc[i][j][1]];
                        c[numcc[i][j][0]] = sub;
                        c[numcc[i][j][1]] = sub;
                        sub++;
                    } else if (c[numcc[i][j][0]] < c[numcc[i][j][1]]) {
                        tabsScopeMatrixMini[numcc[i][j][0]][numcc[i][j][1]] = tabsScopeMatrix[numcc[i][j][0]][numcc[i][j][1]];
                        if (c[numcc[i][j][0]] != 0) {
                            int nn = c[numcc[i][j][0]];
                            for (int n = 0; n < num_OriTabs; n++) {
                                if (c[n] == nn)
                                    c[n] = c[numcc[i][j][1]];
                            }
                        }
                        c[numcc[i][j][0]] = c[numcc[i][j][1]];
                    } else if (c[numcc[i][j][0]] > c[numcc[i][j][1]]) {
                        tabsScopeMatrixMini[numcc[i][j][0]][numcc[i][j][1]] = tabsScopeMatrix[numcc[i][j][0]][numcc[i][j][1]];
                        if (c[numcc[i][j][1]] != 0) {
                            int nn = c[numcc[i][j][1]];
                            for (int n = 0; n < num_OriTabs; n++) {
                                if (c[n] == nn)
                                    c[n] = c[numcc[i][j][0]];
                            }
                        }
                        c[numcc[i][j][1]] = c[numcc[i][j][0]];
                    } else if (c[numcc[i][j][0]] == c[numcc[i][j][1]]) {//两个约束都在图中检测所涉及元组是否包含在之前所涉及元组中
                        int[][] nextlist = new int[num_OriTabs][2];
                        nextlist[0][0] = numcc[i][j][0];
                        nextlist[0][1] = 0;
                        int nextlistmin = 0;
                        int nextlistmax = 1;
                        boolean bool = false;
                        boolean boolnext = false;
                        ArrayList<Integer> intersect = tabsScopeMatrix[numcc[i][j][0]][numcc[i][j][1]];
                        while (nextlistmin < nextlistmax) {
                            for (int u = maxcc; u >= i; u--) {
                                for (int v = 0; v < numccnum[u]; v++) {
                                    if (u != i || v < j) {
                                        if (numcc[u][v][2] == 0
                                                && (numcc[u][v][0] == nextlist[nextlistmin][0] || numcc[u][v][1] == nextlist[nextlistmin][0])) {
                                            bool = true;
                                            ArrayList<Integer> intersectcheck = tabsScopeMatrix[numcc[u][v][0]][numcc[u][v][1]];
                                            HashSet<Integer> hset = new HashSet<>();
                                            // hset stores all the values of arr1
                                            for (int t = 0; t < intersectcheck.size(); t++) {
                                                hset.add(intersectcheck.get(t));
                                            }
                                            // loop to check if all elements of arr2 also
                                            // lies in arr1
                                            for (int t = 0; t < intersect.size(); t++) {
                                                if (!hset.contains(intersect.get(t))) {
                                                    bool = false;
                                                    break;
                                                }
                                            }
                                        }
                                        int next;
                                        if (bool == true) {
                                            if (numcc[u][v][0] == nextlist[nextlistmin][0])
                                                next = numcc[u][v][1];
                                            else
                                                next = numcc[u][v][0];
                                            boolean b = true;
                                            for (int nextlistnum = 0; nextlistnum < nextlistmax; nextlistnum++) {
                                                if (nextlist[nextlistnum][0] == next) {
                                                    b = false;
                                                    break;
                                                }
                                            }
                                            if (b) {
                                                nextlist[nextlistmax][0] = next;
                                                nextlistmax++;
                                            }
                                            if (numcc[i][j][1] > next ? tabsScopeMatrixMini[next][numcc[i][j][1]] != null : tabsScopeMatrixMini[numcc[i][j][1]][next] != null) {
                                                boolnext = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (boolnext) {
                                    break;
                                }
                            }
                            if (boolnext) {
                                break;
                            }
                            nextlistmin++;
                        }
                        if (boolnext) {
                            numcc[i][j][2] = 1;
                        } else {
                            tabsScopeMatrixMini[numcc[i][j][0]][numcc[i][j][1]] = tabsScopeMatrix[numcc[i][j][0]][numcc[i][j][1]];
                        }
                    }
                }
            }
            tabsScopeMatrix = tabsScopeMatrixMini;
        } else {
            for (int i = 0; i < num_OriTabs - 1; i++) {
                var t0 = xm.tabs.get(i);
                for (int j = i + 1; j < num_OriTabs; j++) {
                    var t1 = xm.tabs.get(j);
                    tabsScopeMatrix[i][j] = new ArrayList<>();
                    for (int u = 0; u < t0.arity; u++) {
                        for (int v = 0; v < t1.arity; v++) {
                            if (t0.scopeInt[u] == t1.scopeInt[v]) {
                                tabsScopeMatrix[i][j].add(t0.scopeInt[u]);
                                break;
                            }
                        }
                    }
                }
            }
        }
//        生成commonVarsBoolean
        for (int i = 0; i < num_OriTabs - 1; i++) {
            var t0 = xm.tabs.get(i);
            for (int j = i + 1; j < num_OriTabs; j++) {
                var t1 = xm.tabs.get(j);
                var tSM = tabsScopeMatrix[i][j];
                //<i, k>, <j, l>：约束i的第k个的变量，约束j的第l个变量
                //因为被 收录入公共 变量 所以以后该被 删去
                //多于1的公共变量，才记入commonVarsBoolean
                if (tSM != null && tSM.size() > 1) {
                    for (var vid : tSM) {
                        commonVarsBoolean[i][t0.getVarIndex(vid)] = false;
                        commonVarsBoolean[j][t1.getVarIndex(vid)] = false;
                    }
                }
            }
        }
    }

    void build1DScope() {

        for (var i = 0; i < num_OriTabs - 1; ++i) {
            for (var j = i + 1; j < num_OriTabs; ++j) {
                var aca = tabsScopeMatrix[i][j];
                if (aca != null) {
                    aca.sort(Integer::compareTo);
                    int k = 0;
                    boolean has = false;
                    while (k < addtionTabsVarScopeArray.size() && !has) {
                        var cc = addtionTabsVarScopeArray.get(k);
                        //如果当前的约束aca与队列中的已存的约束有相同的scope
                        //has标记为true，则跳出
                        //cc在addtionTabsArray位置k存入tabsIDMatrix
                        //cc的CScope被存入队列中
                        //addtionTabsArray与addtionTabsScopeArray的位置是严格对应的
                        if (aca.equals(cc)) {           //顺序可能不同，不能判断是否相等
                            has = true;
                            //k存入matrix
                            tabsIDMatrix[i][j] = k;
                            tabsIDMatrix[j][i] = k;
                            var t2 = new Tuple2<>(i, j);
                            addtionTabsTabScopeArray.get(k).add(t2);
                        }
                        ++k;
                    }

                    //新的，没有same scope的附加约束，将新加此约束 i j 存入标记
                    if (!has && aca.size() > 1) {
                        var t2 = new Tuple2<>(i, j);
                        var tq = new ArrayList<Tuple2<Integer, Integer>>();
                        tq.add(t2);
                        addtionTabsTabScopeArray.add(tq);
                        addtionTabsVarScopeArray.add(aca);
                        var addLength = addtionTabsVarScopeArray.size() - 1;
                        tabsIDMatrix[i][j] = addLength;
                        tabsIDMatrix[j][i] = addLength;
                    }
                }
            }
        }

        //factor variable 个数
        //这个个数 应加在原来变量和约束上
        num_tmp = addtionTabsTabScopeArray.size();
        num_tabs = num_OriTabs + num_tmp;
        num_vars = num_OriVars + num_tmp;
    }

    //确定新的约束范围
    //新约束范围
    //生成AddtionalTabCache
    void buildNewScope() {

        int[][][] newTuples = new int[num_OriTabs][][];
        //遍历旧约束删去公共变量
        for (int i = 0; i < num_OriTabs; ++i) {
            var c = xm.tabs.get(i);
            var scp = c.scopeInt;

            for (var j = 0; j < c.arity; ++j) {
                if (commonVarsBoolean[i][j]) {
                    newScopesInt[i].add(scp[j]);
                }
            }
        }
        for (int i = 0; i < num_tmp; ++i) {
            addtionTabVar(i, addtionTabsVarScopeArray.get(i), addtionTabsTabScopeArray.get(i));        //生成新的变量和约束,并初始化就约束scope
        }
        for (int i = 0; i < num_OriTabs; ++i) {        //newTuples
            XTab c = xm.tabs.get(i);
            newTuples[i] = new int[c.tuples.length][newScopesInt[i].size()];
            XVar[] scp = c.scope;
            int n = 0;
            for (int j = 0; j < scp.length; j++) {
                if (commonVarsBoolean[i][j]) {
                    for (int k = 0; k < c.tuples.length; ++k)
                        newTuples[i][k][n] = c.tuples[k][j];
                    n++;
                }
            }
        }

        for (int i = 0; i < num_tmp; ++i) {
            int vid = i + num_OriVars;
            int tid = i + num_OriTabs;
            HashMap<ArrayList<Integer>, Integer> tupleMap = new HashMap<ArrayList<Integer>, Integer>();
            for (int j = 0; j < tabs[tid].tuples.length; j++) {
                ArrayList<Integer> tuple = new ArrayList<>();
                for (int u = 0; u < tabs[tid].arity - 1; u++) {
                    tuple.add(tabs[tid].tuples[j][u]);
                }
                tupleMap.put(tuple, tabs[tid].tuples[j][tabs[tid].arity - 1]);
            }
            ArrayList<Integer> indexVar = new ArrayList<>();
            HashSet<Integer> c = new HashSet<>();
            for (Tuple2<Integer, Integer> c2 : addtionTabsTabScopeArray.get(i)) {
                if (!c.contains(c2._1)) {
                    int[][] ctuples = xm.tabs.get(c2._1).tuples;
                    for (int j = 0; j < tabs[tid].arity - 1; j++) {
                        int index = tabs[tid].scope[j].id;
                        for (int k = 0; k < xm.tabs.get(c2._1).arity; k++) {
                            if (index == xm.tabs.get(c2._1).scope[k].id) {
                                indexVar.add(k);
                                break;
                            }
                        }

                    }
                    for (int t = 0, end = ctuples.length; t < end; t++) {
                        ArrayList<Integer> tuple = new ArrayList<>();
                        for (int k = 0; k < indexVar.size(); k++) {
                            tuple.add(ctuples[t][indexVar.get(k)]);
                        }
                        int indextuple = tupleMap.get(tuple);
                        newTuples[c2._1][t][newScopesInt[c2._1].indexOf(vid)] = indextuple;
                    }
                    indexVar.clear();
                    c.add(c2._1);
                }
                if (!c.contains(c2._2)) {
                    int[][] ctuples = xm.tabs.get(c2._2).tuples;
                    for (int j = 0; j < tabs[tid].arity - 1; j++) {
                        int index = tabs[tid].scope[j].id;
                        for (int k = 0; k < xm.tabs.get(c2._2).arity; k++) {
                            if (index == xm.tabs.get(c2._2).scope[k].id) {
                                indexVar.add(k);
                                break;
                            }
                        }
                    }
                    for (int t = 0, end = ctuples.length; t < end; t++) {
                        ArrayList<Integer> tuple = new ArrayList<>();
                        for (int k = 0; k < indexVar.size(); k++) {
                            tuple.add(ctuples[t][indexVar.get(k)]);
                        }
                        int indextuple = tupleMap.get(tuple);
                        newTuples[c2._2][t][newScopesInt[c2._2].indexOf(vid)] = indextuple;
                    }
                    indexVar.clear();
                    c.add(c2._2);
                }
            }
        }
        for (int i = 0; i < num_OriTabs; ++i) {
            ArrayList<Integer> a = newScopesInt[i];
            FDEVar[] newScopes = new FDEVar[a.size()];
            for (int j = 0; j < newScopesInt[i].size(); j++) {
                newScopes[j] = vars[a.get(j)];
//                System.out.print(a.get(j)+"  ");
            }
//            System.out.println();
            tabs[i] = new FDETab(i, "", newTuples[i], newScopes);
//            tabs[i].show();
        }
//        xm.tabs.get(406).show();
    }

    public void addtionTabVar(int id, ArrayList<Integer> scope, ArrayList<Tuple2<Integer, Integer>> scopeArray) {
        int arity = scope.size();
        FDEVar[] scopes = new FDEVar[arity + 1];
        for (int i = 0; i < arity; i++) {
            for (int j = 0, end = xm.vars.size(); j < end; j++) {
                if (scope.get(i) == xm.vars.get(j).id) {
                    scopes[i] = vars[j];
                    break;
                }
            }
        }

        HashSet<ArrayList<Integer>> tupleSet = new HashSet<>();
        HashSet<Integer> c = new HashSet<>();
        int[][] tuples;
//        ArrayList<Integer> tuple = new ArrayList<>();
        int indexTab = 0;
        ArrayList<Integer> indexVar = new ArrayList<>();
        for (int i = 0, end = scopeArray.size(); i < end; i++) {
            if (!c.contains(scopeArray.get(i)._1)) {
                newScopesInt[scopeArray.get(i)._1].add(id + num_OriVars);
                indexTab=scopeArray.get(i)._1;
                for (int k = 0; k < arity; k++) {
                    for (int j = 0; j < xm.tabs.get(indexTab).scope.length; j++) {
                        if (scope.get(k) == xm.tabs.get(indexTab).scope[j].id) {
                            indexVar.add(j);
                            break;
                        }
                    }
                }
                for (int[] tu : xm.tabs.get(indexTab).tuples) {
                    ArrayList<Integer> tuple = new ArrayList<>();
                    for (int k = 0; k < arity; k++) {
                        tuple.add(tu[indexVar.get(k)]);
                    }
                    tupleSet.add(tuple);
                }
                c.add(scopeArray.get(i)._1);
                indexVar.clear();
            }
            if (!c.contains(scopeArray.get(i)._2)) {
                newScopesInt[scopeArray.get(i)._2].add(id + num_OriVars);
                indexTab=scopeArray.get(i)._2;
                for (int k = 0; k < arity; k++) {
                    for (int j = 0; j < xm.tabs.get(indexTab).scope.length; j++) {
                        if (scope.get(k) == xm.tabs.get(indexTab).scope[j].id) {
                            indexVar.add(j);
                            break;
                        }
                    }
                }
                for (int[] tu : xm.tabs.get(indexTab).tuples) {
                    ArrayList<Integer> tuple = new ArrayList<>();
                    for (int k = 0; k < arity; k++) {
                        tuple.add(tu[indexVar.get(k)]);
                    }
                    tupleSet.add(tuple);
                }
                c.add(scopeArray.get(i)._2);
                indexVar.clear();
            }
        }
        tuples = new int[tupleSet.size()][arity + 1];
        int nn = 0;
        for (List l : tupleSet) {
            for (int li = 0; li < arity; li++) {
                tuples[nn][li] = (int) l.get(li);
            }
            tuples[nn][arity] = nn++;
        }
        int[] value = new int[tupleSet.size()];
        for (int i = 0; i < tupleSet.size(); i++)
            value[i] = i;
        vars[id + num_OriVars] = new FDEVar(id + num_OriVars, "", value, true);
//        vars[id + num_OriVars].show();
        scopes[arity] = vars[id + num_OriVars];
        tabs[id + num_OriTabs] = new FDETab(id + num_OriTabs, "", tuples, scopes);
//        tabs[id + num_OriTabs].show();
    }

}
