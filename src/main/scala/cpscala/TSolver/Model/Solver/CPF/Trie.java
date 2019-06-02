package cpscala.TSolver.Model.Solver.CPF;

import org.xcsp.common.domains.Domains;

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

    public Trie(int n,int i)
    {
        id = i;
        count = 0;
        root = null;
        dom_size = n;
    }

    public  void Insert(int[] t)
    {

        if(root == null)
            root = new node(dom_size);

        node head = root;

        for (var i:t) {
           // System.out.println(p.next.size());
            if(head.next[i] == null)
            {
                //p.next.set(i,new node(dom_size));
                head.next[i] = new node(dom_size);
                head.next_size++;
            }
            head = head.next[i];

        }
        head.isEnd = true;

        ++count;
    }

    public void Insert_With_Data(ArrayList<Integer> t,int data)
    {
        if(root == null)
        {
            root = new node(dom_size);
        }
        node p = root;
        for (var i:t
        ) {
            if(p.next[i] == null)
            {
                p.next[i] = new node(dom_size);
                p.next_size++;
            }
            p =  p.next[i];

        }
        p.isEnd = true;

        if(p.Data == null)
            p.Data = new ArrayList<Integer>();
        p.Data.add(data);
        ++count;

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

    public Boolean Contain(int[] t)
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
