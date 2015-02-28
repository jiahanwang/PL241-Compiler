package edu.uci.cs241.frontend;

import edu.uci.cs241.ir.*;
import edu.uci.cs241.ir.types.BasicBlockType;
import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;
import edu.uci.cs241.ir.types.ResultType;
import org.java.algorithm.graph.basics.Graph;
import org.java.algorithm.graph.basics.SimpleDirectedGraph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Ivan on 1/31/2015.
 */
public class Parser {

    // Scanner and token
    private int in; //the current currToken on the input
    private Scanner s;
    private int tokenCount;

    // Flag to mark if parsing has finishes or not
    private boolean parse_finished;

    // Control FLow Graph
    private Graph<BasicBlock, String> cfg;

    // Def-Use Chain
    //private DefUseChain du;

    // HashMaps
    private Map<Integer, BasicBlock> all_blocks = new HashMap<Integer, BasicBlock>();
    private Map<String, BasicBlock> func_blocks = new HashMap<String, BasicBlock>();
    private SymbolTable symbolTable = new SymbolTable();

    //
    private IR ir = new IR();
    private int pc = 0;

    //
    private Function current_func = null;


    // Global flags
    private boolean parsing_function = false;
    private boolean parsing_variable = false;
    private boolean parsing_rest = false;


    private enum IdentType {
        VAR,
        ARRAY,
        FUNC
    }
    private Map<String, IdentType> identTypeMap = new HashMap<String, IdentType>();
    private Map<String, List<Integer>> arrays = new HashMap<String, List<Integer>>();



    // Constructor
    public Parser(String path) throws Exception {
        // initialization
        this.s = new Scanner(path);
        this.tokenCount = 0;
        this.parse_finished = false;
        this.cfg = new SimpleDirectedGraph<BasicBlock, String>();
        //initialize the first token
        next();
        //computation();
        //System.out.println("Finished compiling "+s.getLineNumber()+" lines in "+path+".");
    }

    // Getters
    public Graph getCFG() {
        return parse_finished ? this.cfg : null;
    }



    /**
     *
     * BEGIN RULES FOR PL241
     *
     * **/


