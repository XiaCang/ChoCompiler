package core.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import core.compiler.CompiledProgram;
import core.env.Array;
import core.env.Bool;
import core.env.Closure;
import core.env.CompiledFunction;
import core.env.Error;
import core.env.Hash;
import core.env.HashKey;
import core.env.HashPair;
import core.env.Int;
import core.env.NULL;
import core.env.Obj;
import core.env.ObjType;
import core.env.Str;
import core.env.builtin.BuiltinFunc;
import core.env.builtin.Builtins;
import core.is.InstructionSet;
import core.is.Opcode;

public class Vm {

    private VmStack stack = new VmStack(1024);
    private ArrayList<Obj> constants;
    private Stack<Frame> frames = new Stack<>();

    private Obj lastPoped = new NULL();
    private InstructionSet is = new InstructionSet();
    private Builtins builtins = new Builtins();
    public Vm(CompiledProgram cp) {
        constants = cp.consts();
        ArrayList<Byte> instructions = cp.instructions();
        CompiledFunction mcf = new CompiledFunction(instructions, 0);
        Closure mc = new Closure(mcf, new ArrayList<>());
        Frame mf = new Frame(0, 0, mc);
        frames.push(mf);
    }

    public Obj run() {
        Opcode code;
        int tmp;
        Obj tmpObj;
        int ip;
        while (curFrame().ip() < curInstructions().size()) {
            ip = curFrame().ip();


            //System.out.println("ip: " + ip + "stack size:" + stack.size());
            
            code = Opcode.values()[curInstructions().get(ip).intValue()];
            switch (code) {
                case NULL:
                    stack.push(new NULL());
                    break;
                case Load:
                    tmp = readInt(ip + 1, 2);
                    stack.push(constants.get(tmp));
                    break;
                case Add: case Sub: case Mul: case Div:
                    executeInfix(code);
                    break;
                case Eq: case Ne: case Gt: case Ge: case Lt: case Le:
                    executeComparison(code);
                    break;
                case Neg: case Not:
                    executePrefixOperation(code);
                    break;
                case Pop:
                    lastPoped = stack.pop();
                    break;
                case True:
                    stack.push(new Bool(true));
                    break;
                case False:
                    stack.push(new Bool(false));
                    break;
                case Array:
                    tmp = readInt(ip + 1, 2);
                    executeArray(tmp);
                    break;
                case Hash:
                    tmp = readInt(ip + 1, 2);
                    executeHash(tmp);
                    break;
                case Index:
                    executeIndex();
                    break;
                case Allocate:
                    tmp = readInt(ip + 1, 2);
                    stack.setSp(stack.sp() + tmp);
                    break;
                case SetLocal:
                    tmp = readInt(ip + 1, 2);
                    tmpObj = stack.pop();
                    stack.set(curFrame().basePtr() + tmp, tmpObj);
                    break;
                case Assign:
                    tmp = readInt(ip + 1, 2);
                    tmpObj = stack.pop();
                    stack.set(curFrame().basePtr() + tmp, tmpObj);
                    stack.push(tmpObj);
                    break;
                case GetLocal:
                    tmp = readInt(ip + 1, 2);
                    stack.push(stack.get(curFrame().basePtr() + tmp));
                    break;
                case SetGlobal:
                    tmp = readInt(ip + 1, 2);
                    tmpObj = stack.pop();
                    stack.set(tmp, tmpObj);
                    break;
                case GetGlobal:
                    tmp = readInt(ip + 1, 2);
                    stack.push(stack.get(tmp));
                    break;
                case GetBuiltin:
                    tmp = readInt(ip + 1, 1);
                    stack.push(builtins.get(tmp));
                    break;
                case Jf: case J:
                    executeJump(code);
                    break;
                case Call:
                    tmp = readInt(ip + 1, 1);
                    executeCall(tmp);
                    break;
                case ReturnValue:
                    tmpObj = stack.pop();
                    Frame f = frames.pop();
                    curFrame().inc(is.lengthOf(Opcode.Call)-is.lengthOf(Opcode.ReturnValue));
                    stack.setSp(f.basePtr() - 1);
                    stack.push(tmpObj);
                    break;
                case Return:
                    frames.pop();
                    stack.setSp(curFrame().basePtr() - 1);
                    stack.push(new NULL());
                    curFrame().inc(is.lengthOf(Opcode.Call)-is.lengthOf(Opcode.Return));
                    break;
                case Closure:
                    tmp = readInt(ip + 1, 2);
                    int tmp2 = readInt(ip + 3, 1);
                    executeClosure(tmp, tmp2);
                    break;
                case GetFree:
                    tmp = readInt(ip + 1, 1);
                    Closure curC = curFrame().closure();
                    stack.push(curC.freeVars().get(tmp));
                    break;
                case SetFree:
                    tmp = readInt(ip + 1, 1);
                    tmpObj = stack.pop();
                    curC = curFrame().closure();
                    curC.freeVars().set(tmp, tmpObj);
                    break;
                case CurClosure:
                    stack.push(curFrame().closure());
                    break;
                case SetArray:  
                    executeSetArray();
                    break;
                default:
                    return new Error("unknown instruction: " + code);
            }

            curFrame().inc(is.lengthOf(code));
        }

        return lastPoped;
    }

