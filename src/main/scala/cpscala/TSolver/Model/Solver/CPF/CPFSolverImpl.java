package cpscala.TSolver.Model.Solver.CPF;

import cpscala.TSolver.CpUtil.SearchHelper.CPFSearchHelper;
import cpscala.XModel.XModel;
import cpscala.XModel.XVar;

import java.util.*;

/*
@author luhanzhen

2019.6.1
 */

public class CPFSolverImpl extends CPFSolver {


    class _table
    {
        public int id;
        //public ArrayList<Integer> address = new ArrayList();
        public int address[];
        public ArrayList<Integer> scope = new ArrayList();
        //public ArrayList< ArrayList<Integer> > tuples = new ArrayList<ArrayList<Integer>>();
        public  _table(int t)
        {
            address = new int[t];
        }

    }

    class _dif
    {
        public ArrayList<Integer> same_id = new ArrayList();
        public ArrayList<Integer> diff = new ArrayList();
    }

    class _info
    {
        int id;
        public ArrayList<Integer> scope =  new ArrayList();
    }



    CPFSearchHelper searchhelper;
    int vsize;
    int tabsize;
    ArrayList<_table> Path = new ArrayList<_table>();
    ArrayList<_dif> Path_Diff = new ArrayList<_dif>();
    ArrayList<ArrayList<_info>> Check_Map_Address = new ArrayList();


    HashMap<int[], Integer> Check_Map = new HashMap<int[], Integer>();
    ArrayList<Trie> Filter = new ArrayList<>();
    ArrayList<Trie> Path_Index = new ArrayList<>();
    ArrayList<Integer>  s = new ArrayList<Integer> (vsize);
    boolean flag_for_Solution;

    private ArrayList<Integer> Select_Path()
    {
        ArrayList<Integer> p = new ArrayList<Integer>();
        int graph[][] = new int[tabsize][tabsize];
        int f[][] = new int[tabsize][vsize];
        for(int i = 0;i < tabsize;++i)
        {
            for(var j : hm.tabs.get(i).scope)
            {
                f[i][j.id] = 1;
            }
        }

        for(int i = 0;i < tabsize;++i) {
            for (var j : hm.tabs.get(i).scope) {

                for(int k = i + 1;k < tabsize;++k)
                {
                    if(f[k][j.id] != 0)
                    {
                        graph[i][k]++;
                        graph[k][i]++;
                    }
                }

            }
        }
        int min = -tabsize;
        int t = 0;
        int id = 0;
        for(int i = 0; i < tabsize;++i)
        {
            for(int k = 0; k < tabsize;++k)
            {
                if(graph[i][k] != 0)
                    t++;

            }
            if(min < t)
            {
                min = t;
                id = i;
            }
            t = 0;
        }
        ArrayList<Integer> r = new ArrayList(tabsize);
        int vf[] = new int[vsize];

        int num = 0;
        int i = id;
        int sum = 0;
        int visited[] = new int[tabsize];


        while(num < tabsize)
        {
            r.add(i);
            sum = operate(vf,Accumulate);



            for(var j : hm.tabs.get(r.get(r.size() - 1)).scope)
            {
                //System.out.println(j.id);
                vf[j.id] = 1;

            }
            if(operate(vf,Accumulate) > sum)
                p.add(r.get(r.size() - 1));


            if(operate(vf,Accumulate) >= vsize)
                break;
            visited[i] = 1;
            num++;
            int max = -1;
            int max_index = -1;
            for(int k = 0; k < p.size();++k)
            {
                for(int j = 0; j < tabsize;++j)
                {
                    if (visited[j] == 0 && graph[k][j] > 0 && graph[k][j] > max)
                    {
                        max = graph[k][j];
                        max_index = j;
                    }
                }
            }
            i = max_index;
            if(i < 0 || i > tabsize)
            {
                for (int k = 0; k < tabsize; k++)
                {
                    if (visited[k] == 0)
                    {
                        i = k;
                        visited[k] = 1;
                        break;
                    }
                }
            }




        }
        assert operate(vf,Accumulate) >= vsize;
        return p;

    }

