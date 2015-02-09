package frontend;

import java.io.IOException;
import java.text.ParseException;

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
//        switch(in) {
//            // relation operators
//        }
        if(in >= 20 && in <=25){
            next();
        }
    }

    public void ident() throws Exception {
        if(accept(Token.ident)) {
            next();
        } else {
            error("Missing identifier");
        }
    }

    public void number() throws Exception {
        if(accept(Token.number)) {
            next();
        } else {
            error("Missing number");
        }
    }


    public void designator() throws Exception {
        if(accept(Token.ident)) {
            ident();
            while (accept(Token.openbracketToken)) {
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
    }

    public void factor() throws Exception {
        if(accept(Token.ident)) {
            designator();
        }
        else if(accept(Token.number)) {
            number();
        }
        else if(accept(Token.openparenToken)) {
            next();
            expression();
            if(accept(Token.closeparenToken)) {
                next();
            }
        }
        else if(accept(Token.callToken)) {
            funcCall();
        } else {
            error("Invalid factor call");
        }
    }

    public void term() throws Exception {
        factor();
        while(accept(Token.timesToken) || accept(Token.divToken)) {
            next();
            factor();
        }
    }

    public void expression() throws Exception {
        term();
        while(accept(Token.plusToken) || accept(Token.minusToken)) {
            next();
            term();
        }
    }

    public void relation() throws Exception {
        expression();
        relOp();
        expression();
    }

    public void assignment() throws Exception {
        if(accept(Token.letToken)) {
            next();
            designator();
            if(accept(Token.becomesToken)) {
                next();
                expression();
            } else {
                error("Missing becomes token during assignment");
            }
        }
    }

    public void funcCall() throws Exception {
        if(accept(Token.callToken)) {
            next();
            ident();
            if(accept(Token.openparenToken)) {
                next();
                expression();
                while(accept(Token.commaToken)) {
                    next();
                    expression();
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
                } else {
                    error("Missing fi token");
                }
            } else {
                error("Missing then token");
            }
        } else {
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
                } else {
                    error("Missing od token");
                }
            } else {
                error("Missing do token");
            }
        } else {
            error("Missing while token");
        }
    }

    public void returnStatement() throws Exception {
        if(accept(Token.returnToken)) {
            next();
            expression();
        } else {
            error("Missing return statement");
        }
    }

    public void statement() throws Exception {
        //TODO: figure out how to OR all these
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
                } else {
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
        } else {
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

    public void funcBody() throws Exception {
        if(accept(Token.varToken)) {
            varDecl();
        }
        if(accept(Token.beginToken)) {
            next();
            statSequence();
            if(accept(Token.endToken)) {
                next();
            } else {
                error("Misisng closing bracket for func body");
            }
        } else {
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
                } else {
                    error("Missing closing bracket for main");
                }
            } else {
                error("Missing open bracket for main");
            }
        } else {
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
        throw new Exception("Parser encountered error "+e+" on line "+s.getLineNum()+" near tokenNum:"
                +tokenCount+" ="+Token.getRepresentation(in));
    }
}
