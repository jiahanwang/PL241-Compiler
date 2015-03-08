package edu.uci.cs241.frontend;

import edu.uci.cs241.ir.*;
import edu.uci.cs241.ir.types.*;
import edu.uci.cs241.ir.Result;

import java.util.*;

/**
 * Created by Ivan on 1/31/2015.
 */
public class Parser {

    // Scanner and token
    private int in; //the current currToken on the input
    private Scanner s;
    private int tokenCount;

    // Def-Use Chain
    //private DefUseChain du;

    private Function main;

    private Function current_func;

    // Global flags
    private boolean parsing_function;
    private boolean parsing_variable;
    private boolean parsing_rest;
    private boolean parsing;

    // Settings
    public boolean arrayOverflowCheck;

    // SSA
    private boolean left_side;

    // Constructor
    public Parser(String path) throws Exception {
        // initialization
        this.s = new Scanner(path);
        this.tokenCount = 0;

        // Predefined functions
        this.main = new Function("main");
        Function func = new Function("InputNum");
        func.has_return = true;
        func.predefined = true;
        this.main.addFunction(func);
        func = new Function("OutputNum");
        try {
            func.addAllParameters(Arrays.asList(new String[]{"x"}));
        } catch (Exception e) {
            e.printStackTrace();
        }
        func.predefined = true;
        this.main.addFunction(func);
        func = new Function("OutputNewLine");
        func.predefined = true;
        this.main.addFunction(func);
        this.current_func = null;

        this.parsing_function = false;
        this.parsing_variable = false;
        this.parsing_rest = false;
        this.parsing= false;

        this.arrayOverflowCheck = false;

        this.left_side = false;

        //initialize the first token
        next();
    }

    /**
     * "Main" method of parser
     *
     * **/
    public Function computation() throws Exception {
        BasicBlock b = new BasicBlock("main");
        BasicBlock current = b;
        if (accept(Token.mainToken)) {
            next();
            this.parsing = true;
            // Parsing variables
            this.parsing_variable = true;
            while (accept(Token.varToken) || accept(Token.arrToken)) {
                BasicBlock.merge(current, varDecl());
            }
            this.parsing_variable = false;

            // Parsing functions
            while (accept(Token.funcToken) || accept(Token.procToken)) {
                this.parsing_function = true;
                funcDecl();
                this.parsing_function = false;
            }

            // Parsing the rest of the program
            this.parsing_rest = true;
            if (accept(Token.beginToken)) {
                next();
                //if token is not }, must be statSequence option.
                if (!accept(Token.endToken)) {
                    BasicBlock body = statSequence();
                    current.left = body;
                    current = current.left.exit;
                    b.dom.add(body);
                }
                if (accept(Token.endToken)) {
                    next();
                } else {
                    error("Missing closing bracket for main");
                }
            } else {
                error("Missing open bracket for main");
            }
            this.parsing = false;
        } else {
            error("Missing main");
        }
        // add end node
        current.left = end();
        main.entry = b;
        // kill the empty join node of while in some cases
        List<Function> funcs = new ArrayList<Function>();
        funcs.add(main);
        funcs.addAll(main.getFunctions());
        boolean[] explored = new boolean[1000000];
        for(Function func : funcs){
            killWhileJoin(func.entry, null, explored);
        }
        // Reset static counters for next file
        staticReset();
        return main;
    }

    /**
     *
     * Helper methods
     *
     * **/
    private Function getCurrentFunction(){
        return this.parsing_function ? this.current_func : this.main;
    }

    private void staticReset() {
        BasicBlock.count = 0;
    }

    public int getStart(int a, int b, int c){
        if(a != Integer.MIN_VALUE) return a;
        if(b != Integer.MIN_VALUE) return b;
        return c;
    }

