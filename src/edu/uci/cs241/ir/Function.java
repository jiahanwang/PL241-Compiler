package edu.uci.cs241.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hanplusplus on 2/24/15.
 */
public class Function {

    public String name;

    public Map<String, Integer> parameters;
    public Map<String, Integer> local_variables;
    public Map<String, Array> arrays;

    public BasicBlock entry;
    public BasicBlock exit;

    public int start_line;
    public int end_line;

    public boolean has_return;

    public Function(String name){
        this.name = name;
        this.parameters = new HashMap<String, Integer>();
        this.local_variables = new HashMap<String, Integer>();
        this.arrays = new HashMap<String, Array>();
        this.entry = null;
        this.exit = null;
        this.start_line = 0;
        this.end_line = 0;
        this.has_return = false;
    }

    // can be only called only
    public void addAllParameters(List<String> params) throws Exception {
        if(this.parameters.size() == 0) {
            for (String name : params) {
                if (this.parameters.containsKey(name)) {
                    throw new Exception("Duplicate Parameter Definition " + name + "in Function " + this.name);
                } else {
                    this.parameters.put(name, 0);
                }
            }
        }
    }

    public void addLocalVariable(String ident) throws Exception {
        if(this.local_variables.containsKey(ident)){
            throw new Exception("Duplicate Declaration of " + ident + " in Function " + this.name);
        }else{
            this.local_variables.put(ident, 0);
        }
    }

    public void addArray(Array arr) throws Exception {
        if(this.arrays.containsKey(arr.name)){
            throw new Exception("Duplicate Declaration of Array" + arr.name + " in Function " + this.name);
        }else{
            this.arrays.put(arr.name, arr);
        }
    }

    public boolean checkArray(String name){
        return this.arrays.containsKey(name);
    }

    public boolean checkLocalVariable(String name){
        return this.local_variables.containsKey(name);
    }


}
