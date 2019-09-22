package cpscala.TSolver.Model.Solver.AllDifferent;



import cpscala.XModel.XVar;

import java.util.*;


//A Fast Algorithm for Generalized Arc Consistency of the Alldifferent Constraint
//luhan zhen
//2019.9.8

public class AllDifferent {
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

    public AllDifferent(ArrayList<XVar> XV)
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

//        System.out.println("max match:");
//        for(var a : Max_M)
//       {
//
//            System.out.println(a.S +"----" + a.V);
//
//
//        }
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
//        System.out.print("\nfree node: ");
//        for(var a : free)
//            System.out.print(a + "  ");
//        System.out.println();
        return free;
    }

    private Boolean isReachable(int f,int t,boolean flag) //f -> free ; t -> target
    //判断两个点之间是否存在可达的交替路
    //必须是偶数，因为路径的开始结点和终止结点都是二部图的同一侧（D这一侧）
    //从X->D是匹配的一部分  D->X是非匹配的一部分，二部图变成了有向图，只需要判断有向图是否可达就行了 不再考虑交替路
    //BFS
    {
        if(f == t) //找到了该交替路
            return true;
        if(flag) { //当前点在D
            for (int i = 0; i < vsize;++i)
            {
                if(bipartite[i][f] == 1)
                {
                    return isReachable(i,t,false);
                }
            }
        }
        else //当前点在X
        {
            for(int i = 0; i < all_values.size();++i)
            {
                if(bipartite[f][i] == -1)
                {
                    return isReachable(i,t,true);
                }
            }

        }
        return false;

        /*System.out.println("free: " + f + "  target: " + t);
        Stack<Edge> stack = new Stack<>();
        int [][] visited = new int[vars.size()][all_values.size()];
        for (int i = 0;i < vsize;++i) {
            if (bipartite[i][f] != 0) {
            //    System.out.println("first S: " + i + "  V: " + f);
                visited[i][f] = 1;
                stack.push(new Edge(i,f)); //加入第一条边到栈中
                break;
            }
        }
        boolean flag_V_D = true; //标记当前需要寻找那个边的点 true -> D false -> V
        int num = 0;
        while(!stack.isEmpty())
        {
            if(num > 10)
            break;
          //  System.out.println("stack size is : " + stack.size());

            Edge E = stack.peek();
            if(E.V == t) //找到了该交替路
                return true;
           // System.out.println("G: " + bipartite[E.S][E.V] + "  S: " + E.S + " V: " + E.V);
            assert bipartite[E.S][E.V] != 0;
           // if(bipartite[E.S][E.V] == 1)
             //   System.out.println("unmatched");
           // else
                //System.out.println("matched");
            boolean flag = false;
            if(flag_V_D == true) {
                for (int i = 0; i < all_values.size(); ++i) {
                    if (bipartite[E.S][i] == -bipartite[E.S][E.V] && visited[E.S][i] == 0) {
                        visited[E.S][i] = 1;
                        stack.push(new Edge(E.S, i));
                        num++;
                        flag = true;
                        flag_V_D = false;
                        break;
                    }
                }
            }
            else
            {
                for (int i = 0; i < vsize; ++i) {
                    if (bipartite[i][E.V] == -bipartite[E.S][E.V] && visited[i][E.V]  == 0) {
                        visited[i][E.V]  = 1;
                        stack.push(new Edge(i,E.V));
                        num++;
                        flag = true;
                        flag_V_D = true;
                        break;
                    }
                }
            }

            if(flag)
            {

                continue;
            }
            else {
                Edge pop = stack.pop();
              //  System.out.println("pop -=-> "  + "  S: " + pop.S + " V: " +  pop.V );
                visited[pop.S][pop.V] = 0;
            }
        }


        return false;*/
    }

    private ArrayList<Integer> Get_A(ArrayList<Integer> free, ArrayList<Edge> Max_M) {
        HashSet<Integer> A_T = new HashSet<>();   //存到set中，防止出现重复的结点
        for (var f : free)
        {
            A_T.add(f);
            for(var m :Max_M)
            {
                if(isReachable(values_to_id.get(f),values_to_id.get(m.V),true)) //对于该匹配，是否存在偶数长度的交替路径使得f和m是可达的
                    A_T.add(m.V);


            }

        }

        return new ArrayList<>(A_T); //从hash set到ArrayList

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

    private ArrayList<Integer> Get_Gamma_A(ArrayList<Integer> A)
    {
        HashSet<Integer> Gamma_A_T = new HashSet<>();

        for(var a : A)
        {
            for(int i = 0; i < vsize;++i)
            {
                if(bipartite[i][values_to_id.get(a)] != 0)
                    Gamma_A_T.add(i);
            }
        }
     //   ArrayList<Integer> Gamma_A = ArrayList<>(Gamma_A_T);
//        System.out.println("\nGamma_A (neighbor nodes of A):");
//        for(var a : Gamma_A)
//            System.out.print(a + "  ");

        return   new ArrayList<>(Gamma_A_T);
    }
    private ArrayList<Integer> Get_Xc_minus_Gamma_A(ArrayList<Integer> Gamma_A)
    {
        ArrayList<Integer> Xc_minus_Gamma_A = new ArrayList<>();
        for(int i = 0; i < vsize;i++)
            if(!Gamma_A.contains(i))
                Xc_minus_Gamma_A.add(i);

//        System.out.println("\nXc - Gamma_A (rest of X expect  Gamma_A):");
//        for(var a : Xc_minus_Gamma_A)
//            System.out.print(a + "  ");
        return Xc_minus_Gamma_A;

    }

    private ArrayList<Integer> Get_Dc_minus_A(ArrayList<Integer> A)
    {
        ArrayList<Integer> Dc_minus_A = new ArrayList<>();
        for(var a : all_values)
            if(!A.contains(a))
                Dc_minus_A.add(a);
//        System.out.println("\nDc - A (rest of D expect A):");
//        for(var a : Dc_minus_A)
//            System.out.print(a + "  ");
        return Dc_minus_A;

    }

    private void Prune_all_edge_between_Gamma_A_and_Dc_minus_A( ArrayList<Integer> Gamma_A, ArrayList<Integer> Dc_minus_A)
    {

        for(var a : Gamma_A)
        {
            for(var b : Dc_minus_A)
            {
                if(bipartite[a][values_to_id.get(b)] == 1)
                    bipartite[a][values_to_id.get(b)] = 0;
            }
        }



    }

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
    private void Prune_all_edge_between_SCC(ArrayList<HashSet<Integer>> SCC, ArrayList<Integer> Xc_minus_Gamma_A, ArrayList<Integer> Dc_minus_A)
    {

        for(var a : Xc_minus_Gamma_A) //这里编号是先从X编号完毕再编号D
        {
            for (var b : Dc_minus_A) {
                if (bipartite[a][values_to_id.get(b)] == 1) {

                    if(!Is_Belong_To_One_SCC(SCC, a, values_to_id.get(b) + Xc_minus_Gamma_A.size()))
                    {
                        bipartite[a][values_to_id.get(b)] = 0;
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

            ArrayList<Integer> A = Get_A(free, Max_M);
            ArrayList<Integer> Gamma_A = Get_Gamma_A(A);
            ArrayList<Integer> Dc_minus_A = Get_Dc_minus_A(A);
            ArrayList<Integer> Xc_minus_Gamma_A = Get_Xc_minus_Gamma_A(Gamma_A);

            Prune_all_edge_between_Gamma_A_and_Dc_minus_A(Gamma_A, Dc_minus_A);
          //  ShowGraph();

            ArrayList<HashSet<Integer>> SCC = Get_SCC(Xc_minus_Gamma_A, Dc_minus_A);
            Prune_all_edge_between_SCC(SCC, Xc_minus_Gamma_A, Dc_minus_A);
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

