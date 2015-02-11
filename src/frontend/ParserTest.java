package frontend;

/**
 * Created by Ivan on 2/9/2015.
 */
public class ParserTest {

    public static void main(String[] args) {
        try {
            Parser p;
            for(int i = 1; i <= 1; i++) {
                p = new Parser("tests/test0"+String.format("%02d", i)+".txt");
                ParserHelper.print();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
