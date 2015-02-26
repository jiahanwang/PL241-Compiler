package edu.uci.cs241.frontend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ivan on 1/31/2015.
 */
public class Scanner {

    private char inputSym; //the current char on the input
    private FileReader f;
    private StringBuilder current;
    public List<String> identifiers;

    // Special Keywords
    private String[] keywords = {"then", "do", "od", "fi", "else",
            "let", "call", "if", "while", "return", "var", "array",
            "function", "procedure", "main"};

    private List<Character> singleSymbols = Arrays.asList('*', ',', ';', '{', '}', '[', ']', '.', '(', ')', '+', '-');
    private List<Character> checkSymbols = Arrays.asList('=', '!', '<', '>', '/', '#');
    // checkSymbols = peek ahead required to determine token
    // ==, !=, <=, >=, ++?, --?, //

    // the last number encountered
    public int number;

    // the last identifier encountered
    public int id;

    // the id of the token that has just been parsed by next()
    public int currToken;

    public Scanner(String fileName) throws Exception {
        //constructor: open file and scan the first currToken into 'sym'
        f = new FileReader(fileName);
        identifiers = new ArrayList<String>();
        //blacklist the keywords
        identifiers.addAll(Arrays.asList(keywords));
        // update size of identifiers
        id = identifiers.size() - 1;
        inputSym = f.getSym();
    }

    private void next() throws Exception {
        current = new StringBuilder();  //building out the currToken string in case its an identifier
        // We've reached the end of file
        if (inputSym == (char) -1) {
            currToken = Token.eofToken.value;
            return;
        }
        // Advance to next available input
        while (Character.isWhitespace(inputSym)) {
            inputSym = f.getSym();
        }

        // input [A-z][A-z0-9]
        // will be identifier or keyword
        if (Character.isAlphabetic(inputSym)) {
            currToken = Token.ident.value;
            while (Character.isAlphabetic(inputSym) || Character.isDigit(inputSym)) {
                //read until no more alphanumeric chars
                current.append(inputSym);
                inputSym = f.getSym();
            }
            if (identifiers.contains(current.toString())) {
                // if currToken matches previous identifier then set id.
                id = identifiers.indexOf(current.toString());
                // or could be a keyword? if in pre-allocated reserved space.
                if (id < keywords.length) {
                    // is a keyword, get the correct Token value for it
                    currToken = Token.getValue(current.toString());
                }
            } else {
                // new identifier
                identifiers.add(current.toString());
                id = identifiers.size() - 1;
            }
        }
        // input NNNNN = value
        // will be number
        else if (Character.isDigit(inputSym)) {
            currToken = Token.number.value;
            while (Character.isDigit(inputSym)) {
                // read until no more numbers
                current.append(inputSym);
                inputSym = f.getSym();
            }
            number = Integer.parseInt(current.toString());
        }
        // input '*', ',', ';', '{', '}', '[', ']', '.', '(', ')'
        else if (singleSymbols.contains(inputSym)) {
            // single symbol
            currToken = Token.getValue(Character.toString(inputSym));
            inputSym = f.getSym();
        }
        // these symbols need LL(1) first
        else if (checkSymbols.contains(inputSym)) {
            // consume next and check the Tokens agn
            current.append(inputSym);
            inputSym = f.getSym();
            // if not end of file, then peek ahead
            if (inputSym != -1) {
                String peek = current.toString() + inputSym;
                // SPECIAL CASE: COMMENTS
                if (peek.equals("//") || current.toString().equals("#")) {
                    //go to next line and start on the next char
                    f.nextLine();
                    inputSym = f.getSym();
                    this.next();
                    return;
                }
                // otherwise check up the relational operators
                if (Token.contains(peek)) {
                    // if look up said is valid token, takes precedence over single symbol
                    current.append(inputSym);
                }
            }
            currToken = Token.getValue(current.toString());
            inputSym = f.getSym();
        } else {
            // symbol not recognized
            throw new IOException("IOException : Scanner encountered unknown symbol \"" + inputSym + "\"");
        }
    }

    //return current and advance to the next currToken on the input
    public int getSym() throws Exception {
        this.next();
        return currToken;
    }

    // return the last identifier
    public String getIdent() {
        return this.identifiers.get(id);
    }

    public void Error(String errorMsg) throws Exception {
        throw new Exception("Exception: frontend.Scanner encountered an " +
                "exception when advancing to the next symbol.");
    }


    public int getLineNumber() {
        return f.getLineNumber();
    }
}
