package cpscala.TSolver.Model.Solver.CPFSolver;

import cpscala.XModel.XVar;

import java.util.ArrayList;

public class Trie {


    class node
    {
        public boolean isEnd;
        public node[] next;
        public int next_size;
        public ArrayList<Integer> Data;
        public node(int Dom_size)
        {
            next = new node[Dom_size];

            Data = null;
            next_size = 0;
            isEnd = false;

        }
    }

    node root;
    String name;
    int id = 0;
    int count;
    int dom_size;
    int data_size;
    int[] size;

    public Trie( int ii, XVar[] scope)
    {
        id = ii;
        count = 0;
        root = null;
       // dom_size = n;
        data_size = 0;
        size = new int[scope.length];
        for(int i = 0; i < scope.length;++i)
            size[i] = scope[i].values.length;

    }

    public  void Insert(int[] t)
    {

        if(root == null)
            root = new node(size[0] + 1);

        node head = root;

//        for (var i:t) {
//           // System.out.println(p.next.size());
//            if(head.next[i] == null)
//            {
//                //p.next.set(i,new node(dom_size));
//                head.next[i] = new node(dom_size);
//                head.next_size++;
//            }
//            head = head.next[i];
//
//        }
        for(int i = 0; i < t.length;++i)
        {
            if(head.next[t[i]] == null)
            {

                head.next[t[i]] = new node(size[i]+1);
                head.next_size++;
            }
            head = head.next[t[i]];
        }
        head.isEnd = true;
        ++count;

    }

    public void Insert_With_Data(ArrayList<Integer> t,int data)
    {
        if(root == null)
            root = new node(size[0] + 1);

        node head = root;

//        for (var i:t) {
//           // System.out.println(p.next.size());
//            if(head.next[i] == null)
//            {
//                //p.next.set(i,new node(dom_size));
//                head.next[i] = new node(dom_size);
//                head.next_size++;
//            }
//            head = head.next[i];
//
//        }
        for(int i = 0; i < t.size();++i)
        {
            if(head.next[t.get(i)] == null)
            {

                head.next[t.get(i)] = new node(size[i]+1);
                head.next_size++;
            }
            head = head.next[t.get(i)];
        }
        head.isEnd = true;

        if(head.Data == null)
            head.Data = new ArrayList<>();
        head.Data.add(data);
        ++count;
        data_size++;

    }

    public ArrayList<Integer> Find(int[] t)
    {
        node p = root;
        for (var i:t
             ) {
            if(p.next[i] == null)
                return null;
            else
                p = p.next[i];
        }
        if(p.isEnd == true)
        {
            if(p.Data != null)
                return p.Data;
            else
                return null;
        }
        return null;
    }

    public Boolean Contain(ArrayList<Integer> t)
    {
        node head = root;
        for (var i:t
        )

        {

			if (head.next[i] == null)
			{
				return false;
			}
			else
			{
				head = head.next[i];
			}

        }

        return head.isEnd;

    }








}
