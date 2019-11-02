package cpscala.TSolver.Model.Solver.AllDifferent;

import cpscala.XModel.XVar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

public class AllDifferent_Li extends AllDifferent {
    // 总
    BitSet matchedMask;
    BitSet unmatchedMask;
    BitSet tmp;
    BitSet[] A;
    BitSet[] B;
    BitSet[] C;
    BitSet[] D;
    BitSet t;
    BitSet tt;
    BitSet ts;
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

    List<Integer> ANodes = new LinkedList<>();
    List<Integer> gamma = new LinkedList<>();

    List<Integer> notANodes = new LinkedList<>();
    List<Integer> notGamma = new LinkedList<>();

    List<Integer> freeNodes = new LinkedList<>();

    public AllDifferent_Li(ArrayList<XVar> XV) {
        super(XV);
        arity = vars.size();
        maxDomainSize = values_to_id.size();
        numBit = arity * maxDomainSize;
        matchedMask = new BitSet(numBit);
        unmatchedMask = new BitSet(numBit);
        allowedEdges = new BitSet(numBit);
        tmp = new BitSet(numBit);
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
        ts = new BitSet(numBit);
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
            } else {
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
            for (int i = 0; i < vsize; ++i) {
//                while()
                tmp.clear();
                tmp.or(allowedEdges);
                tmp.and(D[i]);
                if (!tmp.isEmpty()) {
                    extended = true;
                    allowedEdges.or(A[i]);
                    gamma.add(i);
                }
            }

            extended = false;
            for (int i = 0; i < maxDomainSize; ++i) {
                tmp.clear();
                tmp.or(allowedEdges);
                tmp.and(C[i]);
                if (!tmp.isEmpty()) {
                    extended = true;
                    allowedEdges.or(B[i]);
                    ANodes.add(i);
                }
            }
        } while (extended);

        // step 4

        // step 5


        return true;
    }


    // shengcheng gezhong shujujiegou
    private void step1() {

    }


    private int getIndex(int a, int b) {
        return a * maxDomainSize + b;
    }

//    private void swapGamma(int i, int j) {
//        int tmp = gammaADense[i];
//        gammaADense[i] = gammaADense[j];
//        gammaADense[j] = tmp;
//
//        gammaASparse[gammaADense[i]] = i;
//        gammaASparse[gammaADense[j]] = j;
//    }
//
//    private void swapA(int i, int j) {
//        int tmp = ADense[i];
//        ADense[i] = ADense[j];
//        ADense[j] = tmp;
//
//        ASparse[ADense[i]] = i;
//        ASparse[ADense[j]] = j;
//    }


}
