package frontend;

import java.io.IOException;

/**
 * Created by Ivan on 1/31/2015.
 */
public class Parser {

    private int in; //the current currToken on the input
    private Scanner s;

    private int tokenCount = 0;

    public Parser(String path) throws Exception {
        s = new Scanner(path);
       //initialize the first token
        next();
        computation();
        System.out.println("Finished compiling " + s.getLineNumber() + " lines in " + path + ".");
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

    public void relOp() throws Exception {
        // TODO: this
        switch(in) {
            // relation operators
            case 20 : next(); break;
            case 21 : next(); break;
            case 22 : next(); break;
            case 23 : next(); break;
            case 24 : next(); break;
            case 25 : next(); break;
            default:
                error("Unknown symbol for rel op");
        }

    }

    public void ident() throws Exception {
        // TODO: this
        if(accept(Token.ident)) {
            next();
        }
        else {
            error("Missing identifier");
        }
    }

    public Result number() throws Exception {
        Result x = new Result();
        if(accept(Token.number)) {
            x.t = Result.Type.CONST;
            x.value = s.number;
            next();
        }
        else {
            error("Missing number");
        }
        return x;
    }


    public Result designator() throws Exception {
        // TODO: still need to figure out how to deal with arrays
        //
        Result x = new Result();
        if(accept(Token.ident)) {
            ident();
            while (accept(Token.openbracketToken)) {
                x.t = Result.Type.ARR;
                next();
                expression();
                if(accept(Token.closebracketToken)) {
                    next();
                }
                else {
                    error("Missing close bracket in designator");
                }
            }
        }
        else {
            error("Missing identifier from designator");
        }
        return x;
    }

    public Result factor() throws Exception {
        // TODO: this
        Result x = new Result();
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
            funcCall();
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
            Operator o = (accept(Token.timesToken)) ? Operator.MUL : Operator.DIV;
            next();
            y = factor();
            ParserHelper.combine(o, x, y);
        }
        return x;
    }

    public Result expression() throws Exception {
        Result x, y;
        x = term();
        while(accept(Token.plusToken) || accept(Token.minusToken)) {
            Operator o = (accept(Token.plusToken)) ? Operator.ADD : Operator.SUB;
            next();
            y = term();
            ParserHelper.combine(o, x, y);
        }
        return x;
    }

    public Result relation() throws Exception {
        // TODO: this
        Result x, y, z;
        x = expression();
        relOp();
        y = expression();

        return x;
    }

    public void assignment() throws Exception {
        Result x, y;
        if(accept(Token.letToken)) {
            next();
            x = designator();
            if(accept(Token.becomesToken)) {
                next();
                y = expression();
                x = y;
            }
            else {
                error("Missing becomes token during assignment");
            }
        }
        else {
            error("Missing let token during assignment");
        }
    }

    public void funcCall() throws Exception {
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
                }
                else {
                    error("Missing close paren in func call");
                }
            }
            else {
//                error("Missing open paren in func call");
            }
        }
    }

    public void ifStatement() throws Exception {
        if(accept(Token.ifToken)) {
            next();
            relation();
            if(accept(Token.thenToken)) {
                next();
                statSequence();
                if (accept(Token.elseToken)) {
                    next();
                    statSequence();
                }
                if(accept(Token.fiToken)) {
                    next();
                }
                else {
                    error("Missing fi token");
                }
            }
            else {
                error("Missing then token");
            }
        }
        else {
            error("Missing if token");
        }
    }

    public void whileStatement() throws Exception {
        if(accept(Token.whileToken)) {
            next();
            relation();
            if(accept(Token.doToken)) {
                next();
                statSequence();
                if(accept(Token.odToken)) {
                    next();
                }
                else {
                    error("Missing od token");
                }
            }
            else {
                error("Missing do token");
            }
        }
        else {
            error("Missing while token");
        }
    }

    public void returnStatement() throws Exception {
        if(accept(Token.returnToken)) {
            next();
            expression();
        }
        else {
            error("Missing return statement");
        }
    }

    public void statement() throws Exception {
        if(accept(Token.letToken)) {
            assignment();
        }
        else if(accept(Token.callToken)) {
            funcCall();
        }
        else if(accept(Token.ifToken)) {
            ifStatement();
        }
        else if(accept(Token.whileToken)) {
            whileStatement();
        }
        else if(accept(Token.returnToken)) {
            returnStatement();
        } else {
            error("Statement is invalid");
        }
    }

    public void statSequence() throws Exception {
        statement();
        while(accept(Token.semiToken)) {
            next();
            statement();
        }
    }

    public void typeDecl() throws Exception {
        if(accept(Token.varToken) || accept(Token.arrToken)) {
            next();
            while(accept(Token.openbracketToken)) {
                next();
                number();
                if(accept(Token.closebracketToken)) {
                    next();
                }
                else {
                    error("Missing close parenthesis in type declaration");
                }
            }
        }
    }

    public void varDecl() throws Exception {
        typeDecl();
        ident();
        while(accept(Token.commaToken)) {
            next();
            ident();
        }
        if(accept(Token.semiToken)) {
            next();
        }
        else {
            error("Missing semicolon for var declaration");
        }
    }

    public void funcDecl() throws Exception {
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
                    funcBody();
                    if(accept(Token.semiToken)) {
                        next();
                    }
                    else {
                        error("Missing ; after function body");
                    }
                }
                else {
                    error("Missing ; after formal parameters");
                }
            }
            else {
                error("Missing identifier for function declaration");
            }
        }
        else {
            error("Missing function or procedure heading");
        }
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
            }
            else {
                error("Missing close paren for formal params");
            }
        }
        else {
            error("Missing open paren for formal params");
        }
    }

    public void funcBody() throws Exception {
        while(!accept(Token.beginToken)){
            if(accept(Token.varToken) || accept(Token.arrToken)) {
                varDecl();
            }
            else {
                error("Invalid var declaration in func body");
            }
        }
        if(accept(Token.beginToken)) {
            next();
            statSequence();
            if(accept(Token.endToken)) {
                next();
            }
            else {
                error("Misisng closing bracket for func body");
            }
        }
        else {
            error("Missing open bracket for func body");
        }
    }

    public void computation() throws Exception {
        if(accept(Token.mainToken)) {
            next();
            while (accept(Token.varToken) || accept(Token.arrToken)) {
                varDecl();
            }
            while (accept(Token.funcToken) || accept(Token.procToken)) {
                funcDecl();
            }
            if (accept(Token.beginToken)){
                next();
                //if token is not }, must be statSequence option.
                if(!accept(Token.endToken)){
                    statSequence();
                }
                if(accept(Token.endToken)) {
                    next();
                }
                else {
                    error("Missing closing bracket for main");
                }
            }
            else {
                error("Missing open bracket for main");
            }
        }
        else {
            error("Missing main");
        }
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
