package cpscala.TSolver.Model.Solver.AllDifferent;

import cpscala.XModel.XVar;

import java.util.ArrayList;
import java.util.Random;

public class Test {


    static boolean Same(ArrayList<XVar> source, ArrayList<XVar> target) {
        if (source.size() != target.size())
            return false;
        boolean f = true;
        for (int i = 0; i < source.size(); ++i) {
            if (source.get(i).size != target.get(i).size) {
                f = false;
                break;
            }
            for (int j = 0; j < source.get(i).size; ++j) {
                if (source.get(i).values_ori[j] != target.get(i).values_ori[j]) {
                    f = false;
                    break;
                }
            }
            if (!f)
                break;
        }

        return f;

    }

    static ArrayList<XVar> Generate() {
        ArrayList<XVar> all = new ArrayList<XVar>();
        Random r = new Random(1);

        int n = r.nextInt(10) + 2;
        for (int i = 0; i < n; ++i) {
            int m = r.nextInt(10) + 1;
            int[] arr = new int[m];
            for (int j = 0; j < m; ++j)
                arr[j] = r.nextInt(1000);
            all.add(new XVar(i, String.valueOf(i), arr));
        }
        return all;
    }

    static void Compare() {
        long a = 0;
        long b = 0;
        long c = 0;
        long d = 0;
        for (int i = 0; i < 10000; ++i) {
            var all = Generate();
            // all.forEach(e -> e.show());


            var li = new AllDifferent_Li(all);
            d = System.nanoTime();
            li.Solve();
            a += System.nanoTime() - d;
            var li1 = li.get_Var();

            //zhen1.forEach(e -> e.show());
            var zhang = new AllDifferent_Zhang(all);
            d = System.nanoTime();
            zhang.Solve();
            b += System.nanoTime() - d;
            var zhang1 = zhang.get_Var();


            //zhang1.forEach(e -> e.show());
            var regin = new AllDifferent_Regin(all);
            d = System.nanoTime();
            regin.Solve();
            c += System.nanoTime() - d;
            var regin1 = regin.get_Var();


            if (!Same(zhang1, li1)) throw new AssertionError();
            if (!Same(zhang1, regin1)) throw new AssertionError();
        }
        System.out.println("li    " + a);
        System.out.println("zhang " + b);
        System.out.println("regin " + c);

    }

    public static void main(String[] args) {

//
//        ArrayList<XVar> all = new ArrayList<XVar>();
//
//        all.add(new XVar(0, "a", new int[]{0, 1, 2}));
//        all.add(new XVar(1, "b", new int[]{0, 1,77}));
//        all.add(new XVar(2, "c", new int[]{1, 2}));
//        all.add(new XVar(3, "d", new int[]{2, 3, 4,5,6}));
//        all.add(new XVar(4, "e", new int[]{3, 4}));
//        all.add(new XVar(5, "f", new int[]{0, 1, 5, 6,7,8,9,11,33,4,99}));

        //
//            all.add(new XVar(1, "a", Array[Int](1)))
//            all.add(new XVar(2, "b", Array[Int](1, 2)))
//            all.add(new XVar(3, "c", Array[Int](1,2, 3, 4)))
//            all.add(new XVar(4, "d", Array[Int](1, 2, 4, 5)))


//        all.forEach(i -> i.show());
//        System.out.println("---------------------li---------------");
//        var li = new AllDifferent_Li(all);
//        li.Solve();
//        li.get_Var().forEach(i -> i.show());
//
//        System.out.println("---------------------zhang---------------");
//        var zhang = new AllDifferent_Zhang(all);
//        zhang.Solve();
//        zhang.get_Var().forEach(i -> i.show());
//        System.out.println("---------------------Regin---------------");
//        var Regin = new AllDifferent_Regin(all);
//        Regin.Solve();
//        Regin.get_Var().forEach(i -> i.show());


        Compare();

    }
}
