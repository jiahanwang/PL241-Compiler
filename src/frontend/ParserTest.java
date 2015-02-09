package frontend;

/**
 * Created by Ivan on 2/9/2015.
 */
public class ParserTest {

    public static void main(String[] args) {
        try {
            Parser p = new Parser("test003.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
