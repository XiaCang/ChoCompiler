package core.env;

public class Str extends Obj{
    
    private String val;

    public Str(String val) {
        this.val = val;
    }

    public String getValue() {
        return val;
    }

    @Override
    public ObjType type() {
        return ObjType.STRING;
    }

    @Override
    public String inspect() {
        return val;
    }

    public int length() {
        return val.length();
    }

    public HashKey hashKey() {
        return new HashKey(ObjType.STRING, val.hashCode());
    }
}
