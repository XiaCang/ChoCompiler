package core.compiler.utils;

import java.util.ArrayList;

import core.is.InstructionSet;
import core.is.Opcode;

public class CompileScope {
    private ArrayList<Byte> instructions = new ArrayList<>();
    private int params = 0;

    private Opcode lastOpcode;
    private int lastPos;

    private Opcode preOpcode;
    private int prePos;

    InstructionSet is = new InstructionSet();

    public CompileScope(int params) {
        this.params = params;
    }

    public boolean removeLastPop() {
        if (lastOpcode == Opcode.Pop) {
            instructions.remove(lastPos);
            return true;
        }
        return false;
    }

    public void setLast(Opcode lastOpcode, int lastPos) {
        this.preOpcode = lastOpcode;
        this.prePos = lastPos;
        this.lastOpcode = lastOpcode;
        this.lastPos = lastPos;
    }

    public ArrayList<Byte> instructions() {
        return instructions;
    }

    public boolean lastIs(Opcode opcode) {
        return lastOpcode == opcode;
    }

    public int params() {
        return params;
    }

}