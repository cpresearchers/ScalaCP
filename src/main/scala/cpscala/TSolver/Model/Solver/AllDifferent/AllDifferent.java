package cpscala.TSolver.Model.Solver.AllDifferent;

import cpscala.XModel.XVar;

import java.util.*;

public abstract class AllDifferent {
    static class Edge { //边
        int Start, End;

        Edge(int s, int v) {
            Start = s;
            End = v;
        }
        @Override
        public boolean equals(Object obj) {
            if(this == obj)
                return true;
            Edge in = (Edge)obj;
            return Start == in.Start && End == in.End;
        }

    }

    protected ArrayList<XVar> vars;
    private ArrayList<XVar> after_vars;

    int vsize;
    int[][] bipartite;
    ArrayList<Edge> Max_M ;
    HashSet<Integer> free;

    ArrayList<Integer> all_values;
    HashMap<Integer, Integer> values_to_id; //值到id的映射


    AllDifferent(ArrayList<XVar> XV) {

        vars = XV;
        vsize = XV.size();
        HashSet<Integer> all = new HashSet<>(); //将所有的取值扔到set里面
        for (var a : vars)
            for (var b : a.values_ori)
                all.add(b);

        all_values = new ArrayList<>(all); //所有的取值拿到list中
        values_to_id = new HashMap<>();
        for (int i = 0; i < all_values.size(); ++i) {
            values_to_id.put(all_values.get(i), i);
        }
        Generate_Bipartite();
        Max_M = new ArrayList<>();
        free = new HashSet<>();
        Find_Max_Match();
        Get_Free_Node();

    }

    void Get_Free_Node() {
       free.clear();
//        for (var m : M) {
//            if(!all_values.contains(m.End))
//                free.add(m.End);
//
//        }
        for (var a : all_values) {
            boolean f = false;
            for (var b : Max_M) {
                if (a == b.End) {
                    f = true;
                    break;
                }
            }
            if (!f) {
                free.add(a);
            }

        }
//        System.out.print("\nfree node: ");
//        for(var a : free)
//            System.out.print(a + "  ");
//        System.out.println();

    }

    private boolean Hungary_Algorithm(int[] visited, int[] s_v, int[] s_x, int x) //匈牙利算法递归求增广路径
    {
        for (int i = 0; i < s_v.length; ++i) {
            if (bipartite[x][i] == 1 && visited[i] == 0) {
                visited[i] = 1;
                if (s_v[i] == -1 || Hungary_Algorithm(visited, s_v, s_x, s_v[i])) {
                    s_x[x] = i;
                    s_v[i] = x;
                    return true;
                }
            }
        }
        return false;

    }


    void Find_Max_Match() //匈牙利算法求最大匹配
    {

        Max_M.clear();
        int[] s_x = new int[vsize];
        int[] s_v = new int[all_values.size()];
        Arrays.fill(s_x, -1);
        Arrays.fill(s_v, -1);

        int[] visited = new int[all_values.size()];
        for (int i = 0; i < vsize; ++i) {

            Arrays.fill(visited, 0);

            Hungary_Algorithm(visited, s_v, s_x, i);

        }

        for (int i = 0; i < s_x.length; ++i)
            Max_M.add(new Edge(i, all_values.get(s_x[i])));
        for (var m : Max_M) //将图中匹配置为-1
        {
            bipartite[m.Start][values_to_id.get(m.End)] = -bipartite[m.Start][values_to_id.get(m.End)];
        }

//        System.out.println("max match:");
//        for(var a : Max_M)
//       {
//
//            System.out.println(a.S +"----" + a.V);
//
//
//        }
//        for (var e : Max_M) {
//            System.out.println(e.V + "---------->" + e.S);
//        }


    }

    private void Generate_Bipartite() //生成二部图
    {
        bipartite = new int[vars.size()][all_values.size()];
        for (int j = 0; j < vars.size(); ++j)
            for (var b : vars.get(j).values_ori) {
                bipartite[j][values_to_id.get(b)] = 1;
            }

    }

    boolean preprocess()  //预处理 用于检测那些论域为空的变量和论域仅仅只有一个值的变量
    {
        if (vsize > all_values.size())//变量的个数少于值的个数，必然无解
        {

            return true;
        }
        // System.out.println("aa");
        for (int i = 0; i < vsize; ++i) //存在某个变量的论域为空 必然无解
        {
            if (vars.get(i).values_ori.length == 0) {
                return true;
            }
            //if(vars.get(i).values_ori.length == 1)
            //    ori_solution[i] = vars.get(i).values_ori[0];
        }
        return false;
    }

    void generate_new_var() {
        after_vars = new ArrayList<>();
        for (int i = 0; i < vsize; ++i) {
            ArrayList<Integer> values = new ArrayList<>();
            for (int j = 0; j < vars.get(i).values_ori.length; ++j) {
                if (bipartite[i][values_to_id.get(vars.get(i).values_ori[j])] != 0) {
                    values.add(vars.get(i).values_ori[j]);
                }

            }
            int[] v = new int[values.size()];
            for (int k = 0; k < values.size(); ++k)
                v[k] = values.get(k);


            after_vars.add(new XVar(vars.get(i).id, vars.get(i).name, v));

        }
    }

    public boolean Solve() {


        if (preprocess())  //必然不可解，肯定不用解了，直接返回
            return false;

        try {

            generate_new_var();
            //   ShowGraph();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }


    public ArrayList<XVar> get_Var() {
        return after_vars;
    }



    private void ShowGraph() {
        System.out.println("graph:");
        for (var a : bipartite) {
            for (var b : a) {
                System.out.print(b + " ");
            }
            System.out.println();
        }
    }



    public void show() {
        for (var a : all_values)
            System.out.print(a + " ");
        System.out.println();

        for (var a : vars)
            a.show();
        ShowGraph();

        System.out.println();
    }

}
