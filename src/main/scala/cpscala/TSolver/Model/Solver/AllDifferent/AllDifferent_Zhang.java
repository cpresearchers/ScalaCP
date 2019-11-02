package cpscala.TSolver.Model.Solver.AllDifferent;


import cpscala.XModel.XVar;


import java.util.*;


//A Fast Algorithm for Generalized Arc Consistency of the Alldifferent Constraint
//luhan zhen
//2019.9.8

public class AllDifferent_Zhang extends AllDifferent {


    //boolean isSolvable;
    private HashSet<Integer> A;
    private HashSet<Integer> Gamma_A;
    private HashSet<Integer> Xc_minus_Gamma_A;
    private HashSet<Integer> Dc_minus_A;

    public AllDifferent_Zhang(ArrayList<XVar> XV) {
        super(XV);
        // isSolvable = true;

        A = new HashSet<>();
        Gamma_A = new HashSet<>();
        Xc_minus_Gamma_A = new HashSet<>();
        Dc_minus_A = new HashSet<>();

    }


    private Boolean isReachable(int f, int t) //f -> free ; t -> target
    //判断两个点之间是否存在可达的交替路
    //必须是偶数，因为路径的开始结点和终止结点都是二部图的同一侧（D这一侧）
    //从X->D是匹配的一部分  D->X是非匹配的一部分，二部图变成了有向图，只需要判断有向图是否可达就行了 不再考虑交替路
    {
        Stack<Integer> stack = new Stack<>();
        int[] visited = new int[vsize + all_values.size()];
        f = f + vsize;
        t = t + vsize;
        stack.push(f);
        visited[f] = 1;
        while (!stack.isEmpty()) {

            int peek = stack.peek();
            if (peek < vsize) {
                for (var m : Max_M)
                    if (m.Start == peek) {
                        int p = values_to_id.get(m.End) + vsize;
                        if (t == p)
                            return true;
                        if (visited[p] == 0) {
                            stack.push(p);
                            visited[p] = 1;
                        } else
                            stack.pop();

                        break;
                    }
            } else {
                boolean flag = false;
                for (int i = 0; i < vsize; ++i) {
                    if (bipartite[i][peek - vsize] == 1) {
                        if (i == t)
                            return true;
                        if (visited[i] == 0) {
                            stack.push(i);
                            visited[i] = 1;
                            flag = true;
                        }
                    }

                }
                if (!flag) {
                    stack.pop();
                }

            }

        }
        return false;
    }


//            var n = Get_Neight(stack.peek());
//            boolean flag = false;
//            for(var i : n)
//            {
//                if(i == t)
//                    return true;
//                if(visited[i] == 0)
//                {
//                    stack.push(i);
//                    visited[i] = 1;
//                    flag = true;
//                }
//            }
//            if(!flag)
//            {
//                stack.pop();
//            }
//    ArrayList<Integer> Get_Neight(int o)
//    {
//        ArrayList<Integer> t =new ArrayList<>();
//        if(o < vsize) {
//            for (var m : Max_M)
//                if (m.Start == o && bipartite[o][values_to_id.get(m.End)] == -1) {
//                    t.add(values_to_id.get(m.End) + vsize);
//                    break;
//                }
//        }
//        else
//        {
//            for(int i = 0 ;i < vsize; ++i)
//            {
//                if(bipartite[i][o-vsize] == 1)
//                    t.add(i);
//            }
//        }
//
//        return t;
//
//    }

//    private Boolean isReachable(int f, int t, boolean flag) //f -> free ; t -> target
//    //判断两个点之间是否存在可达的交替路
//    //必须是偶数，因为路径的开始结点和终止结点都是二部图的同一侧（D这一侧）
//    //从X->D是匹配的一部分  D->X是非匹配的一部分，二部图变成了有向图，只需要判断有向图是否可达就行了 不再考虑交替路
//    //DFS
//    {
//        if (f == t) //找到了该交替路
//            return true;
//        boolean ft = false;
//        if (flag) { //当前点在D
//            for (int i = 0; i < vsize; ++i) {
//
//                if (bipartite[i][f] == 1) {
//                        if(isReachable(i, t, false))
//                        {
//                            ft = true;
//                            break;
//
//                        }
////                    var res = isReachable(i, t, false);
////                    System.out.println("check : i = " + i + "  f = " + f + " t = " + t + " res = " + res);
////                    return res;
//                }
//            }
//
//        } else //当前点在X
//        {
//            for (int i = 0; i < all_values.size(); ++i) {
//                if (bipartite[f][i] == -1) {
//                    if(isReachable(i, t, true))
//                    {
//                        ft = true;
//                        break;
//                    }
////                    var res = isReachable(i, t, true);
////                    System.out.println("check : i = " + i + "  f = " + f + " t = " + t + " res = " + res);
////                    return res;
////                    return isReachable(i, t, true);
//                }
//            }
//
//        }
//        return ft;
//
//    }

