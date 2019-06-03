package cpscala.TSolver.Model.Solver.CPF;

import cpscala.XModel.XModel;

import java.util.ArrayList;

public abstract class CPFSolver {

    XModel hm;

    interface MathOperation {
        int operation(int[] in);

    }

    MathOperation Accumulate = (int[] in) ->{
        int s = 0;
        for (int i:in
             ) {
            s += i;
        }
        return s;
    };

    public int operate(int[] in, MathOperation m)
    {
        return m.operation(in);
    }

    public CPFSolver(XModel xm , String varType, String heuName)
    {
        hm = xm;
    }


    public static<E> void println(E in)
    {
        System.out.println(in);
    }
    public static<E> void print(E in)
    {
        System.out.print(in);
        System.out.print(" ");
    }

    public void print_All(int [] it)
    {
        for(var i : it) {
            System.out.print(i);
            System.out.print(" ");
        }
        System.out.println();
    }

    public void print_All(ArrayList<Integer> it)
    {
        for(var i : it) {
            System.out.print(i);
            System.out.print(" ");
        }
        System.out.println();
    }


    public  boolean Search() {

        return true;


    }

    public boolean Assignment(int level, int[] solution, int[] table_flag,ArrayList<Integer> p) {

        return true;
    }


    public boolean Check_Filter(int[] solution, final  int level)
    {
        return true;
    }


    public boolean Answer(){
        return true;
    }
}
