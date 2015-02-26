package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.ConditionType;
import edu.uci.cs241.ir.types.ResultType;

import java.util.List;

/**
 * Created by Ivan on 2/6/2015.
 */
public class Result {

    public ResultType type;       // const, var, reg, arr or cond
    public ConditionType con;    // if condition

    public int value;      // if constant
    public int regno;      // register number, if reg
    public int address;    // address if var
    public String name;
    public List<Integer> dimensions; // if array

    public int fixuplocation;

    public String toString() {
        return "TYPE:"+ type.toString() +",COND:"+ con.toString() +
                ",VALUE:" + value + ",REGNO:" + regno + ",FIXUP" + fixuplocation;
    }

}