    private void killWhileJoin(BasicBlock b, BasicBlock parent, boolean[] explored) {
        if(explored[b.id]) {
            return;
        }
        if (b != null) {
            if(b.getStart() == Integer.MIN_VALUE && b.name.toLowerCase().equals("join")){
                // empty while join can only be the right of while and it only has left pointer
                parent.right = b.left;
            }
            explored[b.id] = true;
        }
        if (b.left != null) {
            killWhileJoin(b.left, b, explored);
        }
        if (b.right != null) {
            killWhileJoin(b.right, b, explored);
        }
    }

    /**
     *
     * BEGIN RULES FOR PL241
     *
     * **/

     /**
     * RelOp
     *
     * */
     public ConditionType relOp() throws Exception {
        switch(in) {
            // relation operators
            case 20: next(); return ConditionType.EQ;
            case 21: next(); return ConditionType.NE;
            case 22: next(); return ConditionType.LT;
            case 23: next(); return ConditionType.GE;
            case 24: next(); return ConditionType.LE;
            case 25: next(); return ConditionType.GT;
            default:
                error("Invalid relational operator found");
        }
        return null;
    }


    /**
     * Ident
     *
     * return type: String
     *
     * **/
    public String ident() throws Exception {
        String name = null;
        if(accept(Token.ident)) {
            name = s.getIdent();
            next();
        } else {
            error("Missing identifier");
        }
        return name;
    }

    /**
     *
     * Number
     * result type: const
     * storage location: value
     *
     */
    public Result number() throws Exception {
        Result res = new Result();
        res.type = ResultType.CONST;
        if(accept(Token.number)) {
            res.value = s.number;
            next();
        } else {
            error("Missing number");
        }
        return res;
    }

    /**
     * Designator
     *
     * if it is array, designator will add instructions, thus it will return  the address of array element if write (store),
     *                 line of INST if read (load)
     * if variable, designator just return the variable name
     *
     */
    public Result designator() throws Exception {
        Result res = new Result();
        if(accept(Token.ident)) {
            String name = ident();
            // if it is array
            if(accept(Token.openbracketToken)){
                // check if this array ident has been declared
                Array arr = null;
                IR current_ir = this.getCurrentFunction().ir;
                if(this.parsing_function){
                    if(!this.current_func.checkArray(name)) {
                        if(!this.main.checkArray(name)) {
                            error("Undefined Array " + name + " ");
                        } else {
                            arr = this.main.getArray(name);
                        }
                    } else {
                        arr = this.current_func.getArray(name);
                    }
                }else{
                    if(!this.main.checkArray(name))
                        error("Undefined Array " + name + " ");
                    else
                        arr = this.main.getArray(name);
                }
                int i = 0, dimension, current_line, last_line = Integer.MIN_VALUE;
                Result x = null;
                while (accept(Token.openbracketToken)) {
                    next();
                    x = expression();
                    if(accept(Token.closebracketToken)) {
                        next();
                    } else {
                        error("Missing close bracket in designator");
                    }
                    dimension = arr.dimensions.get(i++);
                    // if it's constant, then we can check overflow
                    if(this.arrayOverflowCheck) {
                        if (x.type == ResultType.CONST && (x.value < 0 || x.value >= dimension)) {
                            error("The " + (i + 1) + "Dimension of Array " + name + "Overflows");
                        }
                    }
                    // one dimension array
                    if(arr.dimensions.size() == 1) break;
                    Instruction in = new Instruction(InstructionType.MUL);
                    in.addOperand(OperandType.CONST, String.valueOf(dimension - 1));
                    in.addOperandByResultType(x);
                    current_line = current_ir.addInstruction(in);
                    // this is the first iteration, set start_line of res
                    if (i == 1) {
                        last_line = current_line;
                    } else {
                        Instruction next_in = new Instruction(InstructionType.ADD);
                        next_in.addOperand(OperandType.INST, String.valueOf(current_line));
                        next_in.addOperand(OperandType.INST, String.valueOf(last_line));
                        last_line = current_ir.addInstruction(next_in);
                    }
                    if (res.start_line == Integer.MIN_VALUE) {
                        res.start_line = x.start_line != Integer.MIN_VALUE ? x.start_line : last_line;
                    }
                }

                // add arr retrieve instruction
                Instruction retrieve_arr = new Instruction(InstructionType.ADDA);
                retrieve_arr.addOperand(OperandType.BASE_ADDRESS, name);
                if(arr.dimensions.size() == 1){
                    retrieve_arr.addOperandByResultType(x);
                }else {
                    retrieve_arr.addOperand(OperandType.INST, String.valueOf(last_line));
                }

                if(!left_side){
                    // just load from this address
                    Instruction load = new Instruction(InstructionType.LOAD);
                    int start_Line = current_ir.addInstruction(retrieve_arr);
                    load.addOperand(OperandType.ARR_ADDRESS, String.valueOf(start_Line));
                    res.type = ResultType.ARR;
                    res.address = current_ir.addInstruction(load);
                    if(arr.dimensions.size() == 1){
                        res.start_line = x.start_line != Integer.MIN_VALUE ? x.start_line : start_Line;
                        res.end_line = res.address;
                    }else{
                        res.end_line = res.line;
                    }
                }else{
                    // left side of assignment, return address for assignment to use in STORE
                    res.type = ResultType.ARR;
                    res.address = current_ir.addInstruction(retrieve_arr);
                    if(arr.dimensions.size() == 1){
                        res.start_line = x.start_line != Integer.MIN_VALUE ? x.start_line : res.address;
                        res.end_line = res.address;
                    }else{
                        res.end_line = res.address;
                    }
                }
            // if it is variable
            }else{
                // check if variable has been defined
                if(this.parsing_function){
                    if(!this.current_func.checkVariable(name) && !this.main.checkVariable(name))
                        error("Undefined Variable " + name + " ");
                }else{
                    if(!this.main.checkVariable(name))
                        error("Undefined Variable " + name + " ");
                }

                /**  SSA  **/
                res.type = ResultType.VAR;
                Map<String, PhiFunction> current_phi_map = this.getCurrentFunction().ssa_stack.peek();
                PhiFunction current_phi = current_phi_map.get(name);
                int which;
                if(current_phi == null){
                    // non-local reference
                    res.name = name;
                }else {
                    if (left_side) {
                        which = ++current_phi.current;
                    } else {
                        which = current_phi.last;
                    }
                    res.name = name + "_" + which;
                }
            }
        } else {
            error("Missing identifier from designator");
        }
        return res;
    }

