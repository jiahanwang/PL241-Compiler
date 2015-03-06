package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;

import java.util.*;

/**
 * Created by hanplusplus on 2/24/15.
 */
public class Function {

    public String name;

    //public Map<String, Integer> parameters;

    public int parameter_size;

    public SymbolTable symbolTable;

    public BasicBlock entry;

    public IR ir;

    public boolean has_return;

    public boolean predefined;

    public LinkedList<Map<String, PhiFunction>> ssa_stack;

    public Function(String name){
        this.name = name;
        this.parameter_size = 0;
        this.symbolTable = new SymbolTable();
        this.entry = null;
        this.ir = new IR();
        this.has_return = false;
        this.predefined = false;
        //SSA
        this.ssa_stack = new LinkedList<Map<String, PhiFunction>>();
        this.ssa_stack.add(new HashMap<String, PhiFunction>());
    }

    // can be only added once
    public boolean addAllParameters(List<String> params) throws Exception {
        if(this.parameter_size == 0) {
            for (String param : params) {
                this.symbolTable.addVariable(param);
                // add to the initial phi map
                this.ssa_stack.get(0).put(param, new PhiFunction(param, 0, 0));
                this.parameter_size++;
            }
            return true;
        }else{
            return false;
        }
    }

    public void addVariable(String ident) throws Exception {
        this.symbolTable.addVariable(ident);
        // add to the initial phi map
        this.ssa_stack.get(0).put(ident, new PhiFunction(ident, 0, 0));
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

    // copy the current top phi map and add it to the top of stack
    public Map<String, PhiFunction> copyAddPhiMap(){
        Map<String, PhiFunction> current_phi_map = this.ssa_stack.peek();
        Map<String, PhiFunction> new_phi_map = new HashMap<String, PhiFunction>();
        for(PhiFunction phi : current_phi_map.values()){
            new_phi_map.put(phi.id, (PhiFunction)phi.clone());
        }
        this.ssa_stack.push(new_phi_map);
        return new_phi_map;
    }

    public Array getArray(String indent){
        return this.symbolTable.arrays.get(indent);
    }

    public Function getFunction(String indent){ return this.symbolTable.functions.get(indent); }

    public List<Function> getFunctions(){
        return new ArrayList<Function>(this.symbolTable.functions.values());
    }

    public PhiFunction getPhiFunction(String name){
        return this.ssa_stack.peek().get(name);
    }

    public boolean checkArray(String name){
        return this.symbolTable.checkArray(name);
    }

    public boolean checkVariable(String name){ return this.symbolTable.checkVariable(name); }

    public boolean checkFunction(String name){
        return this.symbolTable.checkFunction(name);
    }


}
