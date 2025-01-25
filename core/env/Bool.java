package core.env;

public class Bool extends Obj{
    private boolean value;

    public Bool(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public HashKey hashKey() {
        return new HashKey(ObjType.BOOLEAN, value ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(!(o instanceof Bool)) return false;
        Bool bool = (Bool) o;
        return value == bool.value;
    }

    @Override
    public ObjType type() {
        return ObjType.BOOLEAN;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }


}
