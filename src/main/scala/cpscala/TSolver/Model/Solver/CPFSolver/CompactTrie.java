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
    //String name;
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
        size = new int[scope.length];
        for(int i = 0; i < scope.length;++i) {
            size[i] = scope[i].values.length;
            //System.out.print(size[i+1] + "  ");
           // System.out.print(size[i] + "  ");
        }





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
                if (head.address[i] == -2)
                    return true;
                head = head.next.get(head.address[i]);
            }

        }

        return head.isEnd;
    }

    public  void Insert(int[] t)
    {

        if(root == null)
        {
            root = new node(size[0]);
        }

        node head = root;

        for(int i = 0;i < t.length;++i)
        {

            if(head.address[t[i]] == -1)
            {
                if(i != t.length -1) {
                    head.next.add(new node(size[i + 1]));
                    head.address[t[i]] = head.next_size;
                    head.next_size++;
                }
                else
                {
                    head.address[t[i]] = -2;
                   // head.isEnd = true;
                    break;
                }
            }
            head = head.next.get(head.address[t[i]]);




        }

        head.isEnd = true;
        ++count;

    }


//    public  void Insert(int[] t)
//    {
//
//        if(root == null)
//        {
//            root = new node(size[1]);
//        }
//
//        node head = root;
//        //for(var i : t)
//        for(int i = 0;i < t.length;++i)
//        {
//            System.out.print(t[i] + "  ");
//            if(head.address[t[i]] == -1)
//            {
//                if(i != t.length -1) {
//                    head.next.add(new node(size[i + 1]));
//                    head.address[t[i]] = head.next_size;
//                    head.next_size++;
//                }
//                else
//                {
//                    head.address[t[i]] = -2;
//                   // head.isEnd = true;
//                    break;
//                }
//            }
//            head = head.next.get(head.address[t[i]]);
//
//        }
//        System.out.print("\n");
//        head.isEnd = true;
//        ++count;
//
//    }






//    public Boolean Contain(ArrayList<Integer> t)
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
//                if (head.address[i] == -2)
//                    return true;
//                head = head.next.get(head.address[i]);
//            }
//
//
//        }
//
//        return head.isEnd;
//    }

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
