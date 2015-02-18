package frontend;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 2/11/2015.
 */
public class ParserHelper {

    static List<String> instructions = new ArrayList<String>();
    private static int basereg = 0;
    private static int pc = 0;

    public static void combine(Operator op, Result x, Result y) throws Exception {
        if((x.t == Result.Type.CONST) && (y.t == Result.Type.CONST)) {
            if(op == Operator.ADD) {
                x.value += y.value;
            } else if(op == Operator.SUB) {
                x.value -= y.value;
            } else if(op == Operator.MUL) {
                x.value *= y.value;
            } else if(op == Operator.DIV) {
                x.value /= y.value;
            } else {
                throw new Exception("Error while doing combine "
                        +op.toString()+" with Results:["+x.toString()+"] and ["+y.toString()+"]");
            }
        } else {
            load(x);
            if(x.regno == 0 ) {
                x.regno = RegisterAllocator.AllocateReg();
                PutF1("ADD", x.regno, 0, 0);
            }
            if(y.t == Result.Type.CONST) {
                PutF1(op.toString()+"I", x.regno, x.regno, y.value);
            }
            else {
                load(y);
                PutF1(op.toString(), x.regno, x.regno, y.regno);
                RegisterAllocator.deallocate(y.regno);
            }
        }
    }

    public static void load(Result x) {
        if(x.t == Result.Type.VAR) {
            x.regno = RegisterAllocator.AllocateReg();
            x.t = Result.Type.REG;
            PutF1("LDW", x.regno, basereg, x.address);
        }
        else if(x.t == Result.Type.CONST) {
            if(x.value == 0) {
                x.regno = 0;
            }
            else {
                x.regno = RegisterAllocator.AllocateReg();
                PutF1("ADDI", x.regno, 0, x.value);
            }
        }
    }

    public static void PutF1(String s, int a, int b, int c) {

        //instructions[pc++] = s+" "+a+","+b+","+c;
        instructions.add(s+" "+a+","+b+","+c);
        pc++;
    }

    public static void print() {
        for(String s : instructions) {
            System.out.println(s);
        }
    }

}
