package cpscala.TSolver.Model.Solver.AllDifferent;

import cpscala.XModel.XVar;

import java.util.*;

public class AllDifferent_Li extends AllDifferent {
    // 总
    BitSet matchedMask;
    BitSet unmatchedMask;
    BitSet tmp;
    // 跨界边一定被删除
    BitSet transboundary;
    BitSet needCheckEdge;
    BitSet[] A;
    BitSet[] B;
    BitSet[] C;
    BitSet[] D;
    BitSet t;
    BitSet tt;
    BitSet th;
    BitSet allowedEdges;
    int arity;
    int maxDomainSize;
    int numBit;
//    int[] gammaADense;
//    int[] gammaASparse;
//    int numGamma;
//    int[] ADense;
//    int[] ASparse;
//    int numA;

//    List<Integer> ANodes = new LinkedList<>();
//    List<Integer> gamma = new LinkedList<>();
//
//    List<Integer> notANodes = new LinkedList<>();
//    List<Integer> notGamma = new LinkedList<>();
//
//    List<Integer> freeNodes = new LinkedList<>();

    Set<Integer> ANodes = new HashSet<>();
    Set<Integer> notANodes = new HashSet<>();
    Set<Integer> gamma = new HashSet<>();
    Set<Integer> notGamma = new HashSet<>();
    Set<Integer> freeNodes = new HashSet<>();

    public AllDifferent_Li(ArrayList<XVar> XV) {
        super(XV);
        arity = vars.size();
        maxDomainSize = values_to_id.size();
        numBit = arity * maxDomainSize;
        matchedMask = new BitSet(numBit);
        unmatchedMask = new BitSet(numBit);
        allowedEdges = new BitSet(numBit);
        tmp = new BitSet(numBit);
        transboundary = new BitSet(numBit);
        needCheckEdge = new BitSet(numBit);
        A = new BitSet[vsize];
        B = new BitSet[maxDomainSize];
        C = new BitSet[maxDomainSize];
        D = new BitSet[vsize];


//        // 初始化两个稀疏集
//        // 集合前半部分是非gamma的，集合后半部是gamma的
//        gammaADense = new int[vsize];
//        gammaASparse = new int[vsize];
//
//        numGamma = vsize - 1;
//        // 初始化两个稀疏集
//        // 集合前半部分是属于A，集合后半部不属于A
//        ADense = new int[maxDomainSize];
//        ASparse = new int[maxDomainSize];
//        numA = maxDomainSize - 1;
//
//

        for (int i = 0; i < vsize; ++i) {
            A[i] = new BitSet(numBit);
            D[i] = new BitSet(numBit);
//
//            gammaADense[i] = i;
//            gammaASparse[i] = i;
        }

        for (int i = 0; i < maxDomainSize; ++i) {
            B[i] = new BitSet(numBit);
            C[i] = new BitSet(numBit);
//
//            ADense[i] = i;
//            ASparse[i] = i;
        }

        t = new BitSet(numBit);
        tt = new BitSet(numBit);
        th = new BitSet(numBit);
    }


