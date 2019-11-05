package cpscala.TSolver.Model.Solver.AllDifferent;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class testli {
    public static void main(String[] args) {
//        System.out.println("xixi");
//
////        Dichotomy d = new Dichotomy(5, false);
////        Dichotomy.Iterator i = d.latterBegin();
////
////        while (!i.afterLatterEnd()) {
////            System.out.println(i.getValue());
////            if ((i.getValue() % 2) == 0) {
////                i.moveElementToFrontAndGoNext();
////            } else {
////                System.out.println("reserve: " + i.getValue());
////                i.next();
////            }
////        }
//
//        Dichotomy d = new Dichotomy(5, false);
//        Dichotomy.Iterator i = d.latterEnd();
//
//        while (!i.beforeLatterBegin()) {
//            System.out.println(i.getValue());
////            if ((i.getValue() % 2) == 0) {
////                i.moveElementToFrontAndGoPrevious();
////            } else {
////                System.out.println("reserve: " + i.getValue());
////                i.previous();
////            }
//            i.previous();
//        }
//
////        System.out.println("--------------------");
////        Dichotomy.Iterator j = d.frontEnd();
////
////        while (!j.beforeFrontBegin()) {
////            if (j.getValue() == 2) {
////                j.moveElementToLatterAndGoPrevious();
////            } else {
////                System.out.println(j.getValue());
////                j.previous();
////            }
////        }
////
////        System.out.println("--------------------");
////        Dichotomy.Iterator k = d.latterBegin();
////
////        while (!k.afterLatterEnd()) {
////            System.out.println(k.getValue());
////            k.next();
////        }

//        Set<Integer> a = new HashSet<>();
//        a.add(1);
//        a.add(2);
//        a.add(3);
//        a.add(4);
//        var it = a.iterator();

//        while (it.hasNext()) {
//
//
//            if (it.next() == 2) {
//                it.remove();
//            } else {
//                System.out.println(it.next());
//            }
//        }

        BitSet s = new BitSet(5);
//        s.set(0);
//        s.set(4);
//        s.set(7);

//        System.out.println(s.toLongArray());

//        for (int i = 0; i < s.size(); ++i) {
//            System.out.println(s.get(i) ? 1 : 0);
//        }
        int i = s.nextSetBit(0);
        System.out.println(i);
//        BitSet a = new BitSet(5);
//        BitSet b = new BitSet(5);
//        BitSet c = new BitSet(5);
//        c.clear();
//        c.or(a);
//        c.and(b);

    }
}
