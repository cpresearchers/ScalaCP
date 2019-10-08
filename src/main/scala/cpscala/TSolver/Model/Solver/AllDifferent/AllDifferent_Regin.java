package cpscala.TSolver.Model.Solver.AllDifferent;

import cpscala.XModel.XVar;

import java.util.*;



//A filtering algorithm for constraints of difference in CSPs -Regin 1994
//luhan zhen
//2019.9.26
//增加一个点后求全部的SCC，剪去SCC之间的边即可
public class AllDifferent_Regin {
    private static class Edge { //边
        int S,V;

        Edge(int s, int v) {S = s;V = v; }
    }

    private ArrayList<XVar> vars;
    private ArrayList<XVar> after_vars;
    //int[] ori_solution;
    //int[] mapped_solution;
    private int vsize;
    private int [][] bipartite;

    private ArrayList<Integer> all_values;
    private HashMap<Integer,Integer> values_to_id; //值到id的映射


    //boolean isSolvable;

    public AllDifferent_Regin(ArrayList<XVar> XV)
    {
        // isSolvable = true;
        vars = XV;
        vsize = XV.size();
        HashSet<Integer> all = new HashSet<>(); //将所有的取值扔到set里面
        for(var a: vars)
            for (var b :a.values_ori)
                all.add(b);

        all_values = new ArrayList<>(all); //所有的取值拿到list中
        values_to_id = new HashMap<>();
        for(int i = 0; i < all_values.size();++i)
        {
            values_to_id.put(all_values.get(i),i);
        }
        Generate_Bipartite();

    }

    private  boolean Hungary_Algorithm(int[] visited,int[] s_v,int[] s_x,int x) //匈牙利算法递归求增广路径
    {
        for(int i = 0; i < s_v.length;++i)
        {
            if(bipartite[x][i] == 1 && visited[i] == 0)
            {
                visited[i] = 1;
                if(s_v[i] == -1 || Hungary_Algorithm(visited,s_v,s_x,s_v[i]))
                {
                    s_x[x] = i;
                    s_v[i] = x;
                    return true;
                }
            }
        }
        return false;

    }


    private ArrayList<Edge> Find_Max_Match() //匈牙利算法求最大匹配
    {
        ArrayList<Edge> Max_M = new ArrayList<>();

        int[] s_x = new int[vsize];
        int[] s_v = new int[all_values.size()];
        Arrays.fill(s_x,-1);
        Arrays.fill(s_v,-1);

        int[] visited = new int[all_values.size()];
        for(int i = 0; i < vsize;++i)
        {

            Arrays.fill(visited,0);

            Hungary_Algorithm(visited,s_v,s_x,i);

        }

        for (int i = 0; i < s_x.length;++i)
            Max_M.add(new Edge(i,all_values.get(s_x[i])));
        for(var m: Max_M) //将图中匹配置为-1
        {
            bipartite[m.S][values_to_id.get(m.V)] = -bipartite[m.S][values_to_id.get(m.V)];
        }

        return Max_M;

    }

    private void Generate_Bipartite() //生成二部图
    {
        bipartite = new int[vars.size()][all_values.size()];
        for (int j = 0; j < vars.size() ;++j)
            for (var b :vars.get(j).values_ori) {
                bipartite[j][values_to_id.get(b)] =1;
            }

    }


    private ArrayList<Integer> Get_Free_Node(ArrayList<Edge> M)
    {
        ArrayList<Integer> free = new ArrayList<>();
        for(var a: all_values)
        {
            boolean f = false;
            for(var b : M)
            {
                if(a == b.V)
                {
                    f = true;
                    break;
                }
            }
            if(!f)
            {
                free.add(a);
            }

        }

        return free;
    }

