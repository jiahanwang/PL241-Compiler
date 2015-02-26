package edu.uci.cs241.frontend;

/**
 * Created by ivanleung on 2/5/15.
 */
public class ScannerTest {

    public static void main(String[] args) {
        try {
            Scanner s = new Scanner("tests/test016.txt");
            int curr = s.getSym();
            while(curr != Token.eofToken.value && curr != Token.errorToken.value) {
                System.out.print(curr+" "+Token.getRepresentation(curr));
                if(curr == Token.ident.value) {
                    System.out.print(" "+s.id);
                }
                curr = s.getSym();
                System.out.println("");
            }

            System.out.println("Total Line count : " +s.getLineNumber());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