    /**
     *
     * Factor
     * return type: depends
     * storage location : depends
     *
     */
    public Result factor() throws Exception {
        Result res = null;
        if(accept(Token.ident)) {
            // to deal with variable in array's brackets
            this.left_side = false;
            res = designator();
        }
        else if(accept(Token.number)) {
            //
            res = number();
        }
        else if(accept(Token.openparenToken)) {
            next();
            res = expression();
            if(accept(Token.closeparenToken)) {
                next();
            }
        }
        else if(accept(Token.callToken)) {
            res = funcCall();
        }
        else {
            error("Invalid Factor");
        }
        return res;
    }

    /**
     * Term
     *
     * return type : INST and start and end line of instruction list
     *               or the only factor's return type
     * storage location: line
     *
     * */
    public Result term() throws Exception {
        Result x = factor(), y = null;
        int last_line = Integer.MIN_VALUE;
        // if there is only one factor
        if(!accept(Token.timesToken) && !accept(Token.divToken)){
            return x;
        }
        Result res = new Result();
        res.type = ResultType.INST;
        while(accept(Token.timesToken) || accept(Token.divToken)) {
            Instruction in = accept(Token.timesToken) ? new Instruction(InstructionType.MUL) : new Instruction(InstructionType.DIV);
            next();
            y = factor();
            // the first iteration
            if(last_line == Integer.MIN_VALUE) {
                in.addOperandByResultType(x);
            // the rest of iterations
            }else{
                in.addOperand(OperandType.INST, String.valueOf(last_line));
            }
            in.addOperandByResultType(y);
            last_line = this.getCurrentFunction().ir.addInstruction(in);
            res.setRange(x, y, last_line);
            x = y;
        }
        // set up result
        res.line = last_line;
        return res;
    }

