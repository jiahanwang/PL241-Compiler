package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;

import java.util.*;

/**
 * Created by hanplusplus on 2/24/15.
 */
public class Function {

    public String name;

    public int parameter_size;

    public SymbolTable symbolTable;

    public BasicBlock entry;

    public IR ir;

    public boolean has_return;

    public boolean predefined;

    private DefUseChain du;

    public LinkedList<Map<String, PhiFunction>> ssa_stack;

    public Function(String name){
        this.name = name;
        this.parameter_size = 0;
        this.symbolTable = new SymbolTable();
        this.entry = null;
        this.ir = new IR();
        this.has_return = false;
        this.predefined = false;
        // Def Use Chain
        this.du = new DefUseChain();
        //this.du.parent_func = this;
        //SSA
        this.ssa_stack = new LinkedList<Map<String, PhiFunction>>();
        this.ssa_stack.add(new HashMap<String, PhiFunction>());
    }

    public DefUseChain getDu() {
        // during parsing, we need to add defs, just return du
        //if(during_parsing) return this.du;
        // after parsing, if du has been built then just return
        this.du.reset();
        // after parsing, need to go through IR to generate the chain and then return du
        // def has been added in assignment() and varDecl() during parsing
        /** Def Use Chain **/
        // Add defs ans uses
        for (Instruction in : ir.ins) {
            switch (in.operator) {
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                case CMP:
                    this.du.addUse(in.operands.get(0), in);
                    this.du.addUse(in.operands.get(1), in);
                    this.du.addDef(new Operand(OperandType.INST, String.valueOf(in.id)), in);
                    break;
                case ADDA:
                    // do not track the base address of array
                    this.du.addUse(in.operands.get(1), in);
                    break;
                case LOAD:
                    // do not track address
                    //this.du.addUse(in.operands.get(0), in);
                    this.du.addDef(new Operand(OperandType.INST, String.valueOf(in.id)), in);
                    break;
                case STORE:
                    this.du.addUse(in.operands.get(0), in);
                    // do not track address
                    //this.du.addUse(in.operands.get(1), in);
                    break;
                case MOVE:
                    this.du.addUse(in.operands.get(0), in);
                    this.du.addDef(in.operands.get(1), in);
                    break;
                case BNE:
                case BEQ:
                case BLE:
                case BLT:
                case BGE:
                case BGT:
                case WRITE:
                    this.du.addUse(in.operands.get(0), in);
                    // do not track JUMP_ADDRESS
                    break;
                case READ:
                //case LOADPARAM:
                    this.du.addDef(new Operand(OperandType.INST, String.valueOf(in.id)), in);
                    // do not track JUMP_ADDRESS
                    break;
                case PHI:
                    this.du.addDef(in.operands.get(0), in);
                    for(int i = 1, len = in.operands.size(); i < len; i++){
                        this.du.addUse(in.operands.get(i), in);
                    }
                    break;
                case FUNC:
                    // track parameters excluding the hidden one
                    for(int i = 1, len = in.operands.size() - 1; i < len; i++){
                        this.du.addUse(in.operands.get(i), in);
                    }
                    this.du.addDef(new Operand(OperandType.INST, String.valueOf(in.id)), in);
                    break;
                case RETURN:
                    if(in.operands.size() > 1){
                        // if has a return value
                        this.du.addUse(in.operands.get(0), in);
                    }
                    // do not track JUMP_ADDRESS
                    break;
            }
        }
        return this.du;
    }

    // can be only added once
    public boolean addAllParameters(List<String> params) throws Exception {
        if(this.parameter_size == 0) {
            for (String param : params) {
                this.symbolTable.addVariable(param, true);
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
        this.symbolTable.addVariable(ident, false);
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
        // filter predefined functions
        ArrayList<Function> res = new ArrayList<Function>();
        for(Function func : this.symbolTable.functions.values()){
            if(func.predefined) continue;
            res.add(func);
        }
        return res;
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
