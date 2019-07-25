package cpscala.test;

import java.util.Arrays;

public class test2 {
    public static void main(String[] args) {
        int[][] aa = {{0, 0}, {1, 0}, {2, 1}};
        int[] bb = {2, 0};
        var res = Arrays.binarySearch(aa, bb, (int[] x, int[] y) -> {
            for (int i = 0; i < x.length; i++) {
                if (x[i] < y[i]) {
                    return -1;
                } else if (x[i] > y[i]) {
                    return 1;
                }
            }
            return 0;
        });
        System.out.println(res);


    }
}
