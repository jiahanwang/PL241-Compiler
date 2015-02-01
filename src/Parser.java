/**
 * Created by Ivan on 1/31/2015.
 */
public class Parser {

    private int scannerSym; //the current token on the input
    private void Next() {
        scannerSym = Scanner.getSym();
        // advance to the next token
    }
}
