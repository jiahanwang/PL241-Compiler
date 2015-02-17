package frontend;

import IR.BasicBlock;

/**
 * Created by Ivan on 2/9/2015.
 */
public class ParserTest {

    public static void main(String[] args) {
        try {
            Parser p;
            for(int i = 7; i <= 7; i++) {
                p = new Parser("tests/test0"+String.format("%02d", i)+".txt");
                BasicBlock b = p.computation();
                while(b!=null) {
                    System.out.println(b.instruction);
                    if(b.right != null) {
                        System.out.println("----"+b.right.instruction);
                    }
                    //System.out.println("");
                    b = b.left;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
