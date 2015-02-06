package frontend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    private String[] keywords = {"then", "do", "od", "fi", "else", "let",
            "call", "if", "while", "return", "var", "array", "procedure", "main"};

    private List<Character> singleSymbols = Arrays.asList('*', ',', ';', '{', '}', '[', ']', '.', '(', ')');
    private List<Character> checkSymbols = Arrays.asList('=', '!', '<', '>');
    // double symbols
    // ==, !=, <=, >=, ++?, --?, //

    // the last number encountered
    public int number;

    // the last identifier encountered
    public int id;

    // the id of the token that has just been parsed by next()
    public int currToken;

    public Scanner(String fileName) throws IOException {
        //constructor: open file and scan the first currToken into 'sym'
        f = new FileReader(fileName);
        identifiers = new ArrayList<String>();
        //blacklist the keywords
        identifiers.addAll(Arrays.asList(keywords));
        // update size of identifiers
        id = identifiers.size() - 1;
    }

    private void next() throws IOException {
        current = new StringBuilder();  //building out the currToken string in case its an identifier

        // Advance to next available input
        while (inputSym == ' ' || inputSym == '\t' || inputSym == '\n')
        {
            inputSym = f.getSym();
        }

        // We've reached the end of file
        if(inputSym == -1) {
            currToken = Token.eofToken.value;
        }
        // input [A-z][A-z0-9]
        // will be identifier
        if(Character.isAlphabetic(inputSym)) {
            currToken = Token.ident.value;
            while(Character.isAlphabetic(inputSym) || Character.isDigit(inputSym)) {
                //read until no more alphanumeric chars
                current.append(inputSym);
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
        else if(Character.isDigit(inputSym)) {
            currToken = Token.number.value;
            while(Character.isDigit(inputSym)) {
                // read until no more numbers
                current.append(inputSym);
            }
            number = Integer.parseInt(current.toString());
        }
        // input '*', ',', ';', '{', '}', '[', ']', '.', '(', ')'
        else if(singleSymbols.contains(inputSym)) {
            // single symbol
            currToken = Token.getValue(Character.toString(inputSym));
        }
        // these symbols need LL(1) first
        else if(checkSymbols.contains(inputSym)) {
            // consume next and check the Tokens agn
            current.append(inputSym);
            f.getSym();
            if(Token.contains(current.toString()+inputSym)) {
                // look up said is valid token, takes precendence
                current.append(inputSym);
            }
            currToken = Token.getValue(current.toString());
        }
        else {
            // symbol not recognized
            throw new IOException("IOException : Scanner encountered unknown symbol \""+inputSym+"\"");
        }
    }

    //return current and advance to the next currToken on the input
    public int getSym() throws IOException {
        this.next();
        return currToken;
    }

    public void Error(String errorMsg) throws Exception {
        throw new Exception("Exception: frontend.Scanner encountered an " +
                "exception when advancing to the next symbol.");
    }


}