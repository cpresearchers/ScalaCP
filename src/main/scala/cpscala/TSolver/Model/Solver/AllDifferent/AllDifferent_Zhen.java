package cpscala.TSolver.Model.Solver.AllDifferent;

import cpscala.XModel.XVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class AllDifferent_Zhen extends AllDifferent{

    AllDifferent_Zhen(ArrayList<XVar> XV) {
        super(XV);
    }

    public boolean Solve()
    {


        if(!preprocess())  //必然不可解，肯定不用解了，直接返回
            return  false;

        try {
            ArrayList<Edge> Max_M = Find_Max_Match();

            //   ShowGraph();
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        generate_new_var();
        return true;
    }




}
