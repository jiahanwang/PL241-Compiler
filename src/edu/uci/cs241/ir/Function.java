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

    public BasicBlock entry;

    public BasicBlock exit;

    public Function(String name){
        this.name = name;
        this.parameters = new HashMap<String, Integer>();
        this.local_variables = new HashMap<String, Integer>();
        this.entry = null;
        this.exit = null;
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
            throw new Exception("Duplicate Declaration of " + ident + " in function " + this.name);
        }else{
            this.local_variables.put(ident, 0);
        }
    }

}
