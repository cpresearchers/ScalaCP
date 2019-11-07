package cpscala.TSolver.Model.Solver.AllDifferent;

import cpscala.XModel.XVar;
import scala.Tuple2;
import scala.collection.immutable.Range;

import java.io.PrintStream;
import java.util.*;

public class AllDifferent_Li extends AllDifferent {

    // 总
    BitSet matchedMask;
    BitSet unmatchedMask;
    BitSet tmp;
    // 跨界边一定被删除
    BitSet transboundary;
    BitSet untransboundary;
    BitSet needCheckEdge;
    // 删除边
    BitSet removedEdge;
    // 无效边
    BitSet invalidEdge;
    BitSet[] A;
    BitSet[] B;
    BitSet[] C;
    BitSet[] D;
    BitSet t;
    //    BitSet tt;
//    BitSet th;
    BitSet allowedEdges;
    //    int arity;
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

    // 两个临时数据结构
    Set<Integer> notATmp = new HashSet<>();
    Set<Integer> notGTmp = new HashSet<>();

    public AllDifferent_Li(ArrayList<XVar> XV) {
        super(XV);
//        arity = vars.size();
        maxDomainSize = values_to_id.size();
        numBit = vsize * maxDomainSize;
        matchedMask = new BitSet(numBit);
        unmatchedMask = new BitSet(numBit);
        allowedEdges = new BitSet(numBit);
        tmp = new BitSet(numBit);
        transboundary = new BitSet(numBit);
        untransboundary = new BitSet(numBit);
        needCheckEdge = new BitSet(numBit);
        removedEdge = new BitSet(numBit);
        invalidEdge = new BitSet(numBit);
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
//        tt = new BitSet(numBit);
//        th = new BitSet(numBit);
    }


    public boolean Solve() {
        if (preprocess())  //必然不可解，肯定不用解了，直接返回
            return false;

        // 清理集合
        ANodes.clear();
        gamma.clear();
        notANodes.clear();
        notGamma.clear();
        freeNodes.clear();

        // 生成无效边

        // 初始化两个not集合
        for (int i = 0; i < maxDomainSize; ++i) {
            notANodes.add(i);
        }
        for (int i = 0; i < vsize; ++i) {
            notGamma.add(i);
        }

        Find_Max_Match();

        for (int i = 0; i < vsize; ++i) {
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
                        invalidEdge.set(idx);
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
            }
//            else {
//                // 默认都不在gamma里
//                notGamma.remove(i);
//            }
        }

        System.out.println("---A---");
        for (var a : A) {
            System.out.println(a);
        }
        System.out.println("---B---");
        for (var a : B) {
            System.out.println(a);
        }
        System.out.println("---C---");
        for (var a : C) {
            System.out.println(a);
        }
        System.out.println("---D---");
        for (var a : D) {
            System.out.println(a);
        }
        System.out.println("-------");

        System.out.println(freeNodes.toString());
        System.out.println(notANodes.toString());
        System.out.println(ANodes.toString());
        System.out.println(gamma.toString());
        System.out.println(notGamma.toString());
        // step2
        for (var v : freeNodes) {
            allowedEdges.or(C[v]);
        }

        System.out.println("---allowedEdges---");
        System.out.println(allowedEdges);


        // step 3
        //申请一些临时变量用来标记是否结束
        boolean extended = false;
        do {
            var itNotG = notGamma.iterator();
            while (itNotG.hasNext()) {
                var i = itNotG.next();
                tmp.clear();
                tmp.or(allowedEdges);
                tmp.and(D[i]);
                if (!tmp.isEmpty()) {
                    allowedEdges.or(A[i]);
                    itNotG.remove();
                    gamma.add(i);
                }
            }

            extended = false;
            var itNotA = notANodes.iterator();
            while (itNotA.hasNext()) {
                var i = itNotA.next();
                tmp.clear();
                tmp.or(allowedEdges);
                tmp.and(B[i]);
                if (!tmp.isEmpty()) {
                    extended = true;
                    allowedEdges.or(C[i]);
                    itNotA.remove();
                    gamma.add(i);
                }
            }

        } while (extended);

        System.out.println(allowedEdges);

