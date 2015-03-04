package edu.uci.cs241.frontend;

import edu.uci.cs241.ir.*;
import edu.uci.cs241.ir.types.*;

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

    // Symbol Table
    //private SymbolTable symbolTable = new SymbolTable();

    // IR
    //public IR ir = new IR();

    private Function main;


    //private Map<Function, IR> ir_map;

    //
    private Function current_func;


    // Global flags
    private boolean parsing_function;
    private boolean parsing_variable;
    private boolean parsing_rest;
    private boolean parsing;

    // Settings
    public boolean arrayOverflowCheck;


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

        //this.ir_map = new HashMap<Function, IR>();
        //this.ir_map.put(main, new IR());

        this.current_func = null;

        this.parsing_function = false;
        this.parsing_variable = false;
        this.parsing_rest = false;
        this.parsing= false;

        this.arrayOverflowCheck = false;
        //initialize the first token
        next();
    }

//    // Getters
//    public IR getIR() {
//        if(!this.parsing){
//            return this.ir;
//        }else{
//            return null;
//        }
//    }
//
//    public List<Function> getFunctions () {
//        return new ArrayList<Function>(this.symbolTable.functions.values());
//    }

    // Helper
    private IR getCurrentIR(){
        return this.parsing_function ? this.current_func.ir : this.main.ir;
    }

    private Function getCurrentFunction(){
        return this.parsing_function ? this.current_func : this.main;
    }

    /**
     * "Main" method of parser
     *
     * **/
    public Function computation() throws Exception {
        BasicBlock b = new BasicBlock("main");
        BasicBlock current = b;
        if(accept(Token.mainToken)) {
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
                //current = current.left;
            }

            // Parsing the rest of the program
            this.parsing_rest = true;
            if (accept(Token.beginToken)){
                next();
                //if token is not }, must be statSequence option.
                if(!accept(Token.endToken)){
                    BasicBlock body = statSequence();
                    current.left = body;
                    current = current.left.exit;
                }
                if(accept(Token.endToken)) {
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
        // add end
        current.left = end();
        //
        main.entry = b;

        //staticReset();
        return main;
    }

//    private void staticReset() {
//        // reset
//        IR.count = 0;
//        BasicBlock.count = 0;
//    }

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
     * if it is array, designator will add instructions, thus it will return the start and end position
     *    in the instruction list and the address of array element
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
                if(this.parsing_function){
                    if(!this.current_func.checkArray(name)) {
                        if(!this.main.checkArray(name)) {
                            error("Undefined Array " + name + "in Function" + this.current_func.name);
                        } else {
                            arr = this.main.getArray(name);
                        }
                    } else {
                        arr = this.current_func.getArray(name);
                    }
                }else{
                    if(!this.main.checkArray(name))
                        error("Undefined Array " + name + "in Global");
                    else
                        arr = this.main.getArray(name);
                }
                int i = 0, dimension, current_line, last_line = Integer.MIN_VALUE;
                Result x= null;
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
                    if(arr.dimensions.size() == 1)
                        break;
                    Instruction in = new Instruction(InstructionType.MUL);
                    in.addOperand(OperandType.CONST, String.valueOf(dimension - 1));
                    in.addOperandByResultType(x);
                    current_line = this.getCurrentIR().addInstruction(in);
                    // this is the first iteration, set start_line of res
                    if (i == 1) {
                        //res.start_line = x.start_line == Integer.MIN_VALUE ? current_line : x.start_line;
                        last_line = current_line;
                    } else {
                        Instruction next_in = new Instruction(InstructionType.ADD);
                        next_in.addOperand(OperandType.INST, String.valueOf(current_line));
                        next_in.addOperand(OperandType.INST, String.valueOf(last_line));
                        last_line = this.getCurrentIR().addInstruction(next_in);
                    }
                    res.setRange(x, null,last_line);
                }

                // add arr retrieve instruction
                Instruction retrieve_arr = new Instruction(InstructionType.ADDA);
                retrieve_arr.addOperand(OperandType.BASE_ADDRESS, name);
                if(arr.dimensions.size() == 1){
                    retrieve_arr.addOperandByResultType(x);
                }else {
                    retrieve_arr.addOperand(OperandType.INST, String.valueOf(last_line));
                }

                // set result
                res.type = ResultType.ARR;
                res.address = this.getCurrentIR().addInstruction(retrieve_arr);
                if(arr.dimensions.size() == 1){
                    res.setRange(x, null, res.address);
                }else{
                    res.setRange(null, null, res.address);
                }
            // if it is variable
            }else{
                // check if variable has been defined
                if(this.parsing_function){
                    if(!this.current_func.checkVariable(name) && !this.main.checkVariable(name))
                        error("Undefined Variable " + name + " in Function " + this.current_func.name);
                }else{
                    if(!this.main.checkVariable(name))
                        error("Undefined Variable " + name + " in Global");
                }
                // set result
                res.type = ResultType.VAR;
                res.name = name;
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
            last_line = this.getCurrentIR().addInstruction(in);
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
            last_line = this.getCurrentIR().addInstruction(in);
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
        int line = this.getCurrentIR().addInstruction(in);
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
            Result d_res = designator();
            if(accept(Token.becomesToken)) {
                next();
                Result e_res = expression();
                Instruction assi = new Instruction(InstructionType.MOVE);
                assi.addOperandByResultType(e_res);
                assi.addOperandByResultType(d_res);
                // if it is array, designator have already added instructions
                int start = d_res.type == ResultType.ARR ? d_res.start_line : e_res.start_line;
                block.setRange(start, this.getCurrentIR().addInstruction(assi));
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
            int i = func.parameters.size();
            Result x = null, y = null;
            if(accept(Token.openparenToken)) {
                next();
                if(!accept(Token.closeparenToken)) {
                    x = expression();
                    if(--i < 0)
                        error("Function " + name + " only take " + func.parameters.size() + " parameters");
                    in.addOperandByResultType(x);
                    res.setRange(x, null, Integer.MIN_VALUE);
                    // more than one parameters
                    while(accept(Token.commaToken)) {
                        next();
                        y = expression();
                        if(--i < 0)
                            error("Function " + name + " only take " + func.parameters.size() + " parameters");
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
//            else {
//                error("Missing open paren in func call");
//            }
            res.line = this.getCurrentIR().addInstruction(in);
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
        BasicBlock join = new BasicBlock("join");
        b.right = join;
        b.exit = join;
        if(accept(Token.ifToken)) {
            next();
            Result r_res = relation();
            b.start_line = r_res.start_line;
            // add first instruction
            Instruction first = Instruction.createInstructionByConditionType(r_res.con);
            first.addOperandByResultType(r_res);
            this.getCurrentIR().addInstruction(first);
            // add second instruction
            Instruction second = Instruction.createInstructionByConditionType(r_res.con.opposite());
            second.addOperandByResultType(r_res);
            b.end_line = this.getCurrentIR().addInstruction(second);
            if(accept(Token.thenToken)) {
                next();
                b.left = statSequence();
                first.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.start_line));
                // if there is else
                if (accept(Token.elseToken)) {
                    next();
                    Instruction left_exit = new Instruction(InstructionType.BRA);
                    //
                    b.left.exit.setRange(Integer.MIN_VALUE, this.getCurrentIR().addInstruction(left_exit));
                    b.right = statSequence();
                    second.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.right.start_line));
                    left_exit.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.right.exit.end_line + 1));
                // if no else
                }else{
                    second.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.exit.end_line + 1));
                }
                if(accept(Token.fiToken)) {
                    next();
//                    // Temp: could be removed after SSA
//                    if(join.end_line == Integer.MIN_VALUE){
//                        // temporary to tell how deep this whole if is
//                        join.end_line = b.right.exit.end_line;
//                    }
                    if(b.right == join) {
                        join.end_line = b.left.exit.end_line;
                    }else{
                        join.end_line = b.right.exit.end_line;
                    }
                    b.left.exit.left= join;
                    //b.left.exit.exit = join;
                    b.right.exit.left = join;
                    //b.right.exit.exit = join;
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
        BasicBlock join = new BasicBlock("Join");
        b.right = join;
        if(accept(Token.whileToken)) {
            next();
            Result r_res = relation();
            b.start_line = r_res.start_line;
            // add first instruction
            Instruction first = Instruction.createInstructionByConditionType(r_res.con);
            first.addOperandByResultType(r_res);
            this.getCurrentIR().addInstruction(first);
            // add second instruction
            Instruction second = Instruction.createInstructionByConditionType(r_res.con.opposite());
            second.addOperandByResultType(r_res);
            b.end_line = this.getCurrentIR().addInstruction(second);
            if(accept(Token.doToken)) {
                next();
                b.left = statSequence();
                first.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.start_line));
                Instruction left_exit = new Instruction(InstructionType.BRA);
                left_exit.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.start_line));
                // set range for last join
                b.left.exit.setRange(Integer.MIN_VALUE, this.getCurrentIR().addInstruction(left_exit));
                second.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.exit.end_line + 1));
                if(accept(Token.odToken)) {
                    next();
                    // Temp: could be removed after SSA
                    if(join.end_line == Integer.MIN_VALUE){
                        // temporary to tell how deep this whole while is
                        join.end_line = b.left.exit.end_line;
                    }
                    b.left.exit.left = b;
                    //b.left.exit = join;
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
            //b.start_line = e_res.start_line;
            Instruction in = new Instruction(InstructionType.LOADPARAM);
            // Parameter starts from zero
            in.addOperand(OperandType.FUNC_PARAM, String.valueOf(this.current_func.parameters.size()));
            int last_line = this.getCurrentIR().addInstruction(in);
            b.setRange(e_res.start_line, last_line);
            in = new Instruction(InstructionType.RETURN);
            // add return value;
            in.addOperandByResultType(e_res);
            in.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(last_line));
            b.setRange(Integer.MIN_VALUE, this.getCurrentIR().addInstruction(in));
        } else {
            error("Missing return statement");
        }
        //b.exit = b;
        return b;
    }

    public BasicBlock statement() throws Exception {
        if(accept(Token.letToken)) {
            return assignment();
        }
        else if(accept(Token.callToken)) {
            BasicBlock b = new BasicBlock();
            Result res = funcCall();
            b.start_line = res.start_line;
            b.end_line = res.end_line;
            // branch to the function basic block
            b.has_branching = false;
            //b.left = this.symbolTable.functions.get(res.func_name).entry;
            //b.exit = b.left.exit;
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
                //connect top and bottom
                cursor.left = next;
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
                if(this.parsing_function){
                    // add to function scope
                    this.current_func.addArray(arr);
                }else{
                    // add to global scope
                    this.main.addArray(arr);
                }
                //next();
            }while(accept(Token.commaToken));

        } else if (t_res.type == ResultType.VAR){
            // if variable
            do {
                if(accept(Token.commaToken))
                    next();
                String name = ident();
                if(this.parsing_function){
                    // add to function scope
                    this.current_func.addVariable(name);
                }else {
                    // add to global scope
                    this.main.addVariable(name);
                }
                //next();
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
//                    BasicBlock.merge(block, funcBody());
                    func.entry = block;
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
            //BasicBlock.merge(block, varDecl());
        }
        if (accept(Token.beginToken)) {
            next();
            block = statSequence();
            //BasicBlock.merge(block, statSequence());
            if(!this.current_func.has_return){
                // add BRA
                Instruction in = new Instruction(InstructionType.LOADPARAM);
                in.addOperand(OperandType.FUNC_PARAM, String.valueOf(this.current_func.parameters.size() + 1));
                int last_line = this.getCurrentIR().addInstruction(in);
                block.exit.setRange(Integer.MIN_VALUE, last_line);
                in = new Instruction(InstructionType.RETURN);
                in.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(last_line));
                block.exit.setRange(Integer.MIN_VALUE, this.getCurrentIR().addInstruction(in));
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
        int last_line = this.getCurrentIR().addInstruction(in);
        BasicBlock b = new BasicBlock();
        b.setRange(last_line, last_line);
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