    /**
     * Expression
     *
     * return type : INST
     * storage location: line
     *
     * */
    public Result expression() throws Exception {
        Result x = term(), y = null;
        int last_line = Integer.MIN_VALUE;
        // if there is only one term
        if(!accept(Token.plusToken) && !accept(Token.minusToken)){
            return x;
        }
        // if there is more than one terms
        Result res = new Result();
        res.type = ResultType.INST;
        while(accept(Token.plusToken) || accept(Token.minusToken)) {
            Instruction in = accept(Token.plusToken) ? new Instruction(InstructionType.ADD) : new Instruction(InstructionType.SUB);
            next();
            y = term();
            // the first iteration
            if(last_line == Integer.MIN_VALUE) {
                in.addOperandByResultType(x);
            // the rest of iterations
            }else{
                in.addOperand(OperandType.INST, String.valueOf(last_line));
            }
            in.addOperandByResultType(y);
            last_line = this.getCurrentFunction().ir.addInstruction(in);
            res.setRange(x, y, last_line);
            x = y;
        }
        // set up result
        res.line = last_line;
        return res;
    }

    public Result relation() throws Exception {
        Result x = expression();
        Result res = new Result();
        res.type = ResultType.COND;
        res.con = relOp();
        Result y = expression();
        // add cmp instruction
        Instruction in = new Instruction(InstructionType.CMP);
        in.addOperandByResultType(x);
        in.addOperandByResultType(y);
        int line = this.getCurrentFunction().ir.addInstruction(in);
        // set start and end lines
        res.setRange(x, y, line);
        res.line = line;
        return res;
    }

    /**
     * Assignment
     *
     * return type : BasicBlock
     *
     * */
    public BasicBlock assignment() throws Exception {
        BasicBlock block = new BasicBlock();
        if(accept(Token.letToken)) {
            next();
            /** SSA **/
            // tell designator, it is left side the assignment
            this.left_side = true;
            Result d_res = designator();
            if(accept(Token.becomesToken)) {
                next();
                IR current_ir = this.getCurrentFunction().ir;
                /** SSA **/
                // tell designator (if used), it is the right side of the assignment
                this.left_side = false;
                Result e_res = expression();
                // if left side is array, use STORE instead of MOVE
                Instruction assi = d_res.type == ResultType.ARR ? new Instruction(InstructionType.STORE) : new Instruction(InstructionType.MOVE);
                assi.addOperandByResultType(e_res);
                assi.addOperandByResultType(d_res);
                int last_line = current_ir.addInstruction(assi);
                for(int i = this.getStart(d_res.start_line, e_res.start_line, last_line); i <= last_line; i++){
                    block.addInstruction(current_ir.ins.get(i));
                }
                /** SSA **/
                // Finished assignment, update last
                if(d_res.type == ResultType.VAR) {
                    String variable = d_res.name.split("_")[0];
                    PhiFunction phi = this.getCurrentFunction().getPhiFunction(variable);
                    if (phi != null) {
                        phi.last = phi.current;
                    }// otherwise, non-local reference
                }// otherwise, array

            } else {
                error("Missing becomes token during assignment");
            }
        } else {
            error("Missing let token during assignment");
        }
        return block;
    }

