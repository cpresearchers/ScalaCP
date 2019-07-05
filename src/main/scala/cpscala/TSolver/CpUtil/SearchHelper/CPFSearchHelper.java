package cpscala.TSolver.CpUtil.SearchHelper;

public class CPFSearchHelper extends SearchHelper {


    double time;




    public CPFSearchHelper(int numVars, int numTabs)
    {
        super(numVars,numTabs);
    }

    public void start()
    {
        time = 0;
    }




}
