package cpscala.TSolver.Model.Solver.CPFSolver;

import cpscala.TSolver.CpUtil.SearchHelper.SearchHelper;
import cpscala.XModel.XModel;


//CPF从过滤的多线程
public class CPF_MultiThread_F extends  CPFSolverImpl{

    public CPF_MultiThread_F(XModel xm , String varType, String heuName, SearchHelper sear)
    {
        super(xm, varType, heuName,sear);

    }



}