    public int stackCount() {
        return stack.sp();
    }

    public void pushClosure(int index, int freeCount) {
        Obj cf = constants.get(index);
        CompiledFunction mcf = (CompiledFunction) cf;
        ArrayList<Obj> frees = new ArrayList<>();
        for (int i = 0; i < freeCount; i++) {
            frees.add(stack.get(stack.sp() + i - freeCount));
        }

        Closure mc = new Closure(mcf, frees);
        stack.push(mc);
    }

    private void executeSetArray() {
        Int index = (Int) stack.pop();
        Array arr = (Array) stack.pop();
        Obj value = stack.pop();
        arr.getElements().set(index.getValue(), value);
        stack.push(arr);
    }

    private void executeClosure(int index, int freeCount) {
        pushClosure(index, freeCount);
    }

    private void executeCall(int argc) {
        Obj cf =  stack.get(stack.sp() - argc - 1);

        if (cf.type() == ObjType.BUILTIN) {
            BuiltinFunc bf = (BuiltinFunc) cf;
            ArrayList<Obj> args = new ArrayList<>();
            for (int i = 0; i < argc; i++) {
                args.add(stack.pop());
            }

            Obj result = bf.exec(args);
            stack.pop();
            stack.push(result);
        }
        else {

            Closure closure = (Closure) cf;
            Frame frame = new Frame(stack.sp() - closure.function().paramCount(), -is.lengthOf(Opcode.Call), closure);
            frames.push(frame);        
        }

        
    }

    private void executeJump(Opcode code) {
        int offset = readSignedInt(curFrame().ip() + 1, 2);

        if (code == Opcode.Jf) {
            Obj obj = stack.pop();

            if (obj.type() == ObjType.BOOLEAN) {
                if (((Bool) obj).getValue() ) return;
                curFrame().inc(offset);
                return;
            }

            if (obj.type() == ObjType.NULL) {
                curFrame().inc(offset);
                return;
            }
            
            stack.push(new Error("unknown operator: " + code + " for " + obj.type()));
        }

        if (code == Opcode.J) {
            curFrame().inc(offset);
        }
    }

