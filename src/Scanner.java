/**
 * Created by Ivan on 1/31/2015.
 */
public class Scanner {

    private char inputSym; //the current char on the input
    private void Next() {
        inputSym = FileReader.GetSym();
        //advance to next character
    }

    //return current and advance to the next token on the input
    public int GetSym();

    // the last number encountered
    public int number;

    // the last identifier encountered
    public int id;

    public void Error(String errorMsg) {
        FileReader.error(erroMsg);
    }

    public Scanner(Stirng fileName) {
        //constructor: open file and scan the first token into 'sym'
    }
}
