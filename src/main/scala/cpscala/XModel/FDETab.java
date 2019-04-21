package cpscala.XModel;

import java.util.Arrays;

public class FDETab extends XTab {

    FDETab(int id, String name, int[][] ts, FDEVar[] scp) {
        super(id, name, ts, scp);
        sort();
    }

    public void sort() {
        Arrays.sort(tuples, (Object o1, Object o2) -> {
            int[] one = (int[]) o1;
            int[] two = (int[]) o2;
            for (int i = 0; i < arity; i++) {
                if (one[i] > two[i]) {
                    return 1;
                } else if (one[i] < two[i]) {
                    return -1;
                } else {
                    continue;  //如果按一条件比较结果相等，就使用第二个条件进行比较。
                }
            }
            return 0;

        });
    }
}
