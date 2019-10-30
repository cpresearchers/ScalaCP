package cpscala.TSolver.Model.Solver.AllDifferent;


import cpscala.XModel.XVar;

import java.util.*;


//A Fast Algorithm for Generalized Arc Consistency of the Alldifferent Constraint
//luhan zhen
//2019.9.8

public class AllDifferent_Zhang extends AllDifferent{


    //boolean isSolvable;

    public AllDifferent_Zhang(ArrayList<XVar> XV) {
        super(XV);
        // isSolvable = true;



    }





    private Boolean isReachable(int f, int t, boolean flag) //f -> free ; t -> target
    //判断两个点之间是否存在可达的交替路
    //必须是偶数，因为路径的开始结点和终止结点都是二部图的同一侧（D这一侧）
    //从X->D是匹配的一部分  D->X是非匹配的一部分，二部图变成了有向图，只需要判断有向图是否可达就行了 不再考虑交替路
    //DFS
    {
        if (f == t) //找到了该交替路
            return true;
        boolean ft = false;
        if (flag) { //当前点在D
            for (int i = 0; i < vsize; ++i) {

                if (bipartite[i][f] == 1) {
                        if(isReachable(i, t, false))
                        {
                            ft = true;
                            break;

                        }
//                    var res = isReachable(i, t, false);
//                    System.out.println("check : i = " + i + "  f = " + f + " t = " + t + " res = " + res);
//                    return res;
                }
            }

        } else //当前点在X
        {
            for (int i = 0; i < all_values.size(); ++i) {
                if (bipartite[f][i] == -1) {
                    if(isReachable(i, t, true))
                    {
                        ft = true;
                        break;
                    }
//                    var res = isReachable(i, t, true);
//                    System.out.println("check : i = " + i + "  f = " + f + " t = " + t + " res = " + res);
//                    return res;
//                    return isReachable(i, t, true);
                }
            }

        }
        return ft;

    }

    private ArrayList<Integer> Get_A(ArrayList<Integer> free, ArrayList<Edge> Max_M) {
        HashSet<Integer> A_T = new HashSet<>();   //存到set中，防止出现重复的结点
        for (var f : free) {
            A_T.add(f);
            for (var m : Max_M) {
                if (isReachable(values_to_id.get(f), values_to_id.get(m.V), true)) //对于该匹配，是否存在偶数长度的交替路径使得f和m是可达的
                {
                    A_T.add(m.V);
                   // System.out.println(m.V);
                }


            }

        }

        return new ArrayList<>(A_T); //从hash set到ArrayList

    }



    private ArrayList<Integer> Get_Gamma_A(ArrayList<Integer> A) {
        HashSet<Integer> Gamma_A_T = new HashSet<>();

        for (var a : A) {
            for (int i = 0; i < vsize; ++i) {
                if (bipartite[i][values_to_id.get(a)] != 0)
                    Gamma_A_T.add(i);
            }
        }
        //   ArrayList<Integer> Gamma_A = ArrayList<>(Gamma_A_T);
//        System.out.println("\nGamma_A (neighbor nodes of A):");
//        for(var a : Gamma_A)
//            System.out.print(a + "  ");

        return new ArrayList<>(Gamma_A_T);
    }

    private ArrayList<Integer> Get_Xc_minus_Gamma_A(ArrayList<Integer> Gamma_A) {
        ArrayList<Integer> Xc_minus_Gamma_A = new ArrayList<>();
        for (int i = 0; i < vsize; i++)
            if (!Gamma_A.contains(i))
                Xc_minus_Gamma_A.add(i);

//        System.out.println("\nXc - Gamma_A (rest of X expect  Gamma_A):");
//        for(var a : Xc_minus_Gamma_A)
//            System.out.print(a + "  ");
        return Xc_minus_Gamma_A;

    }

    private ArrayList<Integer> Get_Dc_minus_A(ArrayList<Integer> A) {
        ArrayList<Integer> Dc_minus_A = new ArrayList<>();
        for (var a : all_values)
            if (!A.contains(a))
                Dc_minus_A.add(a);
//        System.out.println("\nDc - A (rest of D expect A):");
//        for(var a : Dc_minus_A)
//            System.out.print(a + "  ");
        return Dc_minus_A;

    }

    private void Prune_all_edge_between_Gamma_A_and_Dc_minus_A(ArrayList<Integer> Gamma_A, ArrayList<Integer> Dc_minus_A) {

        for (var a : Gamma_A) {
            for (var b : Dc_minus_A) {
                if (bipartite[a][values_to_id.get(b)] == 1)
                    bipartite[a][values_to_id.get(b)] = 0;
            }
        }


    }

