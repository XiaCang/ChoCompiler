package core.env;

import java.util.ArrayList;

public class Closure extends Obj {
    CompiledFunction func;
    ArrayList<Obj> freeVars = new ArrayList<>();

    public Closure(CompiledFunction func, ArrayList<Obj> freeVars) {
        this.func = func;
        this.freeVars = freeVars;
    }

    public CompiledFunction function() {
        return func;
    }

    public ArrayList<Obj> freeVars() {
        return freeVars;
    }

    @Override
    public ObjType type() {
        return ObjType.Closure;
    }

    @Override
    public String toString() {
        return "Closure{" +
                "func=" + func +
                ", freeVars=" + freeVars +
                '}';
    }

}
