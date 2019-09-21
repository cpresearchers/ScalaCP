package cpscala.TSolver.Model.Solver.CPFSolver;

import cpscala.XModel.XTab;

import java.util.ArrayList;


//用表来存字典树，节约空间，
//多元拆成二元

public class Table_Trie {
    private class table{
        public int a,b;
        public char[][] data;
        public char[] row;
        public char[] col;
        public table(int aa,int bb,int a_domain, int b_domain)
        {
            a = aa;
            b = bb;
            data = new char[a_domain][b_domain];
            row = new char[a_domain];
            col = new char[b_domain];
        }
        public void add(int i,int j)
        {
            data[i][j] = 1;
            row[i] = 1;
            col[j] = 1;
        }
        public void show()
        {
            for(int i = 0; i < data.length;++i)
            {
                for(int j = 0; j < data[i].length;++j)
                {
                    if(data[i][j] == 1)
                        System.out.print("1");
                    else
                        System.out.print("0");
                }
                System.out.println();
            }
        }
        public char get(int i,int j)
        {
            return  data[i][j];
        }
        public char get_row_ith(int i)
        {
            return row[i];
        }
        public char get_col_ith(int j)
        {
            return col[j];
        }

    }

    public ArrayList<table> T;
    int id = 0;
    public Table_Trie(int i)
    {
        id = i;
        T = new ArrayList<>(0);

    }

    public void AddTabs(XTab xt)
    {

        for (int i = 0; i < xt.scope.length -1;++i) {

            T.add(new table(xt.scope[i].id,xt.scope[i+1].id,xt.scope[i].size,xt.scope[i+1].size));
        }
        for(var t : xt.tuples)
        {
            for(int i = 0; i < t.length - 1;++i)
            {
                T.get(i).add(t[i],t[i+1]);

            }
        }

    }

    public void Show()
    {
        for (var t:
             T
             ) {
            System.out.println(t.a+"  "+t.b+"  "+t.row.length+"  "+t.col.length);
            t.show();
        }

    }
    public boolean Check(int[] S)
    {
        return true;

    }


}