    private ArrayList<HashSet<Integer>> Get_SCC(ArrayList<Integer> Xc_minus_Gamma_A, ArrayList<Integer> Dc_minus_A) {

        ArrayList<HashSet<Integer>> SCC = new ArrayList<>();
        ArrayList<Edge> All_Egde = new ArrayList<>();
        int sum = Xc_minus_Gamma_A.size() + Dc_minus_A.size();


        for (var a : Xc_minus_Gamma_A) //这里编号是先从X编号完毕再编号D
        {
            for (var b : Dc_minus_A) {
                if (bipartite[a][values_to_id.get(b)] == 1) {

                    //x = values_to_id.get(b)+Xc_minus_Gamma_A.size(), y = a
                    All_Egde.add(new Edge(values_to_id.get(b) + Xc_minus_Gamma_A.size(), a));


                } else if (bipartite[a][values_to_id.get(b)] == -1) {

                    //y = values_to_id.get(b)+Xc_minus_Gamma_A.size(), x = a
                    All_Egde.add(new Edge(a, values_to_id.get(b) + Xc_minus_Gamma_A.size()));


                }
            }
        }
//        System.out.println("123");
//        for (var a : All_Egde) {
//            System.out.println(a.S + "---------->" + a.V);
//        }
        int[] isvisited = new int[sum];
        int[] LOW = new int[sum];
        Stack<Integer> stack = new Stack<>();
        int time = 0;
        for (int i = 0; i < sum; ++i) {
            if (isvisited[i] != 1)
                TarjanAlgorithm(time, stack, All_Egde, SCC, isvisited, LOW, i);
        }
//        for (var a : SCC) {
//            System.out.println();
//            for (var b : a)
//                System.out.print(b + " ");
//        }
        return SCC;
    }

    //递归求强连通分量
    private void TarjanAlgorithm(int time, Stack<Integer> stack, ArrayList<Edge> All_Egde, ArrayList<HashSet<Integer>> SCC, int[] isvisited, int[] LOW, int current) {
        LOW[current] = time++;
        isvisited[current] = 1;
        stack.push(current);
        boolean isRootComponent = true;
        for (var a : All_Egde) {
            if (a.S == current) {
                if (isvisited[a.V] != 1)
                    TarjanAlgorithm(time, stack, All_Egde, SCC, isvisited, LOW, a.V);
                if (LOW[current] > LOW[a.V]) {
                    LOW[current] = LOW[a.V];
                    isRootComponent = false;
                }

            }

        }
        if (isRootComponent) {
            HashSet<Integer> list = new HashSet<>();
            while (true) {
                int p = stack.pop();
                list.add(p);
                LOW[p] = Integer.MAX_VALUE;
                if (p == current)
                    break;
            }
            SCC.add(list);
        }


    }


    private boolean Is_Belong_To_One_SCC(ArrayList<HashSet<Integer>> SCC, int a, int b) {

        for (var i : SCC) {
            if (i.contains(a) && i.contains(b))
                return true;

        }
        return false;

    }

    //剪掉强连通分量之间的边
    private void Prune_all_edge_between_SCC(ArrayList<HashSet<Integer>> SCC, ArrayList<Integer> Xc_minus_Gamma_A, ArrayList<Integer> Dc_minus_A) {

        for (var a : Xc_minus_Gamma_A) //这里编号是先从X编号完毕再编号D
        {
            for (var b : Dc_minus_A) {
                if (bipartite[a][values_to_id.get(b)] == 1) {

                    if (!Is_Belong_To_One_SCC(SCC, a, values_to_id.get(b) + Xc_minus_Gamma_A.size())) {
                        bipartite[a][values_to_id.get(b)] = 0;
                    }


                }
            }
        }
    }

    public boolean Solve() {


        if (!preprocess())  //必然不可解，肯定不用解了，直接返回
            return false;

        try {
            ArrayList<Edge> Max_M = Find_Max_Match();
            //ShowGraph();
            ArrayList<Integer> free = Get_Free_Node(Max_M);

            ArrayList<Integer> A = Get_A(free, Max_M);
            ArrayList<Integer> Gamma_A = Get_Gamma_A(A);
            ArrayList<Integer> Dc_minus_A = Get_Dc_minus_A(A);
            ArrayList<Integer> Xc_minus_Gamma_A = Get_Xc_minus_Gamma_A(Gamma_A);

//            System.out.println("free");
//            for (var e : free)
//                System.out.print(e + "  ");
//
//            System.out.println("A");
//            for (var e : A)
//                System.out.print(e + "  ");
//            System.out.println("Gamma A");
//            for (var e : Gamma_A)
//                System.out.print(e + "  ");
//            System.out.println("Dc_minus_A");
//            for (var e : Dc_minus_A)
//                System.out.print(e + "  ");
//            System.out.println("Xc_minus_Gamma_A");
//            for (var e : Xc_minus_Gamma_A)
//                System.out.print(e + "  ");
//            System.out.println();
            Prune_all_edge_between_Gamma_A_and_Dc_minus_A(Gamma_A, Dc_minus_A);
            //ShowGraph();

            ArrayList<HashSet<Integer>> SCC = Get_SCC(Xc_minus_Gamma_A, Dc_minus_A);
            Prune_all_edge_between_SCC(SCC, Xc_minus_Gamma_A, Dc_minus_A);
            generate_new_var();
            //   ShowGraph();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

}