    private void executeIndex() {
        Obj index = stack.pop();
        Obj left = stack.pop();

        if (left.type() == ObjType.ARRAY && index.type() == ObjType.INTEGER) {
            Array array = (Array) left;
            int i = ((Int) index).getValue();
            if (i < 0 || i >= array.getElements().size()) {
                stack.push(new Error("index out of bounds"));
                return;
            }
            stack.push(array.getElements().get(i));
            return;
        }

        if (left.type() == ObjType.HASH) {
            Hash hash = (Hash) left;
            HashKey hashKey = new HashKey();
            if (index.type() == ObjType.STRING) {
                Str key = (Str) index;
                hashKey = key.hashKey();
            }
            if (index.type() == ObjType.INTEGER) {
                Int key = (Int) index;
                hashKey = key.hashKey();
            }
            if (index.type() == ObjType.BOOLEAN) {
                Bool key = (Bool) index;
                hashKey = key.hashKey();
            }
            HashPair value = hash.get(hashKey);
            if (value == null) {
                stack.push(new Error("key not found in hash: " + index.inspect()));
                return;
            }
            stack.push(value.getValue());
            return;
        }

        stack.push(new Error("index operator not supported: " + left.inspect() + "[" + index.inspect() + "]"));

    }

    private void executeHash(int size) {
        HashMap<HashKey,HashPair> hm = new HashMap<>();
        HashKey hk;
        HashPair hp;
        Obj key;
        Obj val;
        for (int i = 0; i< size;i++) {
            key = stack.pop();
            val = stack.pop();

            switch (key.type()) {
                case INTEGER:
                    hk = new HashKey(ObjType.INTEGER, ((Int)key).getValue());
                    break;
                case BOOLEAN:
                    hk = new HashKey(ObjType.BOOLEAN, ((Bool)key).getValue() ? 1 : 0);
                    break;
                case STRING:
                    hk = new HashKey(ObjType.STRING, ((Str)key).getValue().hashCode());
                    break;
                default:
                    stack.push(new Error("unknown key type: " + key.type()));
                    return;
            }

            hp = new HashPair(key, val);

            hm.put(hk, hp);
        }

        stack.push(new Hash(hm));
    }

