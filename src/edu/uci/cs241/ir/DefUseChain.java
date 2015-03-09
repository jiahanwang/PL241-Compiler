package edu.uci.cs241.ir;

import java.util.*;

/**
 * Created by ivanleung on 2/17/15.
 */
public class DefUseChain {

    public Map<String, Item> variables;
    public Map<Integer, Item> constants;
    public Map<Integer, Item> intermediates;

    // pointer to parent function, so we can check non-local reference
    //Function parent_func;

    public DefUseChain (){
        this.variables = new HashMap<String, Item>();
        this.constants = new HashMap<Integer, Item>();
        this.intermediates = new HashMap<Integer, Item>();
    }

    public void reset(){
        this.variables = new HashMap<String, Item>();
        this.constants = new HashMap<Integer, Item>();
        this.intermediates = new HashMap<Integer, Item>();
    }

    public boolean addInitalDef(String name){
        if(this.variables.containsKey(name))
            return false;
        Item item = new Item();
        // no def in item
        this.variables.put(name + "_0", item);
        return true;
    }

    public boolean addDef(Operand operand, Instruction in){
        switch(operand.type){
            case VARIABLE: {
                // non-local reference
                if (operand.global) {
                    return false;
                }
                Item item;
                if (this.variables.containsKey(operand.name)){
                    // if empty def has been added by while PHI
                    item = this.variables.get(operand.name);
                } else{
                    item = new Item();
                }
                item.def = in;
                this.variables.put(operand.name, item);
                return true;
            }
            case CONST: {
                if (this.constants.containsKey(operand.value))
                    return false;
                Item item = new Item();
                // const has no def
                this.constants.put(operand.value, item);
                return true;
            }
            case INST: {
                if (this.intermediates.containsKey(operand.line))
                    return false;
                Item item = new Item();
                item.def = in;
                this.intermediates.put(operand.line, item);
                return true;
            }
        }
        return false;
    }

    public boolean addUse(Operand operand, Instruction in){
        switch(operand.type){
            case VARIABLE: {
                // non-local reference
                if (operand.global) {
                    return false;
                }
                Item item;
                if (!this.variables.containsKey(operand.name)) {
                    // every variable has been checked by parser, this is to deal with while PHI
                    // add an empty def first
                    item = new Item();
                    this.variables.put(operand.name, item);
                } else {
                    item = this.variables.get(operand.name);
                }
                // and then add this use
                item.addUse(in);
                return true;
            }
            case CONST: {
                if (!this.constants.containsKey(operand.value)) {
                    // no def for constant, so just add it
                    addDef(operand, in);
                }
                Item item = this.constants.get(operand.value);
                item.addUse(in);
                return true;
            }
            case INST: {
                if (!this.intermediates.containsKey(operand.line))
                    return false;
                Item item = this.intermediates.get(operand.line);
                item.addUse(in);
                return true;
            }
        }
        return false;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("DU:\n");
        for(Map.Entry<String, Item> entry : this.variables.entrySet()){
            builder.append(entry.getKey() + ":  " + entry.getValue() + "\n");
        }
        //builder.append("\n");
        for(Map.Entry<Integer, Item> entry : this.constants.entrySet()){
            builder.append("#" + entry.getKey() + ":  " + entry.getValue() + "\n");
        }
        //builder.append("\n");
        for(Map.Entry<Integer, Item> entry : this.intermediates.entrySet()){
            builder.append("[" + entry.getKey() + "]:  " + entry.getValue() + "\n");
        }
        return builder.toString();
    }

    public class Item {
        public Instruction def;
        public List<Instruction> uses;
        Item(){
            def = null;
            uses = new LinkedList<Instruction>();
        }

        public String toString(){
            StringBuilder builder = new StringBuilder();
            builder.append("def: " + (def == null ? -1 : def.id) + "  uses: ");
            for(Instruction in : uses){
                builder.append(in.id).append(',');
            }
            builder.deleteCharAt(builder.length() - 1);
            return  builder.toString();
        }

        public boolean addUse(Instruction in){
            Iterator<Instruction> iterator = this.uses.iterator();
            // detect duplicates
            while(iterator.hasNext()){
                if(iterator.next().id == in.id)
                    return false;
            }
            this.uses.add(in);
            return true;
        }

    }

}