        // step 5 删掉Dc-A到gamma(A)的边
        // 方法：从Dc-A的非匹配出边集合，与gmma(A)的非匹配入边的交集就是这种跨界边，原则小循环套大循环，一般而言Dc-A比较小

        transboundary.clear();
        var it = notANodes.iterator();
        while (it.hasNext()) {
            var a = it.next();
            var jt = gamma.iterator();
            while (jt.hasNext()) {
                var v = jt.next();
                tmp.clear();
                tmp.or(C[a]);
                tmp.and(D[v]);
                transboundary.or(tmp);
            }
        }

        // 删除跨界边
        removedEdge.clear();
        removedEdge.and(transboundary);
        untransboundary.clear();
        untransboundary.or(transboundary);
        untransboundary.flip(0, numBit);
        System.out.println("-----transboundary-----");
        System.out.println(transboundary.toString());

        System.out.println("-----untransboundary-----");
        System.out.println(untransboundary.toString());
//
        // step 6 过滤需要检查强连通分量的边
        System.out.println("-----allowedEdges-----");
        System.out.println(allowedEdges.toString());
        System.out.println("-----matchedMask-----");
        System.out.println(matchedMask.toString());
        System.out.println("-----invalidEdge-----");
        System.out.println(invalidEdge.toString());


        needCheckEdge.clear();
        needCheckEdge.or(allowedEdges);
        needCheckEdge.or(transboundary);
        needCheckEdge.or(matchedMask);
        needCheckEdge.or(invalidEdge);
        needCheckEdge.flip(0, numBit);

        System.out.println("-----needCheckEdge-----");
        System.out.println(needCheckEdge.toString());
//
        System.out.println("-----SCC-----");
        // step 7 SCC检查
        int ii = needCheckEdge.nextSetBit(0);
        // 第i个位置不为1
        while (ii != -1) {
            System.out.println(ii);
            t.clear();
            t.set(ii);


            // 获得边信息这个是非匹配边
            var v_a = getValue(ii);

            System.out.println(v_a);
            notATmp.clear();
            notGTmp.clear();

            for (var a : notANodes) {
                notATmp.add(a);
            }
            for (var v : notGamma) {
                notGTmp.add(v);
            }
            // 拿到1的匹配边，以后对比这个dest就行了
            var dest = B[v_a._2];
            System.out.println("----dest----");
            System.out.println(dest);
            var inSCC = false;

            do {
                // 头部扩展
                // 匹配变量
                var itNotG2 = notGTmp.iterator();
                while (itNotG2.hasNext()) {
                    var i = itNotG2.next();
                    tmp.clear();
                    tmp.or(t);
                    tmp.and(D[i]);
                    if (!tmp.isEmpty()) {
                        t.or(A[i]);
                        itNotG2.remove();
                    }
                }

                // 匹配值
                extended = false;
                var itNotA2 = notATmp.iterator();
                while (itNotA2.hasNext()) {
                    var i = itNotA2.next();
                    tmp.clear();
                    tmp.or(t);
                    tmp.and(B[i]);
                    if (!tmp.isEmpty()) {
                        extended = true;
                        t.or(C[i]);
                        t.and(untransboundary);
                        itNotA2.remove();
                    }
                }

                tmp.clear();
                tmp.or(dest);
                tmp.and(t);
                inSCC = !tmp.isEmpty();
//                if (!extended) {
//                    break;
//                }
//                if (inSCC) {
//                    break;
//                }
            } while (!inSCC && extended);

            if (inSCC) {
                System.out.println(ii + " in SCC");
            } else if (!extended) {
                System.out.println(ii + " is not SCC");
                removedEdge.set(ii);
            }

            ii = needCheckEdge.nextSetBit(ii + 1);
        }

        for (int i = removedEdge.nextSetBit(0); i != -1; i = removedEdge.nextSetBit(i + 1)) {
            var r = getValue(i);
            bipartite[r._1][r._2] = 0;
        }
        generate_new_var();

        return true;
    }

    private int getIndex(int a, int b) {
        return a * maxDomainSize + b;
    }

    private Tuple2<Integer, Integer> getValue(int index) {
        return Tuple2.apply(index / maxDomainSize, index % maxDomainSize);

    }
}
