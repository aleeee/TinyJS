package tinyjs;

import tinyjs.Node.Function;

public class Evaluate {

    enum EvalType {

        UNDEFINED,
        INT,
        BOOL,
        STRING,
        FUN
    }

    public EvalType type;
    public Object value;

    public Evaluate() {
        this.value = null;
        this.type = EvalType.UNDEFINED;
    }

    public Evaluate(EvalType type) {
        this.value = null;
        this.type = type;
    }

    public Evaluate(int newValue) {
        this.value = newValue;
        this.type = EvalType.INT;
    }

    public Evaluate(boolean newValue) {
        this.value = newValue;
        this.type = EvalType.BOOL;
    }

    public Evaluate(String newValue) {
        this.value = newValue;
        this.type = EvalType.STRING;
    }

    public Evaluate(Function newValue) {
        this.value = newValue;
        this.type = EvalType.FUN;
    }

    public int getIValue() {
        if (this.type == EvalType.INT) {
            return (int) this.value;
        } else {
            System.out.println("Can not return an integer value.");
            System.exit(1);
            return 1;
        }
    }

    public boolean getBValue() {
        if (this.type == EvalType.BOOL) {
            return (boolean) this.value;
        } else {
            System.out.println("Can not return a boolean value.");
            System.exit(1);
            return false;
        }
    }

    public String getSValue() {
        if (this.type == EvalType.STRING) {
            return (String) this.value;
        } else {
            System.out.println("Can not return a string value.");
            System.exit(1);
            return null;
        }
    }

    public Node.Function getFValue() {
        if (this.type == EvalType.FUN) {
            return (Node.Function) this.value;
        } else {
            System.out.println("Can not return a function value.");
            System.exit(1);
            return null;
        }
    }

    public static boolean equal(Evaluate v1, Evaluate v2) {
        if (v1.type != v2.type) {
            return false;
        }
        switch (v1.type) {
            case INT:
                if (v1.getIValue() == v2.getIValue()) {
                    return true;
                } else {
                    return false;
                }
            case BOOL:
                if (v1.getBValue() == v2.getBValue()) {
                    return true;
                } else {
                    return false;
                }
            case STRING:
                if (v1.getSValue().compareTo(v2.getSValue()) == 0) {
                    return true;
                } else {
                    return false;
                }
            case FUN:
                if (v1.getFValue() == v2.getFValue()) {
                    return true;
                } else {
                    return false;
                }
            default:
                return true;
        }
    }
}
