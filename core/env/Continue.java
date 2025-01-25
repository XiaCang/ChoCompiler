package core.env;

public class Continue extends Obj {
    @Override
    public String inspect() {
        return "continue";
    }

    @Override
    public ObjType type() {
        return ObjType.CONTINUE;
    }
    
}
