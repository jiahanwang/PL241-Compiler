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
//    public Map<String, Integer> local_variables;
//    public Map<String, Array> arrays;

    public SymbolTable symbolTable;

    public BasicBlock entry;
    //public BasicBlock exit;

    public int start_line;
    public int end_line;

    public IR ir;

    public boolean has_return;
    public boolean predefined;

    public Function(String name){
        this.name = name;
        this.parameters = new HashMap<String, Integer>();
//        this.local_variables = new HashMap<String, Integer>();
//        this.arrays = new HashMap<String, Array>();
        this.symbolTable = new SymbolTable();
        this.entry = null;
        this.ir = new IR();
        this.has_return = false;
        this.predefined = false;
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

    public void addVariable(String ident) throws Exception {
        this.symbolTable.addVariable(ident);
    }

    public void addArray(Array arr) throws Exception {
        this.symbolTable.addArray(arr);
    }

    public void addFunction(Function func) throws Exception {
        this.symbolTable.addFunction(func);
    }

    public void addIR(IR ir) throws CloneNotSupportedException {
        // deep copy
        this.ir = (IR)ir.clone();
    }

    public Array getArray(String indent){
        return this.symbolTable.arrays.get(indent);
    }

    public Function getFunction(String indent){
        return this.symbolTable.functions.get(indent);
    }

    public List<Function> getFunctions(){
        return new ArrayList<Function>(this.symbolTable.functions.values());
    }

    public boolean checkArray(String name){
        return this.symbolTable.checkArray(name);
    }

    public boolean checkVariable(String name){
        return this.parameters.containsKey(name) || this.symbolTable.checkVariable(name);
    }

    public boolean checkFunction(String name){
        return this.symbolTable.checkFunction(name);
    }


}
