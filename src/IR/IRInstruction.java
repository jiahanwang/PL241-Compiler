package IR;

/**
 * Created by Ivan on 2/18/2015.
 */
public class IRInstruction {

    String op;
    String a, b, c;
    BasicBlock bb;

    public IRInstruction(String op, String b) {
        this.op = op;
        this.b = b;
    }

    public IRInstruction(String op, String a, String b) {
        this.op = op;
        this.a = a;
        this.b = b;
    }

    public IRInstruction(String op, String a, String b, String c) {
        this.op = op;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public BasicBlock getBlock() {
        return bb;
    }

    public void setBasicBlock(BasicBlock bb) {
        this.bb = bb;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(op);

        if(a != null) {
            sb.append(" "+a);
        }
        if(b != null) {
            sb.append(","+b);
        }
        if(c != null) {
            sb.append(","+c);
        }

        return sb.toString();
    }


}
