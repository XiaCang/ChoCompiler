package core.compiler;

import java.util.ArrayList;

import core.env.Obj;

public class CompiledProgram {
    private ArrayList<Byte> instructions;
    private ArrayList<Obj> consts;
    private ArrayList<String> errors;
    public CompiledProgram(ArrayList<Byte> instructions, ArrayList<Obj> consts, ArrayList<String> errors) {
        this.instructions = instructions;
        this.consts = consts;
        this.errors = errors;
    }

    public ArrayList<Byte> instructions() {
        return instructions;
    }

    public ArrayList<Obj> consts() {
        return consts;
    }

    public ArrayList<String> errors() {
        return errors;
    }

    @Override
    public String toString() {
        return "CompiledProgram";
    }

}
