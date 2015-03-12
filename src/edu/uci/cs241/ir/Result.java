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
    public String arr_name;


    public int regno;      // register number, if reg
    public int address;    // address if array
    public ConditionType con;    // if condition


    public int fixuplocation;


    public int start_line = Integer.MIN_VALUE;
    public int end_line = Integer.MIN_VALUE;


    public void fixRange() {
        if(this.start_line == Integer.MIN_VALUE){
            if(this.end_line == Integer.MIN_VALUE)
                return;
            else
                this.start_line = this.end_line;
        }
    }

    public void setRange(Result x, Result y, int line){
        if(this.start_line != Integer.MIN_VALUE){
            this.end_line = line;
            return;
        }
        if(x == null){
            this.start_line = line;
            this.end_line = line;
            return;
        }
        if(x.start_line == Integer.MIN_VALUE){
            if(y == null){
                this.start_line = line;
            } else if(y.start_line == Integer.MIN_VALUE){
                this.start_line = line;
            }else {
                this.start_line = y.start_line;
            }
        }else{
            this.start_line= x.start_line;
        }
        this.end_line = line;
    }

}