    public boolean Solve() {
        if (!preprocess())  //必然不可解，肯定不用解了，直接返回
            return false;

        // 清
        ANodes.clear();
        gamma.clear();
        notANodes.clear();
        notGamma.clear();
        freeNodes.clear();

        ArrayList<Edge> Max_M = Find_Max_Match();
//        ArrayList<Integer> freeNodes = new ArrayList<>();

        for (int i = 0; i < arity; ++i) {
            for (int j = 0; j < maxDomainSize; ++j) {
                var idx = getIndex(i, j);
                switch (bipartite[i][j]) {
                    case 1:
                        C[j].set(idx);
                        D[i].set(idx);
                        unmatchedMask.set(idx);
                        break;
                    case -1:
                        A[i].set(idx);
                        B[j].set(idx);
                        matchedMask.set(idx);
                        break;
                    case 0:
                        break;
                    default:
                        break;
                }
            }
        }

        for (int i = 0; i < maxDomainSize; ++i) {
            // 值的入匹配边为0，说明是自由点
            if (B[i].isEmpty()) {
                freeNodes.add(i);
                ANodes.add(i);
                notANodes.remove(i);
            } else {
                // 默认都不在gamma里
                notGamma.add(i);
            }
        }

//        System.out.println("---A---");
//        for (var a : A) {
//            System.out.println(a);
//        }
//        System.out.println("---B---");
//        for (var a : B) {
//            System.out.println(a);
//        }
//        System.out.println("---C---");
//        for (var a : C) {
//            System.out.println(a);
//        }
//        System.out.println("---D---");
//        for (var a : D) {
//            System.out.println(a);
//        }

        // step2
        for (var v : freeNodes) {
            allowedEdges.or(C[v]);
        }

        // step 3
        //申请一些临时变量用来标记是否结束
        boolean extended = false;
        do {
//            for (int i = 0; i < vsize; ++i) {
////                while()
//                tmp.clear();
//                tmp.or(allowedEdges);
//                tmp.and(D[i]);
//                if (!tmp.isEmpty()) {
//                    extended = true;
//                    allowedEdges.or(A[i]);
//                    gamma.add(i);
//                }
//            }
//            boolean extended = false;

            extended = false;
            var itGamma = notGamma.iterator();

            while (itGamma.hasNext()) {
                var i = itGamma.next();
                tmp.clear();
                tmp.or(allowedEdges);
                tmp.and(D[i]);
                if (!tmp.isEmpty()) {
                    extended = true;
                    allowedEdges.or(A[i]);
                    itGamma.remove();
                    gamma.add(i);
                }
            }

//            extended = false;
//            for (int i = 0; i < maxDomainSize; ++i) {
//                tmp.clear();
//                tmp.or(allowedEdges);
//                tmp.and(C[i]);
//                if (!tmp.isEmpty()) {
//                    extended = true;
//                    allowedEdges.or(B[i]);
//                    ANodes.add(i);
//                }
//            }

            extended = false;
            var itA = notANodes.iterator();
            while (itA.hasNext()) {
                var i = itA.next();
                tmp.clear();
                tmp.or(allowedEdges);
                tmp.and(C[i]);
                while (!tmp.isEmpty()) {
                    extended = true;
                    allowedEdges.or(B[i]);
                    itA.remove();
                    ANodes.add(i);
                }
            }

            extended = false;
            var itGamma2 = notGamma.iterator();

            while (itGamma2.hasNext()) {
                var i = itGamma2.next();
                tmp.clear();
                tmp.or(allowedEdges);
                tmp.and(B[i]);
                if (!tmp.isEmpty()) {
                    extended = true;
                    allowedEdges.or(C[i]);
                    itGamma2.remove();
                    gamma.add(i);
                }
            }

            extended = false;
            var itA2 = notANodes.iterator();
            while (itA2.hasNext()) {
                var i = itA2.next();
                tmp.clear();
                tmp.or(allowedEdges);
                tmp.and(A[i]);
                while (!tmp.isEmpty()) {
                    extended = true;
                    allowedEdges.or(D[i]);
                    itA2.remove();
                    ANodes.add(i);
                }
            }

        } while (extended);

        // step 5 删掉Dc-A到gamma(A)的边
        // 方法：从Dc-A的非匹配出边集合，与gmma(A)的非匹配入边的交集就是这种跨界边，原则小循环套大循环，一般而言Dc-A比较小

        tmp.clear();
        transboundary.clear();
        var it = notANodes.iterator();
        while (it.hasNext()) {
            var a = it.next();

            tmp.or(C[a]);
            var jt = gamma.iterator();
            while (jt.hasNext()) {
                var v = jt.next();
                tmp.and(D[v]);
                transboundary.or(tmp);
            }
        }

        // step 6 过滤需要检查强连通分量的边

        needCheckEdge.clear();
        needCheckEdge.or(allowedEdges);
        needCheckEdge.or(transboundary);
        needCheckEdge.or(matchedMask);
        needCheckEdge.flip(0, numBit - 1);

        // step 7 SCC检查
        int ii = needCheckEdge.nextClearBit(0);


        // 第i个位置不为1
        while (ii != -1) {

            t.clear();
            th.clear();
            t.set(ii);

            // 匹配变量
//        extended = false;
            var itNotGamma = notGamma.iterator();
            while (itNotGamma.hasNext()) {
                var i = itNotGamma.next();
                tmp.clear();
                // 注意这里是t
                tmp.or(t);
                tmp.and(D[i]);
                if (!tmp.isEmpty()) {
                    th.or(A[i]);
                    itNotGamma.remove();
                }
            }

            extended = false;
            var itNotA = notANodes.iterator();
            while (itNotA.hasNext()) {
                var i = itNotA.next();
                tmp.clear();
                tmp.or(t);
                tmp.and(C[i]);
                while (!tmp.isEmpty()) {
                    extended = true;
                    tt.or(B[i]);
                    itNotA.remove();
                }
            }

            // 没有扩展出去，就可以删值了
            // ??
            if (!extended) {

            }
//            ii = needCheckEdge.nextClearBit(ii);
            var xx = false;
//             两处退出条件
//             ??
            while (!xx) {
                // i就是第几个边，需要进行SCC检查
                var itGamma2 = notGamma.iterator();
                while (itGamma2.hasNext()) {
                    var i = itGamma2.next();
                    tmp.clear();
                    // 注意这里是th
                    tmp.or(th);
                    tmp.and(B[i]);
                    if (!tmp.isEmpty()) {
                        extended = true;
                        allowedEdges.or(C[i]);
                        itGamma2.remove();
                        gamma.add(i);
                    }
                }

                extended = false;
                var itA2 = notANodes.iterator();
                while (itA2.hasNext()) {
                    var i = itA2.next();
                    tmp.clear();
                    tmp.or(allowedEdges);
                    tmp.and(A[i]);
                    while (!tmp.isEmpty()) {
                        extended = true;
                        allowedEdges.or(D[i]);
                        itA2.remove();
                        ANodes.add(i);
                    }
                }

                extended = false;
                var itGamma = notGamma.iterator();
                while (itGamma.hasNext()) {
                    var i = itGamma.next();
                    tmp.clear();
                    tmp.or(allowedEdges);
                    tmp.and(D[i]);
                    if (!tmp.isEmpty()) {
                        extended = true;
                        allowedEdges.or(A[i]);
                        itGamma.remove();
                        gamma.add(i);
                    }
                }

                extended = false;
                var itA = notANodes.iterator();
                while (itA.hasNext()) {
                    var i = itA.next();
                    tmp.clear();
                    tmp.or(allowedEdges);
                    tmp.and(C[i]);
                    while (!tmp.isEmpty()) {
                        extended = true;
                        allowedEdges.or(B[i]);
                        itA.remove();
                        ANodes.add(i);
                    }
                }
            }

            ii = needCheckEdge.nextClearBit(ii);
        }

        return true;
    }

    private int getIndex(int a, int b) {
        return a * maxDomainSize + b;
    }
}
