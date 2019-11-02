package cpscala.TSolver.Model.Solver.AllDifferent;

import cpscala.XModel.XVar;

import java.util.*;

public class AllDifferent_Zhen extends AllDifferent {

    public AllDifferent_Zhen(ArrayList<XVar> XV) {
        super(XV);
    }


//    ArrayList<Edge> find_redundant_edges(ArrayList<Integer> free) //标记不可被删除的边
//    {
//        ArrayList<Edge> All_Egde = new ArrayList<>();
//        int sum = vsize + all_values.size();
//
//        for(int i = 0 ;i <  vsize;++i) //这里编号是先从X编号完毕再编号D
//        {
//            for(var b : all_values)
//            {
//                if(bipartite[i][values_to_id.get(b)] == -1)
//                {
//
//                    //x = values_to_id.get(b)+Xc_minus_Gamma_A.size(), y = a
//                    All_Egde.add(new Edge(i,values_to_id.get(b)+vsize));
//
//                }
//                else  if(bipartite[i][values_to_id.get(b)] == 1)
//                {
//
//                    //y = values_to_id.get(b)+Xc_minus_Gamma_A.size(), x = a
//
//                    All_Egde.add(new Edge(values_to_id.get(b)+vsize,i));
//
//
//
//                }
//            }
//        }
//
//        for(var a : all_values)
//        {
//            if(free.contains(a))
//                All_Egde.add(new Edge(sum,values_to_id.get(a)+vsize));
//            else
//                All_Egde.add(new Edge(values_to_id.get(a)+vsize,sum));
//        }
//        for(var e: All_Egde)
//            System.out.println(e.S + "------->" + e.V);
//        System.out.println("---------------------------------------------------------");
//
//
//        ArrayList<Edge> edges = new ArrayList<>();
//
//        int[] flag = new int[sum + 1];
//        int i = 0;
//        while(get_sum(flag) != sum + 1) {
//            if(flag[i] == 1)
//                continue;
//
//            Stack<Integer> stack = new Stack<>();
//            flag[i] = 1;
//            stack.push(i);
//
//            while (!stack.isEmpty()) {
//                int top = stack.peek();
//                var neighboor = Get_neighbor(All_Egde,top);
//                for(var n : neighboor)
//                {
//                    if(stack.contains(n))
//                    {
//
//                    edges.add(new Edge(stack.peek(),n));
//                    stack.pop();
//                    while(!stack.isEmpty() && stack.peek() != n)
//                    {
//                        int v = stack.peek();
//                        stack.pop();
//                        int s = stack.peek();
//                        stack.pop();
//                        edges.add(new Edge(s,v));
//
//                    }
//
//                    }
//                    else
//                    {
//                        stack.push(n);
//                        flag[n] = 1;
//                    }
//                }
//
//            }
//        }
//
//
//        for(var e : edges)
//            System.out.println(e.S + "------->" + e.V);
//        return edges;
//
//    }
//
//    ArrayList<Integer> Get_neighbor(ArrayList<Edge> All_Edges,int t)
//    {
//        ArrayList<Integer> r = new ArrayList<>();
//        for(var e : All_Edges)
//        {
//            if(e.S == t)
//                r.add(e.V);
//        }
//        return r;
//
//    }

//    int get_sum(int[] flag)
//    {
//        int sum = 0;
//        for(var a : flag)
//        {
//            sum += a;
//        }
//        return sum;
//    }
//
//
//    void delete_redundant_edges(ArrayList<Edge> edges)
//    {
//
//
//    }


    private ArrayList<Edge> find_redundant_edges() //标记不可被删除的边
    {
        ArrayList<Edge> All_Egde = new ArrayList<>();
        int sum = vsize + all_values.size();
        int[] flag = new int[sum + 1];

        for (int i = 0; i < vsize; ++i) //这里编号是先从X编号完毕再编号D
        {
            for (var b : all_values) {
                if (bipartite[i][values_to_id.get(b)] == -1) {

                    //x = values_to_id.get(b)+Xc_minus_Gamma_A.size(), y = a
                    All_Egde.add(new Edge(i, values_to_id.get(b) + vsize));
                    flag[values_to_id.get(b) + vsize]++;

                } else if (bipartite[i][values_to_id.get(b)] == 1) {

                    //y = values_to_id.get(b)+Xc_minus_Gamma_A.size(), x = a

                    All_Egde.add(new Edge(values_to_id.get(b) + vsize, i));
                    flag[i]++;

                }
            }
        }
//        for(int i = 0; i < vsize;++i)
//        {
//            for(var v : vars.get(i).values_ori) {
//
//                if (!Max_M.contains(new Edge(i,values_to_id.get(v)))) {
//                   // All_Egde.add(new Edge(v.id, values_to_id.get(i) + vsize));
//                    All_Egde.add(new Edge(values_to_id.get(v) + vsize, i));
//                    flag[i]++;
//                }
//            }
//        }
//        for(var m : Max_M)
//        {
//            All_Egde.add(new Edge(m.Start, values_to_id.get(m.End) + vsize));
//             flag[values_to_id.get(m.End) + vsize]++;
//        }

        for (var a : all_values) {
            if (free.contains(a)) {
                All_Egde.add(new Edge(sum, values_to_id.get(a) + vsize));
                flag[values_to_id.get(a) + vsize]++;
            } else {
                All_Egde.add(new Edge(values_to_id.get(a) + vsize, sum));
                flag[sum]++;
            }
        }
        while (Get_Accr(flag) == 0) {

            for (int i = 0; i < flag.length; ++i) {
                if (flag[i] == 0) {
                    flag[i] = -1;

                    for (int j = All_Egde.size() - 1; j > -1; j--) {
                        var e = All_Egde.get(j);
                        if (e.Start == i) {
                            flag[e.End]--;
                            All_Egde.remove(j);
                            if (e.Start > e.End && !(e.Start == vsize + all_values.size() || e.End == vsize + all_values.size()))
                            {
                                bipartite[e.End][e.Start - vsize] = 0;

                            }

                        }
                    }
                }
            }
        }
        return All_Egde;
    }

    private void ReGenerate_Bipartite(ArrayList<Edge> All_Edges, ArrayList<Edge> Max_M) {
        int[][] rebipartite = new int[vars.size()][all_values.size()];
        for (var e : Max_M) {

            rebipartite[e.Start][values_to_id.get(e.End)] = -1;
        }
        for (var e : All_Edges) {
            if (e.Start > e.End && !(e.Start == vsize + all_values.size() || e.End == vsize + all_values.size()))
                rebipartite[e.End][e.Start - vsize] = 1;
        }

        bipartite = rebipartite;

    }

    private int Get_Accr(int[] flag) {
        int s = 1;
        for (var e : flag)
            s *= e;
        return s;
    }


    public boolean Solve() {


        if (preprocess())  //必然不可解，肯定不用解了，直接返回
            return false;

        try {
          //  Find_Max_Match();
           // Get_Free_Node();
            var edges = find_redundant_edges();
            //find_redundant_edges(Get_Free_Node(Find_Max_Match()));
            // ShowGraph();
           // ReGenerate_Bipartite(edges, Max_M);
            // ShowGraph();
            generate_new_var();
            //   ShowGraph();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }


}
