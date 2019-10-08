package cpscala.ZModel;

import java.util.LinkedHashMap;
import java.util.Map;

public class Variable extends  Instance{


    public int id;
    public String name;
    public int[] values;
    public int[] values_ori;
    public Map<Integer, Integer> values_map = new LinkedHashMap<>();
    public int size;
    public boolean isSTD;

    public Variable()
    {



    }


}
