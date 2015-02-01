/**
 * Created by Ivan on 1/31/2015.
 */
public class Tokens {
    public enum Token {
        errorToken("", 0),

        timesToken("*", 1),
        divToken("/", 2),

        plusToken("+", 11),
        minusToken("-", 12),

        eqlToken("==", 20),
        neqToken("!=", 21),
        lssToken("<", 22),
        geqToken(">=", 23),
        leqToken("<=", 24),
        gtrToken(">", 25),

        periodToken(".", 30),
        commaToken(",", 31),
        openbracketToken("[", 32),
        closebracketToken("]", 33),
        closeparenToken(")", 34),

        becomesToken("<-", 40),
        thenToken("then", 41),
        doToken("do", 42),

        openparenToken("(", 50),

        number("number", 60),
        ident("identifier", 61),

        semiToken(";", 70),

        endToken("}", 80),
        odTOken("od", 81),
        fiToken("fi", 82),

        elseToken("else", 90),

        letToken("let", 100),
        callToken("call", 101),
        ifToken("if", 102),
        whileToken("while", 103),
        returnToken("return", 104),

        varToken("var", 110),
        arrToken("array", 111),
        funcToken("function", 112),
        procToken("procedure", 113),

        beginToken("{", 150),
        mainToken("main", 200),
        eofToken("end of file", 255);

        public final String representation;
        public int value;
        Token(String representation, int value) {
            this.representation = representation;
            this.value = value;
        }
    }
}
