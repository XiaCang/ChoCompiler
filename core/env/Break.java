package core.env;

public class Break extends Obj {
    
    @Override
    public String inspect() {
        return "break";
    }

    @Override
    public ObjType type() {
        return ObjType.BREAK;
    }
}
