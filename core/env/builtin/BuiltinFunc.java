package core.env.builtin;

import java.util.ArrayList;

import core.env.Obj;
import core.env.ObjType;

public class BuiltinFunc extends Obj {
    private  AnyFunc anyFunc;

    public BuiltinFunc(AnyFunc anyFunc) {
        this.anyFunc = anyFunc;
    }

    public Obj exec(ArrayList<Obj> args) {
        return anyFunc.apply(args);
    }

    @Override
    public String inspect() {
        return "<builtin function>";
    }

    @Override   
    public ObjType type() {
        return ObjType.BUILTIN;
    }
}
