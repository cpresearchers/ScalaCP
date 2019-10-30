package cpscala.TSolver.Model.Solver.CPFSolver;

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper;
import cpscala.XModel.XModel;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;



//CPF从搜索入口开始的多线程
public class CPF_MultiThread_S extends  CPFSolverImpl{ // implements Runnable{

    private int stride;
    public AtomicInteger status = new AtomicInteger();
    public AtomicInteger node_m = new AtomicInteger();
    private AtomicBoolean m_flag_for_Solution = new AtomicBoolean();
    private AtomicBoolean writed_solution = new AtomicBoolean();
   // AtomicIntegerArray ans;

    public CPF_MultiThread_S(XModel xm , String varType, String heuName, SearchHelper sear)
    {
        super(xm, varType, heuName,sear);
        //ans = new AtomicIntegerArray(vsize);
        writed_solution.set(true);
        m_flag_for_Solution.set(false);
    }

    @Override
    public boolean Assignment(final int level, int[] solution, int[] table_flag,ArrayList<Integer> p) {

        if(level == 0)
        {

            for(int i = 0;i < Path.get(0).scope.size();++i) {
              //  if(table_flag[0] >= hm.tabs.get(Path.get(0).id).tuples.length)
                 //   return false;
                solution[Path.get(0).scope.get(i)] = hm.tabs.get(Path.get(0).id).tuples[table_flag[0]][i];
            }

            table_flag[0] += stride;
            //println( table_flag[0] );
            return true;
        }
        if(p == null)
            return false;
        int j = table_flag[level];
        if(j < p.size())
        {
            for(var i : Path_Diff.get(level).diff)
            {

                solution[Path.get(level).scope.get(i)] = hm.tabs.get(Path.get(level).id).tuples[p.get(j)][i];
            }
            table_flag[level] = j + 1;
            return true;
        }
        else
            return false;


    }

    private   boolean Search(long limit_time, int start) {

      // boolean result = true;
        long start_time = System.nanoTime();

        //println(Path.size());
        int[] table_flag = new int[Path.size()];
        table_flag[0] = start;

        int[] solution = new int[vsize];

        int[][] for_find = new int[vsize][];
        for (int i = 0; i < Path.size(); ++i)
            for_find[i] = new int[Path_Diff.get(i).same_id.size()];


        int level = 0;
        int lastlevel = 0;
        ArrayList<Integer> p = null;
        ArrayList<Integer> for_check = new ArrayList<>();
        long current_Time ;
        boolean flag_for_last = true; //万一当前的table_flag是最后一个，本该继续搜索，就因为table_flag[0] 超出范围而导致不搜索了
        while ( !m_flag_for_Solution.get() &&(flag_for_last || table_flag[0] < hm.tabs.get(Path.get(0).id).tuples.length)) {

            current_Time = System.nanoTime();
            if (current_Time - start_time > limit_time) {
                status.getAndSet(2);
                break;
            }

            //println( table_flag[0] + "  " + flag_for_Solution);
            if (lastlevel != level && level != 0 && level != Path.size()) {
                for (int i = 0; i < Path_Diff.get(level).same_id.size(); ++i)
                    for_find[level][i] = solution[Path.get(level).scope.get(Path_Diff.get(level).same_id.get(i))];
                p = Path_Index.get(level - 1).Find(for_find[level]);

            }

            if (!Assignment(level, solution, table_flag, p)) {

                if (level > 0 && (p == null || table_flag[level] >= p.size())) {
                    SetTableFlag(table_flag, level);
                    lastlevel = level;
                    level--;
                }
                if (level == 0 && table_flag[0] >= hm.tabs.get(Path.get(0).id).tuples.length)
                    flag_for_last = false;
              // continue;

            }else {
                if (Check_Filter(solution, level, for_check)) {

                    node_m.getAndIncrement();
                    lastlevel = level;
                    level++;

                    if (!m_flag_for_Solution.get() && level == Path.size()) {
                        // flag_for_Solution = ;
                        m_flag_for_Solution.set(true);
                        synchronized (this) {
                            if (writed_solution.get()) {
                                writed_solution.set(false);
                                status.getAndSet(1);

                                for (var v : solution
                                ) {
                                    s.add(v);
                                }
                            }

                            break;
                        }
                    }

                }
                else {
                    if (level == 0 && table_flag[0] >= hm.tabs.get(Path.get(0).id).tuples.length)
                        flag_for_last = false;
                }

                //println(start);
            }

        }
        if(!m_flag_for_Solution.get() && status.get() == 0)
            status.getAndSet(3);
        return m_flag_for_Solution.get();
    }
    @Override
    public boolean Answer(){

        if(m_flag_for_Solution.get())
        {
            println("Solution is :");
            print_All(s);
        }
        else
            println("no Solution!");

        return flag_for_Solution;
    }
//    void test(int i)
//    {
//        for(int j = 0; j < 100;++j)
//             println(i + " " + j);
//    }

    public int Solve(int num_thread,long limit_time) throws InterruptedException {

        if(num_thread < 3)
             return -1;
        if(num_thread > hm.tabs.get(Path.get(0).id).tuples.length)
            num_thread = hm.tabs.get(Path.get(0).id).tuples.length;
        ArrayList<Thread> all = new ArrayList<>();
        stride = num_thread;
        for(int i = 0 ; i < num_thread;++i) {
            //Search(limit_time,i,num_thread);
            int f = i;
            all.add(new Thread(() -> {

              //  System.out.println("子线程开启！" + f);
               Search(limit_time, f);
              //  test(f);
            }
                ));
        }
        for(var t : all)
            t.start();
        for(var t : all) {
            t.join();
        }
        return num_thread;

    }

//
//   @Override
//   public void run() {
//
//
//
//    }
}
