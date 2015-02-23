package frontend;

import IR.BasicBlock;
import IR.DefUseChain;
import IR.IRInstruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 1/31/2015.
 */
public class Parser {

    // Vars used for parsing the tokens
    private int in;                     // the current currToken on the input
    private Scanner s;
    private int tokenCount = 0;         // debugging

    // Stuffs for outputting IR or DLX
    public List<IRInstruction> buffer;    // the buffer of instructions to output
    private int pc = 0;                 // program counter
    BasicBlock c;       //the current basicblock we're writing to

    // Vars used for optimization
    private DefUseChain du;
    private int basereg;


    // Constructor for the parser
    public Parser(String path) throws Exception {
        s = new Scanner(path);
       //initialize the first token
        next();

        buffer = new ArrayList<IRInstruction>();
    }

    // Advancing the token input from scanner to next
    private void next() throws IOException {
        try {
            in = s.getSym();
            tokenCount++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        BEGIN RULES FOR PL241
    */

    public Result.Condition relOp() throws Exception {
        switch(in) {
            // IDE would not allow Token.LEQ.value for switch
            // consume token and return the corresponding condition
            case 20: next(); return Result.Condition.EQ;
            case 21: next(); return Result.Condition.NE;
            case 22: next(); return Result.Condition.LT;
            case 23: next(); return Result.Condition.GE;
            case 24: next(); return Result.Condition.LE;
            case 25: next(); return Result.Condition.GT;
            default:
                error("Invalid relational operator found");
                return null;
        }
    }

    public Result ident() throws Exception {
        Result r = new Result();
        r.t = Result.Type.VAR;
        if(accept(Token.ident)) {
            //store info inside the Result and move onto next token
            r.address = s.id;//TODO: store the identifiers and their scopes somewhere
            next();
        } else {
            error("Missing identifier");
        }
        return r;
    }

    public Result number() throws Exception {
        // build the Result and store the number
        Result r = new Result();
        r.t = Result.Type.CONST;
        if(accept(Token.number)) {
            r.value = s.number;
            next();
        } else {
            error("Missing number");
        }
        return r;
    }


    public Result designator() throws Exception {
        // Determine if designator is a var or array?
        Result r = null, x;
        if(accept(Token.ident)) {
            // ident defaults to var
            r = ident();
            while (accept(Token.openbracketToken)) {
                // [] means array
                r.t = Result.Type.ARR;
                next();
                //TODO: not sure what to do with these expressions? i think these are the array offsets.
                x = expression();
                // probably compute them and reference the array after
                // be careful of nesting [][][]
                if(accept(Token.closebracketToken)) {
                    next();
                } else {
                    error("Missing close bracket in designator");
                }
            }
        } else {
            error("Missing identifier from designator");
        }
        return r;
    }

    public Result factor() throws Exception {
        //TODO: not sure how to deal with all these different types :(
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
            String op = (accept(Token.timesToken)) ? "MUL" : "DIV";
            next();
            y = factor();
//            combine(op, x, y);
            String op1 = (x.t == Result.Type.CONST) ? Integer.toString(x.value) : s.identifiers.get(x.address);
            String op2 = (y.t == Result.Type.CONST) ? Integer.toString(y.value) : s.identifiers.get(y.address);
            insertInstruction(c, new IRInstruction(op, op1, op2));
        }
        return x;
    }

    public Result expression() throws Exception {
        Result x, y;
        x = term();
        while(accept(Token.plusToken) || accept(Token.minusToken)) {
            String op = (accept(Token.plusToken)) ? "ADD" : "SUB";
            next();
            y = term();
            combine(op, x, y);
            String op1 = (x.t == Result.Type.CONST) ? Integer.toString(x.value) : s.identifiers.get(x.address);
            String op2 = (y.t == Result.Type.CONST) ? Integer.toString(y.value) : s.identifiers.get(y.address);
            insertInstruction(c, new IRInstruction(op, op1, op2));
        }
        return x;
    }

    public Result relation() throws Exception {
        Result x = null,y;
        int op;

        x = expression();
        x.c = relOp();
        y = expression();

        combine("CMP", x, y);
        x.t = Result.Type.COND;

        String op1 = (x.t == Result.Type.CONST) ? Integer.toString(x.value) : s.identifiers.get(x.address);
        String op2 = (y.t == Result.Type.CONST) ? Integer.toString(y.value) : s.identifiers.get(y.address);

        insertInstruction(c, new IRInstruction("CMP", op1, op2));
        return x;
    }

    public BasicBlock assignment() throws Exception {
        BasicBlock b = new BasicBlock();
        Result x, y;
        IRInstruction i;
        c = b;
        if(accept(Token.letToken)) {
            next();
            x = designator();
            if(accept(Token.becomesToken)) {
                next();
                y = expression();
                //TODO: this later
                combine("MOVE", x, y);
                String assignee = (x.t == Result.Type.CONST) ? Integer.toString(x.value) : s.identifiers.get(x.address);
                String assignor = (y.t == Result.Type.CONST) ? Integer.toString(y.value) : s.identifiers.get(y.address);
                insertInstruction(b, new IRInstruction("MOVE", assignor, assignee));    // move y x -> x:=y
            } else {
                error("Missing becomes token during assignment");
            }
        } else {
            error("Missing let token during assignment");
        }
        b.exit = b;
        return b;
    }

    public Result funcCall() throws Exception {
        Result x = null, y;
        if(accept(Token.callToken)) {
            next();
            x = ident(); //this is the func call name
            insertInstruction(c, new IRInstruction("call", "0", "0"));
            if(accept(Token.openparenToken)) {
                next();
                //TODO: handle the parms if there are any
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
                error("Missing open paren in func call");
            }
        }
        return x;
    }

    //TODO: generate phis inside the join block
    public BasicBlock ifStatement() throws Exception {

        // create the top "if" block and bottom "fi" block
        // the right side will default to end unless an else is detected
        BasicBlock b = new BasicBlock();
        b.hasBranching = true;
        BasicBlock join = new BasicBlock();
        // set the right and exit to the fi block
        b.right = b.exit = join;

        if(accept(Token.ifToken)) {
            next();
            Result r = relation();
            r.fixuplocation = pc;
            insertInstruction(b, new IRInstruction("BRA", "IF"));
            //TODO: suppose to get the result from relation to determine if branch for codegen
            if(accept(Token.thenToken)) {
                next();
                // set the fall through if to the statement sequence
                b.left = statSequence();
                insertInstruction(b.left, new IRInstruction("BRA", "FWD"));
                // this connects the left statement's exit to the fi block
                (b.left).exit.left = join;
                if (accept(Token.elseToken)) {
                    next();
                    // if else is detected, rearrange the right to include the block
                    b.right = statSequence();
                    // this connects the else statement's exit to the fi block
                    (b.right).exit.left = join;
                }
                if(accept(Token.fiToken)) {
                    next();
                } else {
                    error("Missing fi token");
                }
            } else {
                error("Missing then token");
            }
        } else {
            error("Missing if token");
        }
        // connect dom tree, b is the parent of all the blocks
        (b.right).myDom = (b.left).myDom = join.myDom = b;
        return b;
    }

    //TODO: generate phis inside the join block
    public BasicBlock whileStatement() throws Exception {

        // Setting up the extra nodes for CFG
        BasicBlock b = new BasicBlock();
        b.hasBranching = true;
        BasicBlock join = new BasicBlock();
        //setting the default exits
        b.right = b.exit = join;
        // setting the branch back up to loop header
        int loop = pc;

        if(accept(Token.whileToken)) {
            next();
            c = b;
            Result x = relation();

            insertInstruction(b, new IRInstruction("BNE", "end"));
            if(accept(Token.doToken)) {
                next();
                b.left = statSequence();
                insertInstruction(b.left, new IRInstruction("wBRA", String.valueOf(loop-pc)));
                // setting the exit to fall back towards the while loop header
                (b.left).exit.left = b;
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
        // connect dom tree, b is the parent of all the blocks
        (b.right).myDom = (b.left).myDom = join.myDom = b;
        return b;
    }

    public BasicBlock returnStatement() throws Exception {
        BasicBlock b = new BasicBlock();
        if(accept(Token.returnToken)) {
            next();
            expression();
            insertInstruction(b, new IRInstruction("BRA", "return addr"));
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
            //TODO: func call will probably look up a table for the jump?
            funcCall();
            BasicBlock b = new BasicBlock();
            b.exit = b;
            c = b;
            return b;
        }
        else if(accept(Token.ifToken)) {
            return ifStatement();
        }
        else if(accept(Token.whileToken)) {
            return whileStatement();
        }
        else if(accept(Token.returnToken)) {
            //TODO: need to work on dis. prob doesnt work, has not been tested
            return returnStatement();
        } else {
            error("Statement is invalid");
        }
        return null;
    }

    public BasicBlock statSequence() throws Exception {
        BasicBlock start = statement();
        c = start;
        while(accept(Token.semiToken)) {
            next();
            BasicBlock temp = statement();
            if(temp.hasBranching) {
                // connect the dom tree
                temp.myDom = start.exit;

                //connect top and bottom
                (start.exit).left = temp;
                start.exit = temp.exit;

                //redefine the current block to append instructions to
                c = start.exit;
            } else {
                // just append instead
                for(IRInstruction i : temp.instructions) {
                    start.exit.append(i);
                }
            }
        }
        return start;
    }

    public BasicBlock typeDecl() throws Exception {
        BasicBlock b = new BasicBlock();
        if(accept(Token.varToken) || accept(Token.arrToken)) {
            next();
            while(accept(Token.openbracketToken)) {
                next();
                number();
                if(accept(Token.closebracketToken)) {
                    next();
                } else {
                    error("Missing close parenthesis in type declaration");
                }
            }
        }

        b.exit = b;
        return b;
    }

    public BasicBlock varDecl() throws Exception {
        BasicBlock b = new BasicBlock();
        c = b;
        //TODO: probably setup for symbol table or scope? doesn't actually generate anything?
        typeDecl();
        ident();
        while(accept(Token.commaToken)) {
            next();
            ident();
        }
        if(accept(Token.semiToken)) {
            next();
        } else {
            error("Missing semicolon for var declaration");
        }
        b.exit = b;
        insertInstruction(b, new IRInstruction("var", "decl"));
        return b;
    }

    public BasicBlock funcDecl() throws Exception {
        BasicBlock b = new BasicBlock();
        //TODO: probably setup some kind of address to this func
        insertInstruction(b, new IRInstruction("label", "func"));
        if(accept(Token.funcToken) || accept(Token.procToken)) {
            next();
            if(accept(Token.ident)) {
                next();
                //if not semiToken, then formalParams MUST be following
                if(!accept(Token.semiToken)) {
                    formalParam();
                }
                if(accept(Token.semiToken)) {
                    next();
                    b.left = funcBody();
                    b.exit = b.left.exit;
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
        return b;
    }

    public void formalParam() throws Exception {
        if(accept(Token.openparenToken)) {
            next();
            while(!accept(Token.closeparenToken)) {
                ident();
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
    }

    public BasicBlock funcBody() throws Exception {
        BasicBlock b = new BasicBlock();
        while(!accept(Token.beginToken)){
            if(accept(Token.varToken) || accept(Token.arrToken)) {
                varDecl();
            } else {
                error("Invalid var declaration in func body");
            }
        }
        if(accept(Token.beginToken)) {
            next();
            b.left = statSequence();
            b.exit = (b.left).exit;
            if(accept(Token.endToken)) {
                next();
            } else {
                error("Misisng closing bracket for func body");
            }
        } else {
            error("Missing open bracket for func body");
        }
        return b;
    }

    public BasicBlock computation() throws Exception {
        BasicBlock b = new BasicBlock();
        BasicBlock current = b;
        if(accept(Token.mainToken)) {
            next();
            b.append(new IRInstruction("main", "0"));
            while (accept(Token.varToken) || accept(Token.arrToken)) {
                current.left = varDecl();
                current = current.left;
            }
            while (accept(Token.funcToken) || accept(Token.procToken)) {
                current.left = funcDecl();
                current = current.left;
            }
            if (accept(Token.beginToken)){
                next();
                //if token is not }, must be statSequence option.
                if(!accept(Token.endToken)){
                    current.left = statSequence();
                    current = current.left;
                    b.exit = current.exit;
                }
                if(accept(Token.endToken)) {
                    next();
                    (b.exit).append(new IRInstruction("end", "0"));

                } else {
                    error("Missing closing bracket for main");
                }
            } else {
                error("Missing open bracket for main");
            }
        } else {
            error("Missing main");
        }
        return b;
    }
    /*
        Auxillary Functions
    */
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

    public void print() {
        for(IRInstruction i : buffer) {
            System.out.println(i.toString());
        }
    }

    public void combine(String op, Result x, Result y) throws Exception {
        if((x.t == Result.Type.CONST) && (y.t == Result.Type.CONST)) {
            if(op == "ADD") {
                x.value += y.value;
            } else if(op == "SUB") {
                x.value -= y.value;
            } else if(op == "MUL") {
                x.value *= y.value;
            } else if(op == "DIV") {
                x.value /= y.value;
            } else {
                throw new Exception("Error while doing combine "
                        +op.toString()+" with Results:["+x.toString()+"] and ["+y.toString()+"]");
            }
// if(op == Operator.ADD) {
//                x.value += y.value;
//            } else if(op == Operator.SUB) {
//                x.value -= y.value;
//            } else if(op == Operator.MUL) {
//                x.value *= y.value;
//            } else if(op == Operator.DIV) {
//                x.value /= y.value;
//            } else {
//                throw new Exception("Error while doing combine "
//                        +op.toString()+" with Results:["+x.toString()+"] and ["+y.toString()+"]");
//            }
        } else {
            load(x);
            if(x.regno == 0 ) {
                x.regno = RegisterAllocator.AllocateReg();
                PutF1("ADD", x.regno, 0, 0);
            }
            if(y.t == Result.Type.CONST) {
                PutF1(op.toString()+"I", x.regno, x.regno, y.value);
            }
            else {
                load(y);
                PutF1(op.toString(), x.regno, x.regno, y.regno);
                RegisterAllocator.deallocate(y.regno);
            }
        }
    }

    public void load(Result x) {
        if(x.t == Result.Type.VAR) {
            x.regno = RegisterAllocator.AllocateReg();
            x.t = Result.Type.REG;
            PutF1("LDW", x.regno, basereg, x.address);
        }
        else if(x.t == Result.Type.CONST) {
            if(x.value == 0) {
                x.regno = 0;
            }
            else {
                x.regno = RegisterAllocator.AllocateReg();
                PutF1("ADDI", x.regno, 0, x.value);
            }
        }
    }

    public void insertInstruction(BasicBlock bb, IRInstruction i) {
        bb.append(i);
    }

    public void PutF1(String s, int a, int b, int c) {

        // write later
        pc++;
    }
    

}
