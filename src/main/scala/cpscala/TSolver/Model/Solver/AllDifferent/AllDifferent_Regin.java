package cpscala.TSolver.Model.Solver.AllDifferent;

import cpscala.XModel.XVar;

import java.util.*;



//A filtering algorithm for constraints of difference in CSPs -Regin 1994
//luhan zhen
//2019.9.26
//增加一个点后求全部的SCC，剪去SCC之间的边即可
public class AllDifferent_Regin extends AllDifferent{


    //boolean isSolvable;

    public AllDifferent_Regin(ArrayList<XVar> XV)
    {
        super(XV);
        // isSolvable = true;


    }




    private ArrayList<HashSet<Integer>> Get_SCC()
    {
    ArrayList<HashSet<Integer>> SCC = new ArrayList<>();
    ArrayList<Edge> All_Egde = new ArrayList<>();
    int sum = vsize + all_values.size();

    for(int i = 0 ;i <  vsize;++i) //这里编号是先从X编号完毕再编号D
    {
        for(var b : all_values)
        {
            if(bipartite[i][values_to_id.get(b)] == -1)
            {

                //x = values_to_id.get(b)+Xc_minus_Gamma_A.size(), y = a
                All_Egde.add(new Edge(i,values_to_id.get(b)+vsize));

            }
            else  if(bipartite[i][values_to_id.get(b)] == 1)
            {

                //y = values_to_id.get(b)+Xc_minus_Gamma_A.size(), x = a

                All_Egde.add(new Edge(values_to_id.get(b)+vsize,i));



            }
        }
    }

    for(var a : all_values)
    {
        if(free.contains(a))
            All_Egde.add(new Edge(sum,values_to_id.get(a)+vsize));
        else
            All_Egde.add(new Edge(values_to_id.get(a)+vsize,sum));
    }

    //System.out.println(All_Egde.size());
//    for(var a : All_Egde)
//    {
//        System.out.println(a.S + "---------->" + a.V);
//    }

    //   System.out.print(a.S + "->" + a.V + "   ");
    int[] isvisited = new int[sum+1];
    int[] LOW = new int[sum+1];
    int time = 0;
    Stack<Integer> stack = new Stack<>();

    for(int i =0; i < sum+1; ++i)
    {
        if(isvisited[i] != 1)
            TarjanAlgorithm(time,stack,All_Egde,SCC,isvisited,LOW,i);
    }
//    for(var a : SCC)
//    {
//        for(var b : a)
//            System.out.print(b + "  ");
//        System.out.println();
//    }
    return SCC;
}

    //递归求强连通分量
    private void TarjanAlgorithm(int time,Stack<Integer> stack ,ArrayList<Edge> All_Egde,ArrayList<HashSet<Integer>> SCC,int[] isvisited,int[] LOW,int current )
    {
        LOW[current] = time++;
        isvisited[current] = 1;
        stack.push(current);
        boolean isRootComponent = true;
        for(var a : All_Egde)
        {
            if(a.Start == current)
            {
                if(isvisited[a.End] != 1)
                    TarjanAlgorithm(time,stack,All_Egde,SCC,isvisited,LOW,a.End);
                if(LOW[current] > LOW[a.End])
                {
                    LOW[current] = LOW[a.End] ;
                    isRootComponent = false;
                }

            }

        }
        if(isRootComponent)
        {
            HashSet<Integer> list = new HashSet<>();
            while (true)
            {
                int p = stack.pop();
                list.add(p);
                LOW[p] = Integer.MAX_VALUE;
                if(p == current)
                    break;
            }
            SCC.add(list);
        }


    }

    private boolean Is_Belong_To_One_SCC(ArrayList<HashSet<Integer>> SCC,int a,int b)
    {

        for (var i : SCC)
        {
            if(i.contains(a) && i.contains(b))
                return true;

        }
        return false;

    }
    //剪掉强连通分量之间的边
    private void Prune_all_edge_between_SCC(ArrayList<HashSet<Integer>> SCC)
    {

        for(int i = 0; i < vsize;++i) //这里编号是先从X编号完毕再编号D
        {
            for (var b : all_values) {
                if (bipartite[i][values_to_id.get(b)] == 1) {

                    if(!Is_Belong_To_One_SCC(SCC, i, values_to_id.get(b) + vsize))
                    {
                        bipartite[i][values_to_id.get(b)] = 0;
                    }


                }
            }
        }
    }

    public boolean Solve()
    {


        if(preprocess())  //必然不可解，肯定不用解了，直接返回
            return  false;

        try {
        //    Find_Max_Match();
           // Get_Free_Node();


            ArrayList<HashSet<Integer>> SCC = Get_SCC();
            Prune_all_edge_between_SCC(SCC);

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

