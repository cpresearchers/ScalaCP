package cpscala.TSolver.Model.Solver.AllDifferent;

import cpscala.XModel.XVar;
import scala.Int;

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


    ArrayList<Edge> find_redundant_edges(ArrayList<Integer> free) //标记不可被删除的边
    {
        ArrayList<Edge> All_Egde = new ArrayList<>();
        int sum = vsize + all_values.size();
        int [] flag = new int[sum+1];

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

        for (var a : all_values) {
            if (free.contains(a))
            {
                All_Egde.add(new Edge(sum, values_to_id.get(a) + vsize));
                flag[values_to_id.get(a) + vsize]++;
            }
            else
            {
                All_Egde.add(new Edge(values_to_id.get(a) + vsize, sum));
                flag[sum]++;
            }
        }
//        for (var e : All_Egde)
//            System.out.println(e.S + "------->" + e.V);
//        System.out.println("---------------------------------------------------------");
//        for (var e : flag)
//            System.out.print(e + " ");
//        System.out.println("---------------------------------------------------------");

        while(Get_Accr(flag) == 0)
        {

            for(int i = 0;i < flag.length;++i)
            {
                if(flag[i] == 0)
                {
                    flag[i] = -1;
                  //  int []f = new int[All_Egde.size()];
                    for(int j = All_Egde.size() - 1; j > -1;j--){
                        var e = All_Egde.get(j);
                        if (e.S == i) {
                            flag[e.V]--;
                            All_Egde.remove(j);
                            //f[j] = 1;

                        }
                    }
//                    for(int j = f.length - 1; j > -1;j--)
//                    {
//                        if(f[j] == 1)
//                            All_Egde.remove(j);
//                    }
                }
            }
        }

//        for(int j = All_Egde.size() - 1; j > -1;j--)
//        {
//            if(All_Egde.get(j).S == sum || sum == All_Egde.get(j).V)
//                All_Egde.remove(j);
//        }

//        for (var e : All_Egde)
//            System.out.println(e.S + "------->" + e.V);
//        System.out.println("---------------------------------------------------------");

        return All_Egde;
    }

    void ReGenerate_Bipartite(ArrayList<Edge> All_Edges,ArrayList<Edge> Max_M)
    {
        int [][] rebipartite = new int[vars.size()][all_values.size()];
        for(var e : Max_M)
        {

            rebipartite[e.S][values_to_id.get(e.V)] = -1;
        }
//        System.out.println("regraph:");
//        for (var a : rebipartite) {
//            for (var b : a) {
//                System.out.print(b + " ");
//            }
//            System.out.println();
//        }
        for(var e : All_Edges)
        {
            if(e.S > e.V && !(e.S == vsize + all_values.size() ||  e.V == vsize + all_values.size()))
                rebipartite[e.V][e.S - vsize] = 1;
        }

        bipartite = rebipartite;
//        for (int j = 0; j < bipartite.length; ++j) {
//            for (int i = 0; i < bipartite[j].length; ++i) {
//                if (bipartite[j][i] == -1)
//                    rebipartite[j][i] = 1;
//            }
//        }



    }

    private int Get_Accr(int[] flag)
    {
        int s = 1;
        for(var e : flag)
            s *= e;
        return s;
    }


    public boolean Solve()
    {


        if(!preprocess())  //必然不可解，肯定不用解了，直接返回
            return  false;

        try {
            ArrayList<Edge> Max_M = Find_Max_Match();
            ArrayList<Integer> free = Get_Free_Node(Max_M);
            var edges = find_redundant_edges(free);
           // ShowGraph();
            ReGenerate_Bipartite(edges,Max_M);
           // ShowGraph();
            generate_new_var();
            //   ShowGraph();
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }


        return true;
    }




}
