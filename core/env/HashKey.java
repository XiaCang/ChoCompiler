package core.env;

import java.util.Objects;
public class HashKey extends Obj {
    ObjType type;
    int val;

    public HashKey() {

    }

    public HashKey(HashKey hKey) {
        this.type = hKey.type;
        this.val = hKey.val;
    }

    public HashKey(ObjType type, int val) {
        this.type = type;
        this.val = val;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(!(o instanceof HashKey)) return false;
        HashKey hashKey = (HashKey) o;
        return type == hashKey.type && val == hashKey.val;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type.ordinal(),val);
    }
}
