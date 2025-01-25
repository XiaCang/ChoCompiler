package core.is;

import java.util.ArrayList;
import java.util.HashMap;

public class InstructionSet {
    private HashMap<Opcode, Defination> codes = new HashMap<>();

    public InstructionSet() {
        codes.put(Opcode.Nop, new Defination("Nop", 0));
        codes.put(Opcode.Load, new Defination("Load", 2));
        codes.put(Opcode.Add, new Defination("Add", 0));
        codes.put(Opcode.Pop, new Defination("Pop", 0));
        codes.put(Opcode.Sub, new Defination("Sub", 0));
        codes.put(Opcode.Mul, new Defination("Mul", 0));
        codes.put(Opcode.Div, new Defination("Div", 0));
        codes.put(Opcode.True, new Defination("True", 0));
        codes.put(Opcode.False, new Defination("False", 0));
        codes.put(Opcode.Eq, new Defination("Eq", 0));
        codes.put(Opcode.Ne, new Defination("Ne", 0));
        codes.put(Opcode.Gt, new Defination("Gt", 0));
        codes.put(Opcode.Ge, new Defination("Ge", 0));
        codes.put(Opcode.Lt, new Defination("Lt", 0));
        codes.put(Opcode.Le, new Defination("Le", 0));
        codes.put(Opcode.Neg, new Defination("Neg", 0));
        codes.put(Opcode.Not, new Defination("Not", 0));
        codes.put(Opcode.J, new Defination("J", 2));
        codes.put(Opcode.Jf, new Defination("Jf", 2));
        codes.put(Opcode.NULL, new Defination("Null", 0));
        codes.put(Opcode.SetGlobal, new Defination("SetGlobal", 2));
        codes.put(Opcode.GetGlobal, new Defination("GetGlobal", 2));
        codes.put(Opcode.Array, new Defination("Array", 2));
        codes.put(Opcode.Hash, new Defination("Hash", 2));
        codes.put(Opcode.Index, new Defination("Index", 0));
        codes.put(Opcode.Call, new Defination("Call", 1));
        codes.put(Opcode.ReturnValue, new Defination("ReturnValue", 0));
        codes.put(Opcode.Return, new Defination("Return", 0));
        codes.put(Opcode.SetLocal, new Defination("SetLocal", 2));
        codes.put(Opcode.GetLocal, new Defination("GetLocal", 2));
        codes.put(Opcode.GetBuiltin, new Defination("GetBuiltin", 1));
        codes.put(Opcode.Closure, new Defination("Closure", 2,1));
        codes.put(Opcode.GetFree, new Defination("GetFree", 1));
        codes.put(Opcode.SetFree, new Defination("SetFree", 1));
        codes.put(Opcode.CurClosure, new Defination("CurClosure", 0));
        codes.put(Opcode.Assign, new Defination("Assign", 2));
        codes.put(Opcode.Allocate, new Defination("Allocate", 2));
        codes.put(Opcode.SetArray, new Defination("SetArray", 0));
    }

    private Defination getDefine(Opcode code){
        return codes.get(code);
    }

    public int lengthOf(Opcode code){
        Defination def = getDefine(code);
        int ret = 1;
        for (int i = 0; i < def.operandCount(); i++) {
            ret += def.len(i);
        }
        return ret;
    }

    public ArrayList<Byte> encode(Opcode code, int ... operands){
        Defination defination = getDefine(code);
        ArrayList<Byte> ret = new ArrayList<>();
        ret.add((byte) code.ordinal());
        if (defination.operandCount() == 0) {
            return ret;
        }
        for (int i = 0; i < operands.length; i++) {
            int arg = operands[i];
            ArrayList<Byte> argBytes = getBytes(arg, defination.len(i));
            ret.addAll(argBytes);
        }
        return ret;
    }

    public ArrayList<String> decode(ArrayList<Byte> bytes){
        ArrayList<String> ret = new ArrayList<>();

        for (int i = 0; i < bytes.size(); ) {
            String instr = String.format("%04X", i) + " ";
            Opcode c = Opcode.values()[bytes.get(i)];

            instr += String.format("  %02X", bytes.get(i).byteValue());
            i ++;
            Defination def = getDefine(c);
            int j = 0;
            if (def.operandCount() == 0) {
                instr = instr + "             ";
                
                instr += def.name();
                ret.add(instr);
                continue;
            }

            
            for (j = 0; j < def.operandCount(); j++) {
                for (int k = 0; k < def.len(j); k++) {
                    instr = instr + " " + String.format("%02X", bytes.get(i + k).byteValue());
                }
            }

            for (j = lengthOf(c); j < 5; j++) {
                instr = instr + "   ";
            }

            instr += " " + def.name();
            for (j = 0; j < def.operandCount(); j++) {
                instr = instr + " " + getInt(new ArrayList<>(bytes.subList(i, i + def.len(j))));
                i += def.len(j);
            }
            ret.add(instr);
        }
        return ret;
    }

    private ArrayList<Byte> getBytes(int num, int len) {
        ArrayList<Byte>  bytes = new ArrayList<>();
        for (int i = len - 1; i >= 0; i--) {
            bytes.add((byte) (num >> (i * 8)));
        }
        return bytes;
    }

    private int getInt(ArrayList<Byte> bytes) {
        int num = 0;
        for (int i = 0; i < bytes.size(); i++) {
            int b = bytes.get(i) & 0xFF;
            num = (num << 8) + b;
        }
        return num;
    }
}


class Defination {
    private String name;
    private ArrayList<Integer> operandLen = new ArrayList<>();

    public Defination(String name, int... len) {
        this.name = name;
        for (int i = 0; i < len.length; i++) {
            this.operandLen.add(len[i]);
        }
    }

    public String name() {
        return name;
    }

    public ArrayList<Integer> operandLen() {
        return operandLen;
    }

    public int operandCount() {
        if (operandLen.get(0) == 0) return 0;
        return operandLen.size();
    }

    public int len(int index) {
        return operandLen.get(index);
    }
}