    private boolean preprocess()  //预处理 用于检测那些论域为空的变量和论域仅仅只有一个值的变量
    {
        if(vsize > all_values.size())//变量的个数少于值的个数，必然无解
        {

            return false;
        }
        // System.out.println("aa");
        for(int i = 0; i < vsize;++i) //存在某个变量的论域为空 必然无解
        {
            if(vars.get(i).values_ori.length == 0)
            {
                return false;
            }
            //if(vars.get(i).values_ori.length == 1)
            //    ori_solution[i] = vars.get(i).values_ori[0];
        }
        return true;
    }

private ArrayList<HashSet<Integer>> Get_SCC(ArrayList<Integer> free)
{
    ArrayList<HashSet<Integer>> SCC = new ArrayList<>();
    ArrayList<Edge> All_Egde = new ArrayList<>();
    int sum = vsize + all_values.size();


    for(int i = 0 ;i <  vsize;++i) //这里编号是先从X编号完毕再编号D
    {
        for(var b : all_values)
        {
            if(bipartite[i][values_to_id.get(b)] == 1)
            {

                //x = values_to_id.get(b)+Xc_minus_Gamma_A.size(), y = a
                All_Egde.add(new Edge(values_to_id.get(b)+vsize,i));


            }
            else  if(bipartite[i][values_to_id.get(b)] == -1)
            {

                //y = values_to_id.get(b)+Xc_minus_Gamma_A.size(), x = a
                All_Egde.add(new Edge(i,values_to_id.get(b)+vsize));


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

   /* System.out.println(All_Egde.size());
    for(var a : All_Egde)
        System.out.println(a.S + " - " + a.V);
    System.out.println();*/

    // System.out.println(sum);
    //  for (var a : All_Egde)
    //   System.out.print(a.S + "->" + a.V + "   ");
    int[] isvisited = new int[sum+1];
    int[] LOW = new int[sum+1];
    Stack<Integer> stack = new Stack<>();

    for(int i =0; i < sum+1; ++i)
    {
        if(isvisited[i] != 1)
            TarjanAlgorithm(0,stack,All_Egde,SCC,isvisited,LOW,i);
    }
  /*  for(var a : SCC)
    {
        for(var b : a)
            System.out.print(b + "  ");
        System.out.println();
    }*/
    return SCC;
}
    /*
    private ArrayList<HashSet<Integer>> Get_SCC(ArrayList<Integer> Xc_minus_Gamma_A,ArrayList<Integer> Dc_minus_A)
    {

        ArrayList<HashSet<Integer>> SCC = new ArrayList<>();
        ArrayList<Edge> All_Egde = new ArrayList<>();
        int sum = Xc_minus_Gamma_A.size() + Dc_minus_A.size();


        for(var a : Xc_minus_Gamma_A) //这里编号是先从X编号完毕再编号D
        {
            for(var b : Dc_minus_A)
            {
                if(bipartite[a][values_to_id.get(b)] == 1)
                {

                    //x = values_to_id.get(b)+Xc_minus_Gamma_A.size(), y = a
                    All_Egde.add(new Edge(values_to_id.get(b)+Xc_minus_Gamma_A.size(),a));


                }
                else  if(bipartite[a][values_to_id.get(b)] == -1)
                {

                    //y = values_to_id.get(b)+Xc_minus_Gamma_A.size(), x = a
                    All_Egde.add(new Edge(a,values_to_id.get(b)+Xc_minus_Gamma_A.size()));


                }
            }
        }
        // System.out.println(sum);
        //  for (var a : All_Egde)
        //   System.out.print(a.S + "->" + a.V + "   ");
        int[] isvisited = new int[sum];
        int[] LOW = new int[sum];
        Stack<Integer> stack = new Stack<>();
        int time = 0;
        for(int i =0; i < sum; ++i)
        {
            if(isvisited[i] != 1)
                TarjanAlgorithm(time,stack,All_Egde,SCC,isvisited,LOW,i);
        }
        // for(var a : SCC)
        //  {
        //   System.out.println();
        //    for(var b:a)
        //     System.out.print(b + " ");
        //   }
        return SCC;
    }*/
    //递归求强连通分量
    private void TarjanAlgorithm(int time,Stack<Integer> stack ,ArrayList<Edge> All_Egde,ArrayList<HashSet<Integer>> SCC,int[] isvisited,int[] LOW,int current )
    {
        LOW[current] = time++;
        isvisited[current] = 1;
        stack.push(current);
        boolean isRootComponent = true;
        for(var a : All_Egde)
        {
            if(a.S == current)
            {
                if(isvisited[a.V] != 1)
                    TarjanAlgorithm(time,stack,All_Egde,SCC,isvisited,LOW,a.V);
                if(LOW[a.V] < LOW[current])
                {
                     LOW[current] = LOW[a.V];
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
    /*private void TarjanAlgorithm(int time,Stack<Integer> stack ,ArrayList<Edge> All_Egde,ArrayList<HashSet<Integer>> SCC,int[] isvisited,int[] LOW,int current )
    {
        LOW[current] = time++;
        isvisited[current] = 1;
        stack.push(current);
        boolean isRootComponent = true;
        for(var a : All_Egde)
        {
            if(a.S == current)
            {
                if(isvisited[a.S] != 1)
                    TarjanAlgorithm(time,stack,All_Egde,SCC,isvisited,LOW,a.S);
                if(LOW[a.S] > LOW[current])
                {
                    LOW[a.S] = LOW[current];
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


    }*/


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


        if(!preprocess())  //必然不可解，肯定不用解了，直接返回
            return  false;

        try {
            ArrayList<Edge> Max_M = Find_Max_Match();
            ArrayList<Integer> free = Get_Free_Node(Max_M);


            ArrayList<HashSet<Integer>> SCC = Get_SCC(free);
            Prune_all_edge_between_SCC(SCC);
            //   ShowGraph();
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        after_vars = new ArrayList<>();
        for(int i = 0;i < vsize;++i)
        {
            ArrayList<Integer> values = new ArrayList<>();
            for(int j = 0; j < vars.get(i).values_ori.length;++j)
            {
                if(bipartite[i][values_to_id.get(vars.get(i).values_ori[j])] != 0)
                {
                    values.add(vars.get(i).values_ori[j]);
                }

            }
            int[] v = new int[values.size()];
            for (int k = 0; k < values.size();++k)
                v[k] = values.get(k);


            after_vars.add(new XVar(vars.get(i).id,vars.get(i).name,v));

        }
        return true;
    }


    public ArrayList<XVar> get_Var()
    {
        return after_vars;
    }

    private  void ShowGraph()
    {
        System.out.println("graph:");
        for(var a : bipartite)
        {
            for (var b:a)
            {
                System.out.print(b + " ");
            }
            System.out.println();
        }
    }
    public void show()
    {
        for(var a: all_values)
            System.out.print(a + " ");
        System.out.println();

        for(var a : vars)
            a.show();
        ShowGraph();

        System.out.println();
    }

}

