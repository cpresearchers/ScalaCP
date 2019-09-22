package cpscala.TSolver.Model.Solver.CPFSolver;

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper;
import cpscala.XModel.XModel;

import java.util.ArrayList;


//CPF从搜索入口开始的多线程
public class CPF_MultiThread_S extends  CPFSolverImpl{


    public CPF_MultiThread_S(XModel xm , String varType, String heuName, SearchHelper sear)
    {
        super(xm, varType, heuName,sear);

    }

    public  boolean Search(long limit_time, int start,int stride) {
        boolean result = true;
        long start_time = System.nanoTime();


        int[] table_flag = new int[Path.size()];
        table_flag[0] = 0;

        int[] solution = new int[vsize];

        int[][] for_find = new int[vsize][];
        for (int i = 0; i < Path.size(); ++i)
            for_find[i] = new int[Path_Diff.get(i).same_id.size()];
        flag_for_Solution = false;
        int level = 0;
        int lastlevel = 0;
        ArrayList<Integer> p = null;
        ArrayList<Integer> for_check = new ArrayList<Integer>();
        long current_Time = 0;

        while (table_flag[0] < hm.tabs.get(Path.get(0).id).tuples.length && !flag_for_Solution) {

            current_Time = System.nanoTime();
            if (current_Time - start_time > limit_time) {
                result = false;
                break;
            }

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
                continue;

            }
            if (Check_Filter(solution, level, for_check)) {

                node++;
                lastlevel = level;
                level++;

                if (level == Path.size()) {
                    flag_for_Solution = true;
                    for (var v : solution
                    ) {
                        s.add(v);
                    }
                    break;

                          /*level--;

                          print_All(solution);
                          if(solution[0] == 11 && solution[1] == 9 && solution[2] == 7 && solution[3] == 4 )
                              flag_for_Solution = true;*/
                }

            }


        }
        if (result) {
            result = flag_for_Solution;

        }
        return result;
    }

    public Boolean Solve(int num_thread,long limit_time)
    {

        if(num_thread < 3)
             return false;


        return true;


    }


}
