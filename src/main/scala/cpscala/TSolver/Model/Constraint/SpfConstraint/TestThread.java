package cpscala.TSolver.Model.Constraint.SpfConstraint;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class TestThread extends Thread {
    //public class TestThread implements Runnable {
    //public class TestThread implements Runnable {
    static final int iniState = 0;
    static final int utState = 1;
    static final int fdState = 2;
    static final int p = 3;
    static int[] a = IntStream.range(0, 10000000).toArray();

    private int id;
    // state: 0代表ini, 1代表ut, 2代表fd
    private int state;

    private double psum;


    public TestThread(int id, int state) {
        this.id = id;
        this.state = state;
        this.psum = 0;
    }

    public void setState(int state) {
        this.state = state;
    }


    public void initial() {
        float step = (float) a.length / p;
        int i = (int) (id * step);
        int end = (int) ((id + 1) * step);
        while (i < end) {
            psum += (double) 1 / a[i];
            i++;
        }
    }

    public void updateTable() {
        System.out.println("updateTable-" + id);
    }

    public void filterDomain() {
        System.out.println("pfilterDomain-" + id);
    }

    @Override
    public void run() {
        switch (state) {
            case iniState:
                initial();
                break;
            case utState:
                updateTable();
                break;
            case fdState:
                filterDomain();
                break;
        }
    }

    public double getPsum() {
        return psum;
    }

    public static void main(String[] args) {

        double sum = 0;

        long startTime = System.nanoTime();
        for (int i = 0; i < a.length; i++) sum += (double) 1 / a[i];
        long endTime = System.nanoTime();
        System.out.println("sum: " + sum + " time: " + (endTime - startTime) * 1e-9 + "s");

        sum = 0;

        TestThread[] thread = new TestThread[p];
        for (int id = 0; id < p; id++) {
            thread[id] = new TestThread(id, iniState);
        }

        ForkJoinPool pool = new ForkJoinPool(p);

        startTime = System.nanoTime();
        for (int id = 0; id < p; id++) {
            pool.execute(thread[id]);
//            thread[id].run();
        }

        pool.awaitQuiescence(1, TimeUnit.DAYS);
//        for (int id = 0; id < p; id++) {
//            try {
//                thread[id].join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }


        for (int id = 0; id < p; id++) {
            sum += thread[id].getPsum();
        }

        endTime = System.nanoTime();
        System.out.println("sum: " + sum + " time: " + (endTime - startTime) * 1e-9 + "s");

    }


}
