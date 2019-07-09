package cpscala.TSolver.CpUtil.SearchHelper;

public class CPFSearchHelper extends SearchHelper {


    public double time;




    public  CPFSearchHelper(int numVars, int numTabs)
    {
        super(numVars,numTabs);
    }

    public  void  start()
    {
        time = 0;
    }

    //public double get_time(){return time;}




}

