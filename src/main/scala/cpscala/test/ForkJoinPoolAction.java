package cpscala.test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class ForkJoinPoolAction extends RecursiveTask<Integer> {
    private static final long serialVersionUID = -3611254198265061729L;

    public static final int threshold = 2;
    private int start;
    private int end;

    public ForkJoinPoolAction(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        System.out.println("xx");
        int sum = 0;

        //如果任务足够小就计算任务
        boolean canCompute = (end - start) <= threshold;
        if (canCompute) {
            for (int i = start; i <= end; i++) {
                sum += i;
            }
        } else {
            // 如果任务大于阈值，就分裂成两个子任务计算
            int middle = (start + end) / 2;
            ForkJoinPoolAction leftTask = new ForkJoinPoolAction(start, middle);
            ForkJoinPoolAction rightTask = new ForkJoinPoolAction(middle + 1, end);

            // 执行子任务
            leftTask.fork();
            rightTask.fork();

            //等待任务执行结束合并其结果
            int leftResult = leftTask.join();
            int rightResult = rightTask.join();

            //合并子任务
            sum = leftResult + rightResult;

        }

        return sum;
//        return 0;
    }

    public static void main(String[] args) {
        ForkJoinPool forkjoinPool = new ForkJoinPool();
        //生成一个计算任务，计算1+2+3+4
        ForkJoinPoolAction task = new ForkJoinPoolAction(1, 100);


            //执行一个任务
            Future<Integer> result = forkjoinPool.submit(task);
            try {
                System.out.println(result.get());
            } catch (Exception e) {
                System.out.println(e);
            }

    }

}