    private void executeArray(int size) {
        ArrayList<Obj> array = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            array.add(stack.pop());
        }
        Array arr = new Array(array);
        stack.push(arr);
    }

    private void executeComparison(Opcode code) {
        Obj left = stack.pop();
        Obj right = stack.pop();

        if (left.type() == ObjType.INTEGER && right.type() == ObjType.INTEGER) {
            executeComparisonInteger(code, (Int) left, (Int) right);
            return;
        }

        if (left.type() == ObjType.BOOLEAN && right.type() == ObjType.BOOLEAN) {
            executeComparisonBoolean(code, (Bool) left, (Bool) right);
            return;
        }

        if (left.type() == ObjType.STRING && right.type() == ObjType.STRING) {
            executeComparisonString(code, (Str) left, (Str) right);
            return;
        }

        if (left.type() == ObjType.NULL && right.type() == ObjType.NULL) {
            executeComparisonNull(code, (NULL) left, (NULL) right);
            return;
        }

        stack.push(new Error("unknown operator: " + code + " for " + left.type()));
    }

    private void executeComparisonNull(Opcode code, NULL left, NULL right) {
        switch (code) {
            case Eq:
                stack.push(new Bool(true));
                break;
            case Ne:
                stack.push(new Bool(false));
                break;
            default:
                stack.push(new Error("unknown operator: " + code + " for null"));
                break;
        }
    }

    private void executeComparisonString(Opcode code, Str left, Str right) {
        switch (code) {
            case Eq:
                stack.push(new Bool(left.getValue().equals(right.getValue())));
                break;
            case Ne:
                stack.push(new Bool(!left.getValue().equals(right.getValue())));
                break;
            default:
                stack.push(new Error("unknown operator: " + code + " for string"));
                break;
        }
    }

    private void executeComparisonBoolean(Opcode code, Bool left, Bool right) {
        switch (code) {
            case Eq:
                stack.push(new Bool(left.getValue() == right.getValue()));
                break;
            case Ne:
                stack.push(new Bool(left.getValue() != right.getValue()));
                break;
            default:
                stack.push(new Error("unknown operator: " + code + " for boolean"));
                break;
        }
    }

    private void executeComparisonInteger(Opcode code, Int left, Int right) {
        switch (code) {
            case Eq:
                stack.push(new Bool(left.getValue() == right.getValue()));
                break;
            case Ne:
                stack.push(new Bool(left.getValue() != right.getValue()));
                break;
            case Gt:
                stack.push(new Bool(left.getValue() > right.getValue()));
                break;
            case Ge:
                stack.push(new Bool(left.getValue() >= right.getValue()));
                break;
            case Lt:
                stack.push(new Bool(left.getValue() < right.getValue()));
                break;
            case Le:
                stack.push(new Bool(left.getValue() <= right.getValue()));
                break;
            default:
                stack.push(new Error("unknown operator: " + code + " for integer"));
                break;
        }
    }

    private void executePrefixOperation(Opcode code) {
        Obj obj = stack.pop();
        if (obj.type() == ObjType.INTEGER) {
            executePrefixInteger(code, (Int) obj);
            return;
        }

        if (obj.type() == ObjType.BOOLEAN) {
            executePrefixBoolean(code, (Bool) obj);
            return;
        }

        if (obj.type() == ObjType.NULL && code == Opcode.Not) {
            stack.push(new Bool(true));
            return;
        }

        stack.push(new Error("unknown operator: " + code + " for " + obj.type()));
    }

    private void executePrefixInteger(Opcode code, Int obj) {
        switch (code) {
            case Neg:
                stack.push(new Int(-obj.getValue()));
                break;
            default:
                stack.push(new Error("unknown operator: " + code + " for integer"));
                break;
        }
    }

    private void executePrefixBoolean(Opcode code, Bool obj) {
        switch (code) {
            case Not:
                stack.push(new Bool(!obj.getValue()));
                break;
            default:
                stack.push(new Error("unknown operator: " + code + " for boolean"));
                break;
        }
    }

    private void executeInfix(Opcode code) {
        Obj left = stack.pop();
        Obj right = stack.pop();

        if (left.type() == ObjType.INTEGER && right.type() == ObjType.INTEGER) {
            executeInfixInteger(code, (Int) left, (Int) right);
            return;
        }

        if (left.type() == ObjType.STRING && right.type() == ObjType.STRING) {
            executeInfixString(code, (Str) left, (Str) right);
            return;
        }

        stack.push(new Error("type mismatch for infix operation"));
    }

    private void executeInfixInteger(Opcode code, Int left, Int right) {
        switch (code) {
            case Add:
                stack.push(new Int(left.getValue() + right.getValue()));
                break;
            case Sub:
                stack.push(new Int(left.getValue() - right.getValue()));
                break;
            case Mul:
                stack.push(new Int(left.getValue() * right.getValue()));
                break;
            case Div:
                if (right.getValue() == 0) {
                    stack.push(new Error("divide by zero"));
                    return;
                }
                stack.push(new Int(left.getValue() / right.getValue()));
                break;
            default:
                stack.push(new Error("unknown operator: " + code + "for integer"));
                break;
        }
    }

    private void executeInfixString(Opcode code, Str left, Str right) {
        switch (code) {
            case Add:
                stack.push(new Str(left.getValue() + right.getValue()));
                break;

            default:
            stack.push(new Error("unknown operator: " + code + "for String"));
                break;
        }
    }

    private ArrayList<Byte> curInstructions() {
        return curFrame().closure().function().getInstructions();
    }

    private Frame curFrame() {
        return frames.peek();
    }

    private int readInt(int startip, int len) {
        int num = 0;
        for (int i = 0; i < len; i++) {
            int b = curInstructions().get(i + startip) & 0xFF;
            num = (num << 8) + b;
        }
        return num;
    }

    private int readSignedInt(int startip, int len) {
        ArrayList<Byte> instructions = curInstructions();
        int result = 0;

        for (int i = 0; i < len; i++) {
            byte currentByte = instructions.get(startip + i);
            result = (result << 8) | (currentByte & 0xFF);
        }

        if ((result & (1 << (len * 8 - 1))) != 0) {
            result -= (1 << (len * 8));
        }

        return result;
    }


}
