package core.compiler;

import java.util.ArrayList;

import core.env.Obj;

public class CompiledProgram {
    private ArrayList<Byte> instructions;
    private ArrayList<Obj> consts;

    public CompiledProgram(ArrayList<Byte> instructions, ArrayList<Obj> consts) {
        this.instructions = instructions;
        this.consts = consts;
    }

    public ArrayList<Byte> instructions() {
        return instructions;
    }

    public ArrayList<Obj> consts() {
        return consts;
    }

    @Override
    public String toString() {
        return "CompiledProgram";
    }

}