    /**
     * FuncCall
     *
     * return type : Result.INST
     * storage location: line
     *
     * */
    public Result funcCall() throws Exception {
        Result res = new Result();
        res.type = ResultType.FUNC_CALL;
        if(accept(Token.callToken)) {
            next();
            String name = ident();
            if(!this.main.checkFunction(name)){
                error("Function " + name + " is not declared");
            }
            res.func_name = name;
            Function func = this.main.getFunction(name);
            Instruction in = null;
            // if predefined functions
            if(func.predefined){
                if(name.equals("InputNum")) {
                    in = new Instruction(InstructionType.READ);
                }
                if(name.equals("OutputNum")) {
                    in = new Instruction(InstructionType.WRITE);
                }
                if(name.equals("OutputNewLine")) {
                    in = new Instruction(InstructionType.WLN);
                }
            } else {
                in = new Instruction(InstructionType.FUNC);
                in.addOperand(OperandType.FP, func.name);
            }
            int i = func.parameter_size;
            Result x = null, y = null;
            if(accept(Token.openparenToken)) {
                next();
                if(!accept(Token.closeparenToken)) {
                    x = expression();
                    if(--i < 0)
                        error("Function " + name + " only take " + func.parameter_size + " parameters");
                    in.addOperandByResultType(x);
                    res.setRange(x, null, Integer.MIN_VALUE);
                    // more than one parameters
                    while(accept(Token.commaToken)) {
                        next();
                        y = expression();
                        if(--i < 0)
                            error("Function " + name + " only take " + func.parameter_size + " parameters");
                        in.addOperandByResultType(y);
                        res.setRange(x, y, res.line);
                        x = y;
                    }
                }
                if(accept(Token.closeparenToken)) {
                    next();
                } else {
                    error("Missing close paren in func call");
                }
            }
            res.line = this.getCurrentFunction().ir.addInstruction(in);
            res.setRange(null, null, res.line);
            // add last parameter to tell function where to return to
            if(!func.predefined) {
                in.addOperand(OperandType.FUNC_RETURN_PARAM, String.valueOf(res.line));
            }

        }
        return res;
    }


