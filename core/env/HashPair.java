package core.env;

public class HashPair extends Obj {
    private Obj key;
    private Obj value;

    public HashPair(Obj key, Obj value) {
        this.key = key;
        this.value = value;
    }

    public Obj getKey() {
        return key;
    }

    public Obj getValue() {
        return value;
    }

    @Override
    public String inspect() {
        return key.inspect()+":"+value.inspect();
    }

    @Override
    public ObjType type() {
        return ObjType.HASHPAIR;
    }


}
