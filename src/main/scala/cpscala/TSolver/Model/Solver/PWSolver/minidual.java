package cpscala.TSolver.Model.Solver.PWSolver;

import cpscala.XModel.XModel;
import java.util.ArrayList;
import java.util.HashSet;

public class minidual {

    ArrayList<Integer>[][] tabsScopeMatrix;

    minidual(XModel xm, boolean mini){
        int num_OriVars=xm.num_vars;
        int num_OriTabs=xm.num_tabs;
        tabsScopeMatrix = new ArrayList[num_OriTabs][num_OriTabs];

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
                                    if (u == i && v >= j) {
                                        break;
                                    }
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
//                                        System.out.println(numcc[i][j][1]+"   "+next);
//                                        System.out.println("------------------------------------------");
//                                        for(int nn=0;nn<num_OriTabs-1;nn++)
//                                            for(int nnn=nn;nnn<num_OriTabs;nnn++)
//                                                System.out.println(nn+"  "+nnn+"  "+tabsScopeMatrixMini[nn][nnn]);
//                                        System.out.println("------------------------------------------");

                                        if (numcc[i][j][1] > next ? tabsScopeMatrixMini[next][numcc[i][j][1]] != null : tabsScopeMatrixMini[numcc[i][j][1]][next] != null) {
                                            boolnext = true;
                                            break;
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
    }

}
