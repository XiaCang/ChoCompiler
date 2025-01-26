package core.env;

public class NULL extends Obj {
    @Override
    public ObjType type() {
        return ObjType.NULL;
    }

    @Override
    public String inspect() {
        return "null";
    }
}
