package core.env;

import java.util.ArrayList;

public class CompiledFunction extends Obj{

    private ArrayList<Byte> instr = new ArrayList<>();
    private int paramCount;

    public CompiledFunction(ArrayList<Byte> instr, int paramCount) {
        this.instr = instr;
        this.paramCount = paramCount;
    }

    public ArrayList<Byte> getInstructions() {
        return instr;
    }
    
    public int paramCount() {
        return paramCount;
    }

    @Override
    public ObjType type() {
        return ObjType.CompiledFunction;
    }

    @Override
    public String inspect() {
        return "<compiled function>";
    }
}