     public Result.Condition relOp() throws Exception {
        switch(in) {
            // relation operators
            case 20: next(); return Result.Condition.EQ;
            case 21: next(); return Result.Condition.NE;
            case 22: next(); return Result.Condition.LT;
            case 23: next(); return Result.Condition.GE;
            case 24: next(); return Result.Condition.LE;
            case 25: next(); return Result.Condition.GT;
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
        Result res = null;
        if(accept(Token.ident)) {
            String name = ident();
            // if it is array
            if(accept(Token.openbracketToken)){
                // check if this array ident has been declared
                if(this.parsing_function){
                    if(!this.current_func.checkArray(name))
                        error("Undefined Array " + name + "in Function" + this.current_func.name);
                }else{
                    if(!this.symbolTable.checkArray(name))
                        error("Undefined Array " + name + "in Global");
                }
                int i = 0, dimension, current_line, last_line = Integer.MIN_VALUE;
                Array arr = this.current_func.arrays.get(name);
                // deal with nested array
                do{
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
                }while(accept(Token.openbracketToken));

                Instruction retrieve_arr = new Instruction(InstructionType.ADDA);
                retrieve_arr.addOperand(OperandType.BASEADDRESS, name);
                retrieve_arr.addOperand(OperandType.INST, String.valueOf(last_line));

                // set result
                res.type = ResultType.ARR;
                res.address = ir.addInstruction(retrieve_arr);
                res.end_line = res.address;

                if(accept(Token.closebracketToken)) {
                    next();
                } else {
                    error("Missing close bracket in designator");
                }
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
        Result x = factor(), y;
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
                addOperandByResultType(y, in);
            // the rest of iterations
            }else{
                in.addOperand(OperandType.INST, String.valueOf(last_line));
            }
            in.addOperand(OperandType.INST, String.valueOf(y.line));
            int line = ir.addInstruction(in);
            // the first iteration, set res start_line
            if(last_line == Integer.MIN_VALUE){
                res.start_line = x.type == ResultType.VAR ? line : x.start_line;
            }
            last_line = line;
        }
        // set up result
        res.line = last_line;
        res.end_line = last_line;
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
        Result x = term(), y;
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
                addOperandByResultType(y, in);
            // the rest of iterations
            }else{
                in.addOperand(OperandType.INST, String.valueOf(last_line));
            }
            in.addOperand(OperandType.INST, String.valueOf(y.line));
            int line = ir.addInstruction(in);
            // the first iteration, set res start_line
            if(last_line == Integer.MIN_VALUE){
                res.start_line = x.type == ResultType.VAR ? line : x.start_line;
            }
            last_line = line;
        }
        // set up result
        res.line = last_line;
        res.end_line = last_line;
        return res;
    }

    public Result relation() throws Exception {
        Result x = null,y;
        int op;

        x = expression();
        relOp();
        y = expression();

        //combine(CMP, x, y);

        return x;
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
                addOperandByResultType(e_res, assi);
                if(d_res.type == ResultType.ARR){
                    assi.addOperand(OperandType.ADDRESS, String.valueOf(d_res.address));
                }else{
                    assi.addOperand(OperandType.VARIABLE, d_res.name);
                }
                // set result
                block.end_line = ir.addInstruction(assi);
                // if it is array, designator have already added instructions
                if(d_res.type == ResultType.ARR) {
                    block.start_line = d_res.start_line;
                    // if variable, designator function just return the variable name
                }else{
                    block.start_line = e_res.start_line;
                }
                block.end_line = this.ir.addInstruction(assi);
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
                    addOperandByResultType(e_res, in);
                    while(accept(Token.commaToken)) {
                        next();
                        e_res = expression();
                        if(--i < 0)
                            error("Function " + name + " only take " + i + "parameters");
                        addOperandByResultType(e_res, in);
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
            res.end_line = res.line;
        }
        return res;
    }



    public BasicBlock1 ifStatement() throws Exception {
        BasicBlock1 b = new BasicBlock1();
        b.instruction = "if";
        b.hasBranching = true;
        BasicBlock1 join = new BasicBlock1();
        join.instruction = "fi";
        b.right = join;
        if(accept(Token.ifToken)) {
            next();
            relation();
            if(accept(Token.thenToken)) {
                next();
                b.left = statSequence();
                if (accept(Token.elseToken)) {
                    next();
                    b.right = statSequence();
                    b.right.exit.left = join;
                }
                if(accept(Token.fiToken)) {
                    next();
                    b.left.exit.left = join;
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

    public BasicBlock1 whileStatement() throws Exception {
        BasicBlock1 b = new BasicBlock1();
        b.instruction = "while";
        b.hasBranching = true;
        BasicBlock1 j = new BasicBlock1();
        j.instruction = "od";
        b.right = j;            //exiting the while loop
        b.exit = j;
        if(accept(Token.whileToken)) {
            next();
            relation();
            if(accept(Token.doToken)) {
                next();
                BasicBlock1 leftSide = statSequence();
                leftSide.exit.left = b;
                b.left = leftSide;
                if(accept(Token.odToken)) {
                    next();
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

    public BasicBlock1 returnStatement() throws Exception {
        BasicBlock1 b = new BasicBlock1();
        if(accept(Token.returnToken)) {
            next();
            expression();
            b.instruction = "return something";
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
            //TODO: need to work on dis. prob doesnt work
            return returnStatement();
        } else {
            error("Statement is invalid");
        }
        return null;
    }

    public BasicBlock statSequence() throws Exception {
        BasicBlock start = statement();
        while(accept(Token.semiToken)) {
            next();
            BasicBlock next = statement();
            if(next.has_branching) {
                //connect top and bottom
                start.left = next;
                //start.exit = temp.exit;
            } else {
                // just append instead
                start.merge(next);
                //TODO: od and fi are considered part of basic blocks
                // unsure if this is wanted behavior
            }
        }
        return start;
    }

    public Result typeDecl() throws Exception {
        Result res = new Result();
        if (accept(Token.varToken)) {
            res.type = ResultType.VAR;
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
                next();
            }while(accept(Token.commaToken));

        } else if (t_res.type == ResultType.VAR){
            // if variable
            do {
                String name = ident();
                if(this.parsing_function){
                    // add to function scope
                    current_func.addLocalVariable(name);
                }else {
                    // add to global scope
                    symbolTable.addVariable(name);
                }
                next();
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
                //if not semiToken, then formalParams MUST be following
                if(!accept(Token.semiToken)) {
                    func.addAllParameters(formalParam());
                    // add instructions to the block
                    /*for(int i = 0, len = func.parameters.size(); i < len; i++){
                        Instruction in = new Instruction(InstructionType.LOADPARAM);
                        in.addOperand(OperandType.FUNC_PARAM, String.valueOf(i));
                        block.addInstruction(in);
                    }*/
                }
                if(accept(Token.semiToken)) {
                    next();
                    block.merge(funcBody());
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
        while(!accept(Token.beginToken)){
            if(accept(Token.varToken) || accept(Token.arrToken)) {
                block.merge(varDecl());
            } else {
                error("Invalid var declaration in func body");
            }
        }
        if(accept(Token.beginToken)) {
            next();
            block.merge(statSequence());
            //b.exit = (b.left).exit;
            if(accept(Token.endToken)) {
                next();
            } else {
                error("Missng closing bracket for func body");
            }
        } else {
            error("Missing open bracket for func body");
        }
        return block;
    }

    public BasicBlock1 computation() throws Exception {
        BasicBlock1 b = new BasicBlock1();
        BasicBlock1 current = b;
        if(accept(Token.mainToken)) {
            next();
            // Parsing variables
            this.parsing_variable = true;
            while (accept(Token.varToken) || accept(Token.arrToken)) {
                current.left = varDecl();
                current = current.left;
            }
            this.parsing_variable = false;

            // Parsing functions
            this.parsing_function = true;
            while (accept(Token.funcToken) || accept(Token.procToken)) {
                current.left = funcDecl();
                current = current.left;
            }
            this.parsing_function = false;

            // Parsing the rest of the program
            this.parsing_rest = true;
            if (accept(Token.beginToken)){
                next();
                //if token is not }, must be statSequence option.
                if(!accept(Token.endToken)){
                    current.left = statSequence();
                    current = current.left;
                }
                if(accept(Token.endToken)) {
                    next();
                } else {
                    error("Missing closing bracket for main");
                }
            } else {
                error("Missing open bracket for main");
            }
            this.parsing_rest = false;
        } else {
            error("Missing main");
        }
        b.instruction = "main";
        BasicBlock1 exit = new BasicBlock1();
        exit.instruction = "END";
        b.exit = exit;
        current.exit.left = exit;
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

    private boolean addOperandByResultType(Result e_res, Instruction in){
        switch(e_res.type){
            case ARR:
                in.addOperand(OperandType.ADDRESS, String.valueOf(e_res.address));
                break;
            case VAR:
                in.addOperand(OperandType.VARIABLE, String.valueOf(e_res.name));
                break;
            case CONST:
                in.addOperand(OperandType.CONST, String.valueOf(e_res.value));
                break;
            case INST:
                in.addOperand(OperandType.INST, String.valueOf(e_res.line));
                break;
        }
        return true;
    }
}