    public CPFSolverImpl(XModel xm , String varType,  String heuName) {
        super(xm, varType, heuName);
        vsize = hm.num_vars;
        tabsize = hm.num_tabs;
        searchhelper = new CPFSearchHelper(hm.num_vars, hm.num_tabs);
        ArrayList<Integer> p = Select_Path();
        //print_All(p);
        int temp_f[] = new int[tabsize];
        for (var k : p) {
            temp_f[k] = 1;
            _table _t = new _table(vsize);
            _t.id = k;
            int i = 1;
            for (var s : hm.tabs.get(k).scope
            ) {
                _t.address[s.id] = 1;

                _t.scope.add(s.id);
            }
            Path.add(_t);
        }
        for (int i = 0; i < hm.tabs.size(); ++i) {

            if (temp_f[i] != 1) {

                Trie T = new Trie(hm.max_domain_size, i);
                for (var tuple : hm.tabs.get(i).tuples)
                    T.Insert(tuple);
                Filter.add(T);
                int index[] = new int[hm.tabs.get(i).arity];
                for (int j = 0; j < hm.tabs.get(i).scope.length; ++j)
                    index[j] = hm.tabs.get(i).scope[j].id;

                Check_Map.put(index, Check_Map.size() + 1);


            }
        }

        int f[] = new int[vsize];
        int varf[] = new int[vsize];

        for (var P : Path) {
            _dif _d = new _dif();
            for (int i = 0; i < vsize; ++i) {
                if (P.address[i] * f[i] > 0)
                    _d.same_id.add(P.address[i] - 1);
                else {
                    if (P.address[i] != 0)
                        _d.diff.add(P.address[i] - 1);
                }
                f[i] = f[i] + P.address[i];
                if (f[i] > 0)
                    varf[i] = 0;
            }
            Path_Diff.add(_d);
            if (P.id != p.get(0)) {
                Trie T = new Trie(hm.max_domain_size, -P.id);
                for (int i = 0; i < hm.tabs.get(P.id).tuples.length; ++i) {
                    ArrayList<Integer> s = new ArrayList<>();
                    for (var same : Path_Diff.get(Path_Diff.size() - 1).same_id) {
                        s.add(hm.tabs.get(P.id).tuples[i][same]);
                    }
                    T.Insert_With_Data(s, i);
                }
                Path_Index.add(T);
            }


        ArrayList<_info> _t = new ArrayList<>();


        for (var it = Check_Map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<int[], Integer> item = it.next();
            int[] key = item.getKey();
            // print_All(key);
            int val = item.getValue();
            // print(val);
            if (IsExitTable(key, varf) == true && val != 0) {
                _info _tt = new _info();
                _tt.id = val - 1;
                for (var v : key)
                    _tt.scope.add(v);
                _t.add(_tt);


                Check_Map.remove(key);
                Check_Map.put(key, 0);
                print(val);

            }
        }
        Check_Map_Address.add(_t);

    }


    }

    private boolean IsExitTable(int[] scope,int [] varflag)
    {
        for (var v:scope
             ) {
            if(varflag[v] == 0)
                return false;

        }
        return true;
    }

    private void SetTableFlag(int [] table_flag,int level)
    {
        int i = level;
        for(;i < Path.size();++i)
            table_flag[i] = 0;

    }


    @Override
    public  boolean Srearch() {

            int[] table_flag = new int[Path.size()];
            table_flag[0] = 0;
             //ArrayList<Integer> solution = new ArrayList<Integer>(vsize);
            int[] solution = new int[vsize];
            // ArrayList<Integer> for_check = new ArrayList<Integer>(vsize);
             //ArrayList< ArrayList<Integer>> for_find = new ArrayList(vsize);
              int [][] for_find = new int[vsize][];
              for(int i = 0; i < Path.size();++i)
                  for_find[i] = new int[Path_Diff.get(i).same_id.size()];
              flag_for_Solution = false;
              int level = 0;
              int lastlevel = 0;
              ArrayList<Integer> p = null;
             while(table_flag[0] < hm.tabs.get(Path.get(0).id).tuples.length && flag_for_Solution == false)
              {
                  print(level);
                  if(lastlevel != level && level != 0 && level != Path.size())
                  {
                      for(int i = 0; i < Path_Diff.get(level).same_id.size();++i)
                         for_find[level][i] = solution[Path.get(level).scope.get(Path_Diff.get(level).same_id.get(i))];
                      p = Path_Index.get(level - 1).Find(for_find[level]);

                  }
                  if(Assignment(level,solution,table_flag,p) == false)
                  {
                      if(level > 0 && (p == null || table_flag[level] >= p.size()))
                      {
                          SetTableFlag(table_flag,level);
                          lastlevel = level;
                          level--;


                      }
                      continue;

                  }
                  if(Check_Filter(solution,level) == true)
                  {
                      lastlevel = level;
                      level++;
                      if(level == Path.size())
                      {
                          flag_for_Solution = true;
                          for (var v:solution
                               ) {
                              s.add(v);

                          }
                      }
                      break;
                  }



              }
             return flag_for_Solution;

    }
    @Override
    public boolean Assignment(int level, int[] solution, int[] table_flag,ArrayList<Integer> p) {

        if(level == 0)
        {
            for(int i = 0;i < Path.get(level).scope.size();++i)
               solution[Path.get(level).scope.get(i)] = hm.tabs.get(Path.get(level).id).tuples[table_flag[level]][i];

            table_flag[level]++;
            return true;
        }
        if(p == null)
            return false;
        int j = table_flag[level];
        if(j < p.size())
        {
            for(var i : Path_Diff.get(level).diff)
            {
                solution[Path.get(level).scope.get(i)] = hm.tabs.get(Path.get(level).id).tuples[p.get(j)][i];
            }
            table_flag[level] = j + 1;
            return true;
        }
        else
            return false;


    }

    @Override
    public boolean Check_Filter(int[] solution, final  int level)
    {

        ArrayList<Integer> tt = new ArrayList<Integer>(vsize);
        for(var i : Check_Map_Address.get(level))
        {
            for(int j = 0;j < i.scope.size();++j)
                tt.add(solution[i.scope.get(j)]);
            if(Filter.get(i.id-1).Contain(tt) == false)
                return false;
        }

        return true;
    }

    @Override
    public boolean Answer(){
        if(flag_for_Solution == true)
        {
            println("Solution is :");
            print_All(s);
        }
        else
            println("no Solution!");

        return flag_for_Solution;
    }


}


// for (var k:p
//   ) {
//  temp_f.set(k,1);

//  }
//        Trie T = new Trie(hm.max_domain_size,1);
//        for(var i :hm.tabs.get(1).tuples)
//        {
//            T.Insert(i);
//
//        }
//        ArrayList<Integer> ttt;
//
//
//        for(var i :hm.tabs.get(1).tuples)
//        {
//
//            assert T.Contain(i);
//        }