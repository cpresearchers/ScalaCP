//package cpscala.TSolver.CpUtil;
//
//public class JRSBitSet {
//    // array of rlong, words.length = p
//    long words[][];
//    // array of int,  index.length = p
//    int index[];
//    // number of long
//    int numBit;
//    // rint
//    int limit[];
//    // array of long, mask.length = p
//    long mask[];
//
//    int lastLimits;
//
//    //
//    int currentLevel = 0;
//
//    //变量个数
//    int numVars;
//    int numLevel;
//    int numTuples;
//    int id;
//    int lastNewLevel = 0;
//    int preLevel = 0;
//
//    JRSBitSet(int id, int numTuples, int numVars) {
//        this.id = id;
//        this.numVars = numVars;
//        numLevel = numVars + 1;
//        this.numTuples = numTuples;
//
//        numBit = (int) Math.ceil((double) numTuples / (double) Constants.BITSIZE());
//        lastLimits = numTuples % Constants.BITSIZE();
//
//        words = new long[numLevel][numBit];
//
//        for (int i = 0; i < numBit; ++i) {
//            words[0][i] = 0xFFFFFFFFFFFFFFFFL;
//        }
//
//        //lastLimit 取值为[0, 63]
//        //若lastLimit = 0, lastWord不改变
//        //otherwise, lastWord <<= 64 - lastLimit
//        if (lastLimits != 0) {
//            words[0][numBit - 1] <<= 64 - lastLimits;
//        }
//        //初始化limit, index, mask
//        limit = new int[numLevel];
//        for (int i = 0; i < numLevel; i++) {
//            limit[i] = -1;
//        }
//        limit[0] = numBit - 1;
//
//        index = new int[numBit];
//        for (int i = 0; i < numBit; i++) {
//            index[i] = i;
//        }
//
//        mask = new long[numBit];
//    }
//
//
//    public void newLevel(int level) {
////        System.out.println("l: " + level + ", p: " + preLevel);
//        if (currentLevel != level) {
//            currentLevel = level;
//            limit[currentLevel] = limit[preLevel];
//
//            for (int i = limit[preLevel]; i >= 0; --i) {
//                final int offset = index[i];
//                words[currentLevel][offset] = words[preLevel][offset];
//            }
//            preLevel = level;
//        }
//
////        if (lastNewLevel != level) {
////            int preLevel = lastNewLevel;
////            lastNewLevel = level;
////            currentLevel = level;
////            limit[currentLevel] = limit[preLevel];
////            for (int i = limit[currentLevel]; i >= 0; --i) {
////                final int offset = index[i];
////                words[currentLevel][offset] = words[preLevel][offset];
////            }
////        }
//    }
//
////    public void newLevel(int level) {
////        if (currentLevel != level) {
////            System.out.println("delete: c: " + currentLevel + ", l:" + level);
//////            System.out.println(level + "," + currentLevel);
////            int preLevel = currentLevel;
////            currentLevel = level;
////            limit[currentLevel] = limit[preLevel];
////
////            for (int i = limit[currentLevel]; i >= 0; --i) {
////                final int offset = index[i];
////                words[currentLevel][offset] = words[preLevel][offset];
////            }
////        }
////    }
//
//    public void deleteLevel(int level) {
//////        System.out.println("delete: c: " + currentLevel + ", l:" + level);
////        limit[currentLevel] = -1;
////        preLevel = --currentLevel;
////        while (limit[preLevel] == -1) {
////            preLevel--;
////        }
//////        currentLevel = level;
//
//        limit[level] = -1;
//        preLevel = --level;
//        while (limit[preLevel] == -1) {
//            --preLevel;
//        }
//        currentLevel = preLevel;
//
//        ///tips:还有待坐有优化空间
//////        currentLevel =
////        //回溯后currentLevel失效
////        if (currentLevel >= level){
////            preLevel = --level;
////            while (limit[preLevel] == -1) {
////                --preLevel;
////            }
////            currentLevel = preLevel;
////            limit[level] = -1;
////        }
//
//    }
//
//    public void BackToLevel(int level) {
////        if (currentLevel == level) {
////            currentLevel = preLevel;
////        } else {
////            currentLevel
////        }
//        limit[level] = -1;
//        preLevel = --level;
//        while (limit[preLevel] == -1) {
//            --preLevel;
//        }
//        currentLevel = preLevel;
//    }
//
//    public boolean isEmpty() {
//        return limit[currentLevel] == -1;
//    }
//
//    public void clearMask() {
//        for (int i = 0, currentLimit = limit[currentLevel]; i <= currentLimit; ++i) {
//            final int offset = index[i];
//            mask[offset] = 0L;
//        }
//    }
//
//    public void reverseMask() {
//        for (int i = 0, currentLimit = limit[currentLevel]; i <= currentLimit; ++i) {
//            final int offset = index[i];
//            mask[offset] = ~mask[offset];
//        }
//    }
//
//    public void addToMask(long[] m) {
//        for (int i = 0, currentLimit = limit[currentLevel]; i <= currentLimit; i++) {
//            final int offset = index[i];
//            mask[offset] = mask[offset] | m[offset];
//        }
//    }
//
//    public void intersectWithMask() {
//        long w, currentWords;
////        int currentLimit = limit[currentLevel];
//
//        for (int i = limit[currentLevel]; i >= 0; --i) {
//            final int offset = index[i];
//            currentWords = words[currentLevel][offset];
//            w = currentWords & mask[offset];
//            if (w != currentWords) {
//                words[currentLevel][offset] = w;
//                if (w == 0L) {
//                    index[i] = index[limit[currentLevel]];
//                    index[limit[currentLevel]] = offset;
//                    --limit[currentLevel];
//                }
//            }
//        }
//    }
//
//    public int intersectIndex(long[] m) {
//        for (int i = 0, currentLimit = limit[currentLevel]; i <= currentLimit; i++) {
//            final int offset = index[i];
//            if ((words[currentLevel][offset] & m[offset]) != 0L) {
//                return offset;
//            }
//        }
//        return -1;
//    }
//
//    public void show() {
//        System.out.print("id = " + id + ", level = " + currentLevel + " ");
//        for (int i = 0; i < numBit; i++) {
//            System.out.printf("%x ", words[currentLevel][i]);
//        }
//        System.out.println();
//    }
//}
