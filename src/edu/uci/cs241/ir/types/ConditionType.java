package edu.uci.cs241.ir.types;

/**
 * Created by hanplusplus on 2/24/15.
 */
public enum ConditionType {
    GT,
    LT,
    GE,
    LE,
    NE,
    EQ;

    public ConditionType opposite() {
        switch(this){
            case GT:
                return LE;
            case LT:
                return GE;
            case GE:
                return LT;
            case LE:
                return GT;
            case NE:
                return EQ;
            case EQ:
                return NE;
            default:
                return null;
        }
    }
}
