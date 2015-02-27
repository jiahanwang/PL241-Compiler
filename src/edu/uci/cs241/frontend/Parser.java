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

    // next()
    private void next() {
        try {
            in = s.getSym();
            tokenCount++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // BEGIN RULES FOR PL241
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

    public Result number() throws Exception {
        Result r = new Result();
        r.type = ResultType.CONST;
        if(accept(Token.number)) {
            r.value = s.number;
            next();
        } else {
            error("Missing number");
        }
        return r;
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
                retrieve_arr.addOperand(OperandType.LINE, String.valueOf(last_line));

                // set result
                /** not sure the res type of array **/
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

    public Result factor() throws Exception {
        Result x = null, y;
        if(accept(Token.ident)) {
            x = designator();
        }
        else if(accept(Token.number)) {
            x = number();
        }
        else if(accept(Token.openparenToken)) {
            next();
            x = expression();
            if(accept(Token.closeparenToken)) {
                next();
            }
        }
        else if(accept(Token.callToken)) {
            x = funcCall();
        }
        else {
            error("Invalid factor call");
        }
        return x;
    }

    public Result term() throws Exception {
        Result x, y;
        x = factor();
        while(accept(Token.timesToken) || accept(Token.divToken)) {
            next();
            y = factor();
        }
        //combine(MUL, x, y);
        return x;
    }

    public Result expression() throws Exception {
        Result x, y;
        x = term();
        while(accept(Token.plusToken) || accept(Token.minusToken)) {
            next();
            y = term();
        }
        //combine(ADD, x, y);
        return x;
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

    public BasicBlock assignment() throws Exception {
        BasicBlock block = new BasicBlock();
        if(accept(Token.letToken)) {
            next();
            Result d_res = designator();
            if(accept(Token.becomesToken)) {
                next();
                Result e_res = expression();
                Instruction assi = new Instruction(InstructionType.MOVE);
                assi.addOperand(OperandType.INST, String.valueOf(e_res.line));
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
                    // if variable, designator functon just return the variable name
                }else{
                    block.start_line = e_res.start_line;
                }
            } else {
                error("Missing becomes token during assignment");
            }
        } else {
            error("Missing let token during assignment");
        }
        return block;
    }

    public BasicBlock funcCall() throws Exception {
        BasicBlock block = new BasicBlock(BasicBlockType.NORMAL);
        if(accept(Token.callToken)) {
            next();
            ident();
            if(accept(Token.openparenToken)) {
                next();
                if(!accept(Token.closeparenToken)) {
                    expression();
                    while(accept(Token.commaToken)) {
                        next();
                        expression();
                    }
                }
                if(accept(Token.closeparenToken)) {
                    next();
                } else {
                    error("Missing close paren in func call");
                }
            } else {
//                error("Missing open paren in func call");
            }
        }
        return block;
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
            //TODO: don't have assignment return bb
            return assignment();
        }
        else if(accept(Token.callToken)) {
            // function call to get the function name
            //String func_name = funcCall();
            BasicBlock b = new BasicBlock();
            all_blocks.put(b.id, b);
            Instruction in = new Instruction("FUNC");
            in.addOperand("FP", func_name);
            b.addInstruction(in);
            //b.exit = b;
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
                Result i_res = ident();
                if(this.parsing_function){
                    // add to function scope
                    current_func.addLocalVariable(i_res.name);
                }else {
                    // add to global scope
                    symbolTable.addVariable(i_res.name);
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
                params.add(ident().name);
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

    private boolean accept(Token t) {
        return in == t.value;
    }


    private void emit(String s) {
        System.out.println(s);
    }

    public void error(String e) throws Exception {
        throw new Exception("Parser encountered error "+e+" on line "+s.getLineNumber()+" near tokenNum:"
                +tokenCount+" ="+Token.getRepresentation(in));
    }
}
