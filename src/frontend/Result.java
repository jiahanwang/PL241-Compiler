package frontend;

/**
 * Created by Ivan on 2/6/2015.
 */
public class Result {
    public enum Type {CONST, VAR, ARR, REG, COND};
    public enum Condition {GT, LT, GE, LE, NE, EQ}

    Type t;       // const, var, reg
    Condition c;    // if cond

    int value;      // if constant
    int regno;      // register number, if reg
    int address;    // address if var

    int fixuplocation;

    public String toString() {
        return "TYPE:"+t.toString()+",COND:"+c.toString()+",VALUE:"+value+",REGNO:"+regno+",FIXUP"+fixuplocation;
    }

}
