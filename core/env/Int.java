package core.env;

public class Int extends Obj{
    private int val;
    public Int(int val) {
        this.val = val;
    }

    public int getValue() {
        return val;
    }

    @Override
    public ObjType type() {
        return ObjType.INTEGER;
    }

    @Override
    public String inspect() {
        return String.valueOf(val);
    }

    @Override
    public String toString() {
        return String.valueOf(val);
    }

    public HashKey hashKey() {
        return new HashKey(ObjType.INTEGER, val);
    }
}
