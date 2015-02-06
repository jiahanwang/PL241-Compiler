package frontend;

/**
 * Created by ivanleung on 2/5/15.
 */
public class ScannerTest {

    public static void main(String[] args) {
        try {
            Scanner s = new Scanner("test001.txt");
//            for(int i = 0; i < 30; i++) {
//                System.out.println(s.getSym());
//            }
            int curr = s.getSym();
            while(curr != Token.eofToken.value && curr != Token.errorToken.value) {
                System.out.println(curr);
                curr = s.getSym();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
