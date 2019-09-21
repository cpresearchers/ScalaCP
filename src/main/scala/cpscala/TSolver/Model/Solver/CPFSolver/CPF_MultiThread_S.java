package cpscala.TSolver.Model.Solver.CPFSolver;

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper;
import cpscala.XModel.XModel;


//CPF从搜索入口开始的多线程
public class CPF_MultiThread_S extends  CPFSolverImpl{


    public CPF_MultiThread_S(XModel xm , String varType, String heuName, SearchHelper sear)
    {
        super(xm, varType, heuName,sear);

    }

    public Boolean Solve(int num_thread,long limit_time)
    {



        return true;
    }


}
