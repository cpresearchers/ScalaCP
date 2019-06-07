package cpscala.TSolver.Model.Solver.CPFSolver;

import cpscala.XModel.XVar;

import java.util.ArrayList;

public class CompactTrie {


    class node
    {

        public boolean isEnd;

        public int next_size;

        public  int[] address;  //用于映射

        public ArrayList<node> next;

        public node(int Dom_size)
        {
            next = new ArrayList<node>();
            next_size = 0;
            isEnd = false;
            address = new int[Dom_size];
            for(int i = 0; i < Dom_size ; ++i)
                address[i] = -1;


        }
    }

    node root;
    String name;
    int id = 0;
    int count;

    int data_size;
    int[] size;

    public CompactTrie( int ii, XVar[] scope)
    {
        id = ii;
        count = 0;
        root = null;
        // dom_size = n;
        data_size = 0;
        size = new int[scope.length+1];
        for(int i = 0; i < scope.length;++i)
            size[i+1] = scope[i].values.length + 1;

    }

    public  void Insert(int[] t)
    {
        if(root == null)
        {
            root = new node(size[1]);
        }

        node head = root;
        //for(var i : t)
        for(int i = 0;i < t.length;++i)
        {
            if(head.address[t[i]] == -1)
            {

                head.next.add(new node(size[i+1]));
                head.address[t[i]] = head.next_size;
                head.next_size++;
            }
            head = head.next.get(head.address[t[i]]);

        }
        head.isEnd = true;
        ++count;

    }
    public void Build(int[][] tuples)
    {
        for(var i : tuples)
            this.Insert(i);

    }


    public Boolean Contain(ArrayList<Integer> t)
    {
        node head = root;
        for (var i:t)
        {

            if (head.address[i] == -1)
            {
                return false;
            }
            else
            {
                head = head.next.get(head.address[i]);
            }


        }

        return head.isEnd;
    }

//
//    public Boolean Contain(int[] t)
//    {
//        node head = root;
//        for (var i:t)
//        {
//
//            if (head.address[i] == -1)
//            {
//                return false;
//            }
//            else
//            {
//                head = head.next.get(head.address[i]);
//            }
//
//
//        }
//
//        return head.isEnd;
//    }



}