    private void Get_A() {
        //存到set中，防止出现重复的结点
        A.clear();
        for (var f : free) {
            A.add(values_to_id.get(f));
            for (var m : Max_M) {
                if (isReachable(values_to_id.get(f), values_to_id.get(m.End))) //对于该匹配，是否存在偶数长度的交替路径使得f和m是可达的
                {
                    A.add(values_to_id.get(m.End));
                    // System.out.println(m.V);
                }


            }

        }

    }


    private void Get_Gamma_A() {

        Gamma_A.clear();
        for (var a : A) {
            for (int i = 0; i < vsize; ++i) {
                if (bipartite[i][a] != 0)
                    Gamma_A.add(i);
            }
        }
        //   ArrayList<Integer> Gamma_A = ArrayList<>(Gamma_A_T);
//        System.out.println("\nGamma_A (neighbor nodes of A):");
//        for(var a : Gamma_A)
//            System.out.print(a + "  ");


    }

    private void Get_Xc_minus_Gamma_A() {
        Xc_minus_Gamma_A.clear();
        for (int i = 0; i < vsize; i++)
            if (!Gamma_A.contains(i))
                Xc_minus_Gamma_A.add(i);

//        System.out.println("\nXc - Gamma_A (rest of X expect  Gamma_A):");
//        for(var a : Xc_minus_Gamma_A)
//            System.out.print(a + "  ");


    }

    private void Get_Dc_minus_A() {
        Dc_minus_A.clear();
        for (var a : all_values)
            if (!A.contains(values_to_id.get(a)))
                Dc_minus_A.add(values_to_id.get(a));
//        System.out.println("\nDc - A (rest of D expect A):");
//        for(var a : Dc_minus_A)
//            System.out.print(a + "  ");


    }

    private void Prune_all_edge_between_Gamma_A_and_Dc_minus_A() {

        for (var a : Gamma_A) {
            for (var b : Dc_minus_A) {
                if (bipartite[a][b] == 1)
                    bipartite[a][b] = 0;
            }
        }


    }

    private ArrayList<HashSet<Integer>> Get_SCC() {

        ArrayList<HashSet<Integer>> SCC = new ArrayList<>();
        ArrayList<Edge> All_Egde = new ArrayList<>();
        int sum = Xc_minus_Gamma_A.size() + Dc_minus_A.size();

        int i = 0, j = 0;
        for (var a : Xc_minus_Gamma_A) //这里编号是先从X编号完毕再编号D
        // for(int i = 0; i < Xc_minus_Gamma_A.size();++i)
        {
            for (var b : Dc_minus_A) {
                //for(int  j = 0; j < Dc_minus_A.size();++j)
                if (bipartite[a][b] == 1) {

                    //x = values_to_id.get(b)+Xc_minus_Gamma_A.size(), y = a
                    All_Egde.add(new Edge(j + Xc_minus_Gamma_A.size(), i));


                } else if (bipartite[a][b] == -1) {

                    //y = values_to_id.get(b)+Xc_minus_Gamma_A.size(), x = a
                    All_Egde.add(new Edge(i, j + Xc_minus_Gamma_A.size()));


                }
                j++;
            }
            i++;
            j = 0;
        }

        int[] visited = new int[sum];
        int[] LOW = new int[sum];
        Stack<Integer> stack = new Stack<>();
        int time = 0;
        for (i = 0; i < sum; ++i) {
            if (visited[i] != 1)
                TarjanAlgorithm(time, stack, All_Egde, SCC, visited, LOW, i);
        }

        return SCC;
    }

    //递归求强连通分量
    private void TarjanAlgorithm(int time, Stack<Integer> stack, ArrayList<Edge> All_Egde, ArrayList<HashSet<Integer>> SCC, int[] visited, int[] LOW, int current) {
        LOW[current] = time++;
        visited[current] = 1;
        stack.push(current);
        boolean isRootComponent = true;
        for (var a : All_Egde) {
            if (a.Start == current) {
                int end = a.End;
                if (visited[end] != 1)
                    TarjanAlgorithm(time, stack, All_Egde, SCC, visited, LOW, end);
                if (LOW[current] > LOW[end]) {
                    LOW[current] = LOW[end];
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
    private void Prune_all_edge_between_SCC(ArrayList<HashSet<Integer>> SCC) {

        for (var a : Xc_minus_Gamma_A) //这里编号是先从X编号完毕再编号D
        {
            for (var b : Dc_minus_A) {
                if (bipartite[a][b] == 1) {

                    if (!Is_Belong_To_One_SCC(SCC, a, b + Xc_minus_Gamma_A.size())) {
                        bipartite[a][b] = 0;
                    }


                }
            }
        }
    }

    public boolean Solve() {


        if (preprocess())  //必然不可解，肯定不用解了，直接返回
            return false;

        try {
            //  Find_Max_Match();
            //ShowGraph();
            //Get_Free_Node();

            Get_A();
            Get_Gamma_A();
            Get_Dc_minus_A();
            Get_Xc_minus_Gamma_A();
//
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
            Prune_all_edge_between_Gamma_A_and_Dc_minus_A();
            //ShowGraph();

            ArrayList<HashSet<Integer>> SCC = Get_SCC();
            Prune_all_edge_between_SCC(SCC);
            generate_new_var();
            //   ShowGraph();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

}