    public BasicBlock ifStatement() throws Exception {
        BasicBlock b = new BasicBlock();
        b.has_branching = true;
        b.type = BasicBlockType.IF;
        BasicBlock join = new BasicBlock("join");
        (b.dom).add(join);
        b.right = join;
        b.exit = join;
        b.join = join;
        if(accept(Token.ifToken)) {
            next();
            IR current_ir =  this.getCurrentFunction().ir;
            Result r_res = relation();
            // add first instruction
            Instruction first = Instruction.createInstructionByConditionType(r_res.con);
            first.addOperandByResultType(r_res);
            current_ir.addInstruction(first);
            // add second instruction
            Instruction second = Instruction.createInstructionByConditionType(r_res.con.opposite());
            second.addOperandByResultType(r_res);
            //b.end_line = current_ir.addInstruction(second);
            current_ir.addInstruction(second);
            // Set Instructions in Block
            // relation has already added instructions
            for(int i = r_res.start_line, len = r_res.end_line + 2; i <= len; i++){
                b.addInstruction(current_ir.ins.get(i));
            }

            Function current_func = this.getCurrentFunction();
            /** SSA **/
            // Copy and push into stack
            Map<String, PhiFunction> current_phi_map = current_func.copyAddPhiMap();
            if(accept(Token.thenToken)) {
                next();
                b.left = statSequence();
                (b.dom).add(b.left);
                first.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.getStart()));
                /** SSA **/
                // save left
                for(PhiFunction phi : current_phi_map.values()){
                    phi.operands.add(phi.last);
                    phi.lead = ++phi.current;
                    phi.last = phi.original;
                }
                // if there is else
                if (accept(Token.elseToken)) {
                    next();
                    Instruction left_exit = new Instruction(InstructionType.BRA);
                    //
                    current_ir.addInstruction(left_exit);
                    b.left.exit.addInstruction(left_exit);
                    b.right = statSequence();
                    (b.dom).add(b.right);
                    second.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.right.getStart()));
                    left_exit.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.right.exit.getEnd() + 1));
                // if no else
                }else{
                    second.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.exit.getEnd() + 1));
                }
                /** SSA **/
                // save right, add it no matter there is else or not
                for(PhiFunction phi : current_phi_map.values()){
                    phi.operands.add(phi.last);
                }
                if(accept(Token.fiToken)) {
                    next();
                    /** SSA **/
                    // create phi function for join
                    List<Instruction> phis = new ArrayList<Instruction>();
                    Map<String, PhiFunction> pre_phi_map = current_phi_map;
                    this.getCurrentFunction().ssa_stack.pop();
                    current_phi_map = this.getCurrentFunction().ssa_stack.peek();
                    for(PhiFunction phi : pre_phi_map.values()) {
                        PhiFunction cur_phi = current_phi_map.get(phi.id);
                        // both branches do not change
                        if ((phi.operands.get(0) == phi.operands.get(1)) && (phi.operands.get(1) == phi.original)){
                            cur_phi.current = phi.current - 1;
                            continue;
                        }else{
                            cur_phi.current = phi.current;
                            cur_phi.last = phi.lead;
                        }
                        phis.add(phi.toInstruction());
                    }
                    if(phis.size() == 0){
                        // nothing in join node, then as least mark size of the whole if statement
                        if(b.right == join) {
                            join.end_line = b.left.exit.getEnd();
                        }else{
                            join.end_line = b.right.exit.getEnd();
                        }
                    }else{
                        current_func.ir.addInstructions(phis);
                        join.addInstructions(phis);
                    }
                    b.left.exit.left= join;
                    b.right.exit.left = join;
                    b.exit = join;
                } else {
                    error("Missing fi token");
                }
            } else {
                error("Missing then token");
            }
        } else {
            error("Missing if token");
        }
        return b;
    }

    public BasicBlock whileStatement() throws Exception {
        BasicBlock b = new BasicBlock();
        b.has_branching = true;
        b.type = BasicBlockType.WHILE;
        BasicBlock join = new BasicBlock("join"); // if actually has nothing from while in it
        (b.dom).add(join);
        b.right = join;
        b.join = join;
        /** SSA **/
        // Copy and push into stack
        Function current_func = this.getCurrentFunction();
        Map<String, PhiFunction> current_phi_map = current_func.copyAddPhiMap();
        if(accept(Token.whileToken)) {
            next();
            IR current_ir = this.getCurrentFunction().ir;
            Result r_res = relation();
            // add first instruction
            Instruction first = Instruction.createInstructionByConditionType(r_res.con);
            first.addOperandByResultType(r_res);
            current_ir.addInstruction(first);
            // add second instruction
            Instruction second = Instruction.createInstructionByConditionType(r_res.con.opposite());
            second.addOperandByResultType(r_res);
            current_ir.addInstruction(second);
            // Set Instructions in Block
            // relation has already add instructions
            for(int i = r_res.start_line, len = r_res.end_line + 2; i <= len; i++){
                b.addInstruction(current_ir.ins.get(i));
            }
            if(accept(Token.doToken)) {
                next();
                b.left = statSequence();
                (b.dom).add(b.left);
                first.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.getStart()));
                Instruction left_exit = new Instruction(InstructionType.BRA);
                left_exit.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.getStart()));
                // set range for last join
                current_ir.addInstruction(left_exit);
                b.left.exit.addInstruction(left_exit);
                second.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.exit.getEnd() + 1));
                if(accept(Token.odToken)) {
                    next();
                    /** SSA **/
                    // create phi function
                    List<Instruction> phis = new ArrayList<Instruction>();
                    Map<String, PhiFunction> pre_phi_map = current_phi_map;
                    this.getCurrentFunction().ssa_stack.pop();
                    current_phi_map = this.getCurrentFunction().ssa_stack.peek();
                    for(PhiFunction phi : pre_phi_map.values()) {
                        PhiFunction cur_phi = current_phi_map.get(phi.id);
                        // never used
                        if (phi.current == phi.original){
                            continue;
                        }
                        cur_phi.current = ++phi.current;
                        cur_phi.last = phi.current;
                        phi.lead = phi.current;
                        phi.operands.add(phi.original);
                        phi.operands.add(phi.last);
                        phis.add(phi.toInstruction());
                    }
                    // insert into the node
                    if(phis.size() != 0) {
                        current_func.ir.insertPhiInstructions(phis, r_res.start_line);
                        b.insertInstructions(phis, 0);
                        // fix the last jump line
                        Instruction last_jump = current_func.ir.getLastInstruction();
                        last_jump.operands.get(0).line -= phis.size();
                    }
                    if(join.end_line == Integer.MIN_VALUE){
                        // to tell how deep this whole while is
                        join.end_line = b.left.exit.getEnd();
                    }
                    b.left.exit.left = b;
                    b.exit = join;
                } else {
                    error("Missing od token");
                }
            } else {
                error("Missing do token");
            }
        } else {
            error("Missing while token");
        }
        return b;
    }

    public BasicBlock returnStatement() throws Exception {
        BasicBlock b = new BasicBlock();
        if(accept(Token.returnToken)) {
            this.current_func.has_return = true;
            next();
            Result e_res = expression();
            IR current_ir = this.getCurrentFunction().ir;
            Instruction in = new Instruction(InstructionType.LOADPARAM);
            // Parameter starts from zero
            in.addOperand(OperandType.FUNC_PARAM, String.valueOf(this.current_func.parameter_size));
            int start_line = current_ir.addInstruction(in);
            in = new Instruction(InstructionType.RETURN);
            // add return value;
            in.addOperandByResultType(e_res);
            in.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(start_line));
            int last_line = current_ir.addInstruction(in);
            for(int i = getStart(e_res.start_line, start_line, 0); i <= last_line; i++){
                b.addInstruction(current_ir.ins.get(i));
            }
        } else {
            error("Missing return statement");
        }
        return b;
    }

    public BasicBlock statement() throws Exception {
        if(accept(Token.letToken)) {
            return assignment();
        }
        else if(accept(Token.callToken)) {
            IR current_ir = this.getCurrentFunction().ir;
            BasicBlock b = new BasicBlock();
            Result res = funcCall();
            //funcCall has alreay added instructions
            for(int i = res.start_line, len = res.end_line; i<= len; i++){
                b.addInstruction(current_ir.ins.get(i));
            }
            b.has_branching = false;
            return b;
        }
        else if(accept(Token.ifToken)) {
            return ifStatement();
        }
        else if(accept(Token.whileToken)) {
            return whileStatement();
        }
        else if(accept(Token.returnToken)) {
            return returnStatement();
        } else {
                error("Statement is invalid");
        }
        return null;
    }

    public BasicBlock statSequence() throws Exception {
        BasicBlock start = statement();
        BasicBlock cursor = start.exit;
        while(accept(Token.semiToken)) {
            next();
            BasicBlock next = statement();
            if(next.has_branching) {
                // if next one is if , then we can merge
                if(next.type == BasicBlockType.IF){
                    cursor.left = next.left;
                    cursor.right = next.right;
                    // since we merge the if header into, we need to reset all the parents
                    (cursor.dom).add(cursor.left);
                    (cursor.dom).add(cursor.right);
                    (cursor.dom).add(next.join);
                    BasicBlock.merge(cursor, next);
                } else {
                    //connect top and bottom
                    cursor.left = next;
                    (cursor.dom).add(cursor.left);
                }
                cursor = next.exit;
            } else {
                // just append instead
                BasicBlock.merge(cursor, next);
            }
        }
        start.exit = cursor.exit;
        return start;
    }

    public Result typeDecl() throws Exception {
        Result res = new Result();
        if (accept(Token.varToken)) {
            res.type = ResultType.VAR;
            next();
        }
        if(accept(Token.arrToken)) {
            res.type = ResultType.ARR;
            List<Integer> dimensions = new LinkedList<Integer>();
            next();
            while(accept(Token.openbracketToken)) {
                next();
                dimensions.add(number().value);
                if(accept(Token.closebracketToken)) {
                    next();
                } else {
                    error("Missing close parenthesis in type declaration");
                }
            }
            res.dimensions = new LinkedList<Integer>(dimensions);
        }
        return res;
    }

    public BasicBlock varDecl() throws Exception {
        // return BasicBlock but it has nothing in it
        BasicBlock block = new BasicBlock();
        Result t_res = typeDecl();
        if(t_res.type == ResultType.ARR){
            // if array
            do {
                if(accept(Token.commaToken))
                    next();
                String name = ident();
                Array arr = new Array(name);
                arr.addDimensions(t_res.dimensions);
                this.getCurrentFunction().addArray(arr);
            }while(accept(Token.commaToken));

        } else if (t_res.type == ResultType.VAR){
            // if variable
            do {
                if(accept(Token.commaToken))
                    next();
                String name = ident();
                this.getCurrentFunction().addVariable(name);
            }while(accept(Token.commaToken));
        } else {
            error("Wrong type for variable declaration");
        }

        if(accept(Token.semiToken)) {
            next();
        } else {
            error("Missing semicolon for var declaration");
        }
        return block;
    }

    public BasicBlock funcDecl() throws Exception {
        // the first block of function only contains name
        BasicBlock block = new BasicBlock();
        if(accept(Token.funcToken) || accept(Token.procToken)) {
            next();
            if(accept(Token.ident)) {
                Function func = new Function(s.getIdent());
                this.main.addFunction(func);
                this.current_func = func;
                next();
                //then formalParams MUST be following
                func.addAllParameters(formalParam());
                if(accept(Token.semiToken)) {
                    next();
                    block.left = funcBody();
                    block.name = func.name;
                    func.entry = block;
                    (block.dom).add(block.left);
                    if(accept(Token.semiToken)) {
                        next();
                    } else {
                        error("Missing ; after function body");
                    }
                } else {
                    error("Missing ; after formal parameters");
                }
            } else {
                error("Missing identifier for function declaration");
            }
        } else {
            error("Missing function or procedure heading");
        }
        return block;
    }

    public List<String> formalParam() throws Exception {
        List<String> params = new LinkedList<String>();
        if(accept(Token.openparenToken)) {
            next();
            while(!accept(Token.closeparenToken)) {
                params.add(ident());
                if(accept(Token.commaToken)) {
                    next();
                }
            }
            if(accept(Token.closeparenToken)) {
                next();
            } else {
                error("Missing close paren for formal params");
            }
        } else {
            error("Missing open paren for formal params");
        }
        return params;
    }

    public BasicBlock funcBody() throws Exception {
        BasicBlock block = null;
        while(!accept(Token.beginToken)) {
            varDecl();
        }
        if (accept(Token.beginToken)) {
            next();
            block = statSequence();
            if(!this.current_func.has_return){
                // add BRA
                IR current_ir = this.getCurrentFunction().ir;
                Instruction in = new Instruction(InstructionType.LOADPARAM);
                in.addOperand(OperandType.FUNC_PARAM, String.valueOf(this.current_func.parameter_size + 1));
                int last_line = current_ir.addInstruction(in);
                block.exit.addInstruction(in);
                in = new Instruction(InstructionType.RETURN);
                in.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(last_line));
                in.id = last_line + 1;
                current_ir.addInstruction(in);
                block.exit.addInstruction(in);
            }
            if (accept(Token.endToken)) {
                next();
            } else {
                error("Missing closing bracket for func body");
            }
        } else {
            error("Missing open bracket for func body");
        }

        return block;
    }

    public BasicBlock end() {
        Instruction in = new Instruction(InstructionType.END);
        this.getCurrentFunction().ir.addInstruction(in);
        BasicBlock b = new BasicBlock();
        b.addInstruction(in);
        return b;
    }


    /**
    *
    * All Helper Functions
    *
    * */

    private void next() {
        try {
            in = s.getSym();
            tokenCount++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean accept(Token t) {
        return in == t.value;
    }

    private void emit(String s) {
        System.out.println(s);
    }

    private void error(String e) throws Exception {
        throw new Exception("Error: " + e + " on line " + s.getLineNumber() + " near tokenNum:"
                + tokenCount + " = "+Token.getRepresentation(in));
    }


}
