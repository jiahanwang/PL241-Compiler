package edu.uci.cs241.frontend;

import java.util.HashMap;

/**
 * Created by Ivan on 1/31/2015.
 */
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
        odToken("od", 81),
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

        // two way hash-map
        // alternatively can use Google BiMap but this is easier for now.
        private static final HashMap<String, Integer> map = new HashMap<String, Integer>();
        private static final HashMap<Integer, String> rmap = new HashMap<Integer, String>();
        static {
            for(Token t : Token.values()) {
                map.put(t.representation, t.value);
                rmap.put(t.value, t.representation);
            }
        }

        public final String representation;
        public final int value;

        Token(String representation, int value) {
            this.representation = representation;
            this.value = value;
        }

        public static int getValue(String s) {
            //match value with representation
            return map.get(s);
        }

        public static boolean contains(String s) {
            return map.containsKey(s);
        }

        // Only used for debugging
        public static String getRepresentation(int i) {
            return rmap.get(i);
        }
}

