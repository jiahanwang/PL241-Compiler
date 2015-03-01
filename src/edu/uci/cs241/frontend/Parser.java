package edu.uci.cs241.frontend;

import edu.uci.cs241.ir.*;
import edu.uci.cs241.ir.types.*;
import org.java.algorithm.graph.basics.Graph;
import org.java.algorithm.graph.basics.SimpleDirectedGraph;
import sun.reflect.generics.tree.ArrayTypeSignature;

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

    private SymbolTable symbolTable = new SymbolTable();

    // IR
    public IR ir = new IR();

    //
    private Function current_func = null;


    // Global flags
    private boolean parsing_function = false;
    private boolean parsing_variable = false;
    private boolean parsing_rest = false;
    private boolean parsing = false;


    // Constructor
    public Parser(String path) throws Exception {
        // initialization
        this.s = new Scanner(path);
        this.tokenCount = 0;
        this.parsing= false;
        //initialize the first token
        next();
    }

    // Getters
    public IR getIR() {
        if(!this.parsing){
            return this.ir;
        }else{
            return null;
        }
    }



    /**
     *
     * BEGIN RULES FOR PL241
     *
     * **/


    /**
     *
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
                    if(!this.current_func.checkArray(name))
                        error("Undefined Array " + name + "in Function" + this.current_func.name);
                    else
                        arr = this.current_func.arrays.get(name);
                }else{
                    if(!this.symbolTable.checkArray(name))
                        error("Undefined Array " + name + "in Global");
                    else
                        arr = this.symbolTable.arrays.get(name);
                }
                int i = 0, dimension, current_line, last_line = Integer.MIN_VALUE;
                // deal with nested array
                while(accept(Token.openbracketToken)){
                    next();
                    Result e_res = expression();
                    dimension = arr.dimensions.get(i++);
                    Instruction in = new Instruction(InstructionType.MUL);
                    in.addOperand(OperandType.CONST, String.valueOf(dimension - 1));
                    if(e_res.type == ResultType.CONST){
                        // if it's constant, then we can check overflow
                        if(e_res.value >= 0 && e_res.value < dimension){
                            in.addOperand(OperandType.CONST, String.valueOf(e_res.value));
                        }else{
                            error("The " + (i + 1) + "Dimension of Array " + name + "Overflows");
                        }
                    }else{
                        // otherwise, we cannot check
                        in.addOperand(OperandType.INST, String.valueOf(e_res.line));
                    }
                    current_line = ir.addInstruction(in);
                    // this is the first instruction, set start_line of res
                    if(i == 1){
                        res.start_line = current_line;
                    }
                    if(last_line != Integer.MIN_VALUE){
                        Instruction next_in = new Instruction(InstructionType.ADD);
                        next_in.addOperand(OperandType.INST, String.valueOf(current_line));
                        next_in.addOperand(OperandType.INST, String.valueOf(last_line));
                        last_line = ir.addInstruction(next_in);
                    }else{
                        last_line = current_line;
                    }
                    next();
                }

                Instruction retrieve_arr = new Instruction(InstructionType.ADDA);
                retrieve_arr.addOperand(OperandType.BASE_ADDRESS, name);
                retrieve_arr.addOperand(OperandType.INST, String.valueOf(last_line));

                // set result
                res.type = ResultType.ARR;
                res.address = ir.addInstruction(retrieve_arr);
                res.end_line = res.address;

//                if(accept(Token.closebracketToken)) {
//                    next();
//                } else {
//                    error("Missing close bracket in designator");
//                    error("Missing close bracket in designator");
//                }
            // if it is variable
            }else{
                // check if variable has been defined
                if(this.parsing_function){
                    if(!this.current_func.checkLocalVariable(name))
                        error("Undefined Variable " + name + "in Function" + this.current_func.name);
                }else{
                    if(!this.symbolTable.checkVariable(name))
                        error("Undefined Variable " + name + "in Global");
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
            last_line = this.ir.addInstruction(in);
        }
        // set up result
        setRange(res, x, y, last_line);
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
            last_line = this.ir.addInstruction(in);
        }
        // set up result
        setRange(res, x, y, last_line);
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
        int line = this.ir.addInstruction(in);
        // set start and end lines
        setRange(res, x, y, line);
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
                block.setRange(start, this.ir.addInstruction(assi));
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
            if(this.symbolTable.checkFunction(name)){
                error("Function " + name + "is not declared");
            }
            res.func_name = name;
            Function func = this.symbolTable.functions.get(name);
            Instruction in = new Instruction(InstructionType.FUNC);
            in.addOperand(OperandType.FP, func.name);

            int i = func.parameters.size();
            if(accept(Token.openparenToken)) {
                next();
                if(!accept(Token.closeparenToken)) {
                    Result e_res = expression();
                    if(--i < 0)
                        error("Function " + name + " only take " + i + "parameters");
                    res.start_line = e_res.start_line;
                    in.addOperandByResultType(e_res);
                    while(accept(Token.commaToken)) {
                        next();
                        e_res = expression();
                        if(--i < 0)
                            error("Function " + name + " only take " + i + "parameters");
                        in.addOperandByResultType(e_res);
                    }
                }
                if(accept(Token.closeparenToken)) {
                    next();
                } else {
                    error("Missing close paren in func call");
                }
            } else {
                error("Missing open paren in func call");
            }
            res.line = this.ir.addInstruction(in);
            // add last parameter to tell function where to return to
            in.addOperand(OperandType.FUNC_RETURN_PARAM, String.valueOf(res.line));
            res.end_line = res.line;
        }
        return res;
    }



    public BasicBlock ifStatement() throws Exception {
        BasicBlock b = new BasicBlock();
        b.has_branching = true;
        BasicBlock join = new BasicBlock();
        b.right = join;
        if(accept(Token.ifToken)) {
            next();
            Result r_res = relation();
            b.start_line = r_res.start_line;
            // add first instruction
            Instruction first = Instruction.createInstructionByConditionType(r_res.con);
            first.addOperandByResultType(r_res);
            this.ir.addInstruction(first);
            // add second instruction
            Instruction second = Instruction.createInstructionByConditionType(r_res.con.opposite());
            second.addOperandByResultType(r_res);
            b.end_line = this.ir.addInstruction(second);
            if(accept(Token.thenToken)) {
                next();
                b.left = statSequence();
                first.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.start_line));
                // if there is else
                if (accept(Token.elseToken)) {
                    next();
                    Instruction left_exit = new Instruction(InstructionType.BRA);
                    b.left.end_line = this.ir.addInstruction(left_exit);
                    b.right = statSequence();
                    second.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.right.start_line));
                    left_exit.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.right.end_line + 1));
                    b.right.exit = join;
                // if no else
                }else{
                    second.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.end_line + 1));
                }
                if(accept(Token.fiToken)) {
                    next();
                    b.left.exit = join;
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
        BasicBlock join = new BasicBlock();
        b.right = join;
        b.exit = join;
        if(accept(Token.whileToken)) {
            next();
            Result r_res = relation();
            b.start_line = r_res.start_line;
            // add first instruction
            Instruction first = Instruction.createInstructionByConditionType(r_res.con);
            first.addOperandByResultType(r_res);
            this.ir.addInstruction(first);
            // add second instruction
            Instruction second = Instruction.createInstructionByConditionType(r_res.con.opposite());
            second.addOperandByResultType(r_res);
            b.end_line = this.ir.addInstruction(second);
            if(accept(Token.doToken)) {
                next();
                b.left = statSequence();
                first.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.start_line));
                Instruction left_exit = new Instruction(InstructionType.BRA);
                left_exit.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.start_line));
                b.left.end_line = this.ir.addInstruction(left_exit);
                second.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(b.left.end_line + 1));
                if(accept(Token.odToken)) {
                    next();
                    b.left.exit = join;
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
            b.start_line = e_res.start_line;
            Instruction in = new Instruction(InstructionType.LOADPARAM);
            in.addOperand(OperandType.FUNC_PARAM, String.valueOf(this.current_func.parameters.size() + 1));
            int last_line = this.ir.addInstruction(in);
            in = new Instruction(InstructionType.BRA);
            in.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(last_line));
            // add return value;
            in.addOperandByResultType(e_res);
            b.end_line = this.ir.addInstruction(in);
        } else {
            error("Missing return statement");
        }
        b.exit = b;
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
            b.has_branching = true;
            b.right = this.symbolTable.functions.get(res.func_name).entry;
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
        BasicBlock cursor = start;
        while(accept(Token.semiToken)) {
            next();
            BasicBlock next = statement();
            if(next.has_branching) {
                //connect top and bottom
                cursor.left = next;
                cursor.exit = next.exit;
                cursor = cursor.exit;
            } else {
                // just append instead
                BasicBlock.merge(cursor, next);
            }
        }
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
                    current_func.addArray(arr);
                }else{
                    // add to global scope
                    symbolTable.addArray(arr);
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
                    current_func.addLocalVariable(name);
                }else {
                    // add to global scope
                    symbolTable.addVariable(name);
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
                symbolTable.addFunction(func);
                this.current_func = func;
                next();
                //then formalParams MUST be following
                func.addAllParameters(formalParam());
                if(accept(Token.semiToken)) {
                    next();
                    BasicBlock.merge(block, funcBody());
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
        BasicBlock block = new BasicBlock();
        if(!accept(Token.beginToken)) {
            BasicBlock.merge(block, varDecl());
        } else {
            if (accept(Token.beginToken)) {
                next();
                BasicBlock.merge(block, statSequence());
                if(!this.current_func.has_return){
                    // add BRA
                    Instruction in = new Instruction(InstructionType.LOADPARAM);
                    in.addOperand(OperandType.FUNC_PARAM, String.valueOf(this.current_func.parameters.size() + 1));
                    int last_line = this.ir.addInstruction(in);
                    in = new Instruction(InstructionType.BRA);
                    in.addOperand(OperandType.JUMP_ADDRESS, String.valueOf(last_line));
                    block.end_line = this.ir.addInstruction(in);
                }
                if (accept(Token.endToken)) {
                    next();
                } else {
                    error("Missing closing bracket for func body");
                }
            } else {
                error("Missing open bracket for func body");
            }
        }
        return block;
    }

    public BasicBlock computation() throws Exception {
        BasicBlock b = new BasicBlock("main");
        BasicBlock current = b;
        if(accept(Token.mainToken)) {
            next();
            this.parsing = true;
            // Parsing variables
            this.parsing_variable = true;
            while (accept(Token.varToken) || accept(Token.arrToken)) {
                BasicBlock.merge(current, varDecl());
                //current = current.left;
            }
            this.parsing_variable = false;

            // Parsing functions
            this.parsing_function = true;
            while (accept(Token.funcToken) || accept(Token.procToken)) {
                funcDecl();
                //current = current.left;
            }
            this.parsing_function = false;

            // Parsing the rest of the program
            this.parsing_rest = true;
            if (accept(Token.beginToken)){
                next();
                //if token is not }, must be statSequence option.
                if(!accept(Token.endToken)){
                    current.left = statSequence();
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
        throw new Exception("Parser encountered error "+e+" on line "+s.getLineNumber()+" near tokenNum:"
                +tokenCount+" ="+Token.getRepresentation(in));
    }

    private void setRange(Result res, Result x, Result y, int line){
        if(x.start_line == Integer.MIN_VALUE){
            if(y.start_line == Integer.MIN_VALUE){
                res.start_line = line;
            }else {
                res.start_line = y.start_line;
            }
        }else{
            res.start_line= x.start_line;
        }
        res.end_line = line;
    }
}
