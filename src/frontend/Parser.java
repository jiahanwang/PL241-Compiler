package frontend;

import java.io.IOException;

/**
 * Created by Ivan on 1/31/2015.
 */
public class Parser {

    private int scannerSym; //the current currToken on the input
    private Scanner s;

    public Parser(String path) throws IOException {
        s = new Scanner(path);
    }

    private void Next() throws IOException {
        this.scannerSym = s.getSym();
        // advance to the next currToken
    }
}
