package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.ConditionType;
import edu.uci.cs241.ir.types.ResultType;

import java.util.List;

/**
 * Created by Ivan on 2/6/2015.
 */
public class Result {

    public ResultType type;       // const, var, reg, arr or cond


    public int value;      // if constant
    public String name;    // name if variable
    public int line;       // line number of IR if line
    public List<Integer> dimensions; // dimension if array
    public String func_name; // func_name if function call's result


    public int regno;      // register number, if reg
    public int address;    // address if array
    public ConditionType con;    // if condition


    public int fixuplocation;


    public int start_line = Integer.MIN_VALUE;
    public int end_line = Integer.MIN_VALUE;


}
