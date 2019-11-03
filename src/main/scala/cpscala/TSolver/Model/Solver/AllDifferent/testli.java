package cpscala.TSolver.Model.Solver.AllDifferent;

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

        Set<Integer> a = new HashSet<>();
        a.add(1);
        a.add(2);
        a.add(3);
        a.add(4);
        var it = a.iterator();

//        while (it.hasNext()) {
//
//
//            if (it.next() == 2) {
//                it.remove();
//            } else {
//                System.out.println(it.next());
//            }
//        }



    }
}
