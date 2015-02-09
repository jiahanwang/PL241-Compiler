package frontend;

/**
 * Created by Ivan on 2/6/2015.
 */
public class Result {
    public enum Type {CONST, VAR, REG};
    public enum Condition {GT, LT, GE, LE, NE, EQ}

    Type t;       // const, var, reg

    int regno;      // register number

    Condition c;
}
