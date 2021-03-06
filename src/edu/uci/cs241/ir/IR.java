package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;

import java.util.*;

/**
 * Created by hanplusplus on 2/27/15.
 */
public class IR implements Cloneable{

    public ArrayList<Instruction> ins;

    public int count = 0;

    // easy to track use cuz IR can monitor the insertion of instructions
    // tracking of def is done in IR and varDecl() in parser
    //public DefUseChain du;

    //public int pc;


    public IR () {
        this.ins = new ArrayList<Instruction>();
        //this.pc = 0;
    }

//    public IR (DefUseChain du){
//        this();
//        this.du = du;
//    }


    public int addInstruction(Instruction in){
        in.id = count ++;
        this.ins.add(in);
        return count - 1;
    }

    public int addInstructions(List<Instruction> ins){
        int last_line = count;
        for(Instruction in : ins){
            last_line = addInstruction(in);
        }
        return last_line;
    }

    public void insertPhiInstructions(List<Instruction> ins, int start){
        Map<String, String> replace_map = new HashMap<String, String>();
        for(Instruction in : ins){
            Iterator<Operand> it = in.operands.iterator();
            String replacer = it.next().name;
            while(it.hasNext()) {
                String to_replace = it.next().name;
                if (!replace_map.containsKey(to_replace)) {
                    replace_map.put(to_replace, replacer);
                }
            }
        }
        ListIterator<Instruction> iterator = this.ins.listIterator(start);
        while(iterator.hasNext()){
            Instruction in = iterator.next();
            in.id += ins.size();
            // Update all the line number
            for(Operand operand : in.operands){
                switch(operand.type){
                    case ARR_ADDRESS:
                        operand.address += ins.size();
                        break;
                    case INST:
                    case JUMP_ADDRESS:
                    case FUNC_RETURN_PARAM:
                        operand.line += ins.size();
                        break;
                    // Update the usage in the while
                    case VARIABLE:
                        // do not replace the left side of assignment
                        if(in.operator == InstructionType.PHI || in.operator == InstructionType.MOVE && in.operands.indexOf(operand) == 1) break;
                        String name = operand.name;
                        if(replace_map.containsKey(name)){
                            operand.name = replace_map.get(name);
                        }
                }
            }
        }
        // change id of phi in
        int i = start;
        for(Instruction in : ins){
            in.id = i++;
        }
        this.ins.addAll(start, ins);
        count += ins.size();
    }

    public boolean insertInstruction(Instruction insert, int start){
        if(start < 0 || start >= this.ins.size()) return false;
        // change id of incoming ins
        BasicBlock parent = this.ins.get(start).parent;
        insert.id = start;
        insert.parent = parent;
        // insert into BB block
        int offset = start - parent.ins.get(0).id;
        parent.ins.add(offset, insert);
        // Update following lines in IR
        ListIterator<Instruction> iterator = this.ins.listIterator(0);
        while(iterator.hasNext()){
            Instruction in = iterator.next();
            if(in.id >= start)
                in.id += 1;
            for(Operand operand : in.operands){
                switch(operand.type){
                    case ARR_ADDRESS:
                        if(operand.address >= start)
                            operand.address += 1;
                        break;
                    case INST:
                    case JUMP_ADDRESS:
                    case FUNC_RETURN_PARAM:
                        if(operand.line >= start)
                        operand.line += 1;
                        break;
                }
            }
        }
        // insert into IR
        this.ins.add(start, insert);
        count += 1;
        return true;
    }

    public boolean deleteInstruction(int start){
        Instruction delete = this.ins.get(start);
        BasicBlock parent = delete.parent;
        if(parent == null) {
            System.out.println(delete.toString());
        }

        List<Instruction> ins = parent.ins;
        Instruction i = ins.get(0);
        int id = i.id;


        int offset = start - delete.parent.ins.get(0).id;
        // Remove from BB
        try {
            delete.parent.ins.remove(offset);
        } catch (Exception e) {
            System.out.println("");
        }
        // Update lines in IR
        ListIterator<Instruction> iterator = this.ins.listIterator(0);
        while(iterator.hasNext()){
            Instruction in = iterator.next();
            if(in.id > start) in.id -= 1;
            for(Operand operand : in.operands){
                switch(operand.type){
                    case ARR_ADDRESS:
                        if(operand.address > start)
                            operand.address -= 1;
                        break;
                    case INST:
                    case JUMP_ADDRESS:
                    case FUNC_RETURN_PARAM:
                        if(operand.line > start)
                            operand.line -= 1;
                        break;
                }
            }
        }
        // delete from IR
        this.ins.remove(start);
        count -= 1;
        return true;
    }

    public Instruction getLastInstruction(){
        if(this.ins.size() == 0){
            return null;
        }
        return this.ins.get(this.ins.size() - 1);
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        for(int i = 0, len = ins.size(); i < len; i++){
            builder.append(i + ": ");
            builder.append(ins.get(i));
            builder.append("\n");
        }
        return builder.toString();
    }

    public static String toStringOfRange(List<Instruction> ins, int start, int end){
        if(start < 0 || end >= ins.size() || start > end)
            return null;
        StringBuilder builder = new StringBuilder();
        for(int i = start, len = end + 1; i < len; i++){
            Instruction in = ins.get(i);
            builder.append(in.id + ": ");
            builder.append(in.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    public Object clone () throws CloneNotSupportedException {
        IR newObject = new IR();
        newObject.ins = new ArrayList<Instruction>(this.ins);
        return newObject;
    }

}
