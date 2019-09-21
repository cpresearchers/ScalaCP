package cpscala.TSolver.Model.Solver.CPFSolver;

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper;
import cpscala.XModel.XModel;

import java.util.ArrayList;

public class CPF_Path_Imp  extends CPFSolver  {
    ArrayList<Integer> p;
    int tabsize;
    int vsize;

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

    public CPF_Path_Imp(XModel xm) {

        super(xm,null,null,null);
        tabsize = xm.tabs.size();
        vsize = xm.vars.size();
        p = Select_Path();

    }

    public ArrayList<Integer> Get_P()
    {
        return p;

    }

    public double Get_Rate()
    {
        return (double)p.size() / (double)tabsize;
    }




}
