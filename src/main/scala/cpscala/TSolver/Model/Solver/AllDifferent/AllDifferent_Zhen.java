package cpscala.TSolver.Model.Solver.AllDifferent;

import cpscala.XModel.XVar;
import scala.Int;

import java.util.*;

public class AllDifferent_Zhen extends AllDifferent{

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



    public boolean Solve()
    {


        if(!preprocess())  //必然不可解，肯定不用解了，直接返回
            return  false;

        try {
            ArrayList<Edge> Max_M = Find_Max_Match();
            ArrayList<Integer> free = Get_Free_Node(Max_M);


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
