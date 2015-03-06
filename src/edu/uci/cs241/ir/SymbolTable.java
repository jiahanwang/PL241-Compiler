package edu.uci.cs241.ir;

import sun.awt.image.ImageWatched;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by hanplusplus on 2/24/15.
 */
public class   SymbolTable {

    public Map<String, Integer> variables;

    public Map<String, Function> functions;

    public Map<String, Array> arrays;

    public SymbolTable () {
        this.variables = new HashMap<String, Integer>();
        this.functions = new HashMap<String, Function>();
        this.arrays = new HashMap<String, Array>();
    }

    public void addFunction(Function func) throws Exception {
        if(this.functions.containsKey(func.name)){
            throw new Exception("Syntax Error: function declaration " + func.name + " already existed");
        }else{
          this.functions.put(func.name, func);
        }
    }

    public void addVariable (String val) throws Exception {
        if(this.variables.containsKey(val)){
            throw new Exception("Syntax Error: variable " + val + " declaration already existed");
        }else{
            this.variables.put(val, 0);
        }
    }

    public void addArray (Array arr) throws Exception {
        if(this.arrays.containsKey(arr)){
            throw new Exception("Syntax Error: array " + arr.name + " declaration already existed");
        }else{
            this.arrays.put(arr.name, arr);
        }
    }


    public boolean checkArray(String name){
        return this.arrays.containsKey(name);
    }

    public boolean checkVariable(String name){
        return this.variables.containsKey(name);
    }

    public boolean checkFunction(String name){
        return this.functions.containsKey(name);
    }


}
