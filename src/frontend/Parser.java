package frontend;

import IR.BasicBlock;
import IR.DefUseChain;

import java.io.IOException;

/**
 * Created by Ivan on 1/31/2015.
 */
public class Parser {

    private int in; //the current currToken on the input
    private Scanner s;

    private DefUseChain du;

    private int tokenCount = 0;

    public Parser(String path) throws Exception {
        s = new Scanner(path);
       //initialize the first token
        next();
        //computation();
        //System.out.println("Finished compiling "+s.getLineNumber()+" lines in "+path+".");
    }

    private void next() throws IOException {
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

    public Result ident() throws Exception {
        Result r = new Result();
        r.t = Result.Type.VAR;
        if(accept(Token.ident)) {
            next();
        } else {
            error("Missing identifier");
        }
        return r;
    }

    public Result number() throws Exception {
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
        Result r = null;
        if(accept(Token.ident)) {
            r = ident();
            while (accept(Token.openbracketToken)) {
                r.t = Result.Type.ARR;
                next();
                expression();
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
        BasicBlock b = new BasicBlock();
        if(accept(Token.letToken)) {
            next();
            designator();
            if(accept(Token.becomesToken)) {
                next();
                expression();
            } else {
                error("Missing becomes token during assignment");
            }
        } else {
            error("Missing let token during assignment");
        }
        b.instruction = "assignment";
        b.exit = b;
        return b;
    }

    public Result funcCall() throws Exception {
        Result x = null;
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
        return x;
    }

    public BasicBlock ifStatement() throws Exception {
        BasicBlock b = new BasicBlock();
        b.instruction = "if";
        b.hasBranching = true;
        BasicBlock join = new BasicBlock();
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

    public BasicBlock whileStatement() throws Exception {
        BasicBlock b = new BasicBlock();
        b.instruction = "while";
        b.hasBranching = true;
        BasicBlock j = new BasicBlock();
        j.instruction = "od";
        b.right = j;            //exiting the while loop
        b.exit = j;
        if(accept(Token.whileToken)) {
            next();
            relation();
            if(accept(Token.doToken)) {
                next();
                BasicBlock leftSide = statSequence();
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

    public BasicBlock returnStatement() throws Exception {
        BasicBlock b = new BasicBlock();
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
            funcCall();
            BasicBlock b = new BasicBlock();
            b.instruction = "calling func";
            b.exit = b;
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
            BasicBlock temp = statement();
            if(temp.hasBranching) {
                //connect top and bottom
                (start.exit).left = temp;
                start.exit = temp.exit;
            } else {
                // just append instead
                (start.exit).append(temp.instruction);
                //TODO: od and fi are considered part of basic blocks
                // unsure if this is wanted behavior
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
        b.instruction = "typeDecl";
        b.exit = b;
        return b;
    }

    public BasicBlock varDecl() throws Exception {
        BasicBlock b = new BasicBlock();
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
        b.instruction = "varDecl";
        b.exit = b;
        return b;
    }

    public BasicBlock funcDecl() throws Exception {
        BasicBlock b = new BasicBlock();
        b.instruction = "funcDecl";
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
                }
                if(accept(Token.endToken)) {
                    next();
                } else {
                    error("Missing closing bracket for main");
                }
            } else {
                error("Missing open bracket for main");
            }
        } else {
            error("Missing main");
        }
        b.instruction = "main";
        BasicBlock exit = new BasicBlock();
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
