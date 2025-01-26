package core.env;

public class ReturnValue extends Obj {
    private Obj value;
    public ReturnValue(Obj value) {
        this.value = value;
    }

    public Obj getValue() {
        return value;
    }

    @Override
    public ObjType type() {
        return ObjType.RETURNVALUE;
    }

    @Override
    public String inspect() {
        return value.inspect();
    }
}
