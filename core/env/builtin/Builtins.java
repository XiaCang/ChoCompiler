package core.env.builtin;

import core.env.Array;
import core.env.Error;
import core.env.Hash;
import core.env.NULL;
import core.env.Obj;
import core.env.ObjType;
import core.env.Str;
import core.env.Int;

import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;

public class Builtins extends Obj{

    private HashMap<String, BuiltinFunc> functions = new HashMap<>();
    private ArrayList<BuiltinFunc> flatted = new ArrayList<>();
    private HashMap<String, Integer> indices = new HashMap<>();
    public Builtins() { 
        init();
    }

    public BuiltinFunc get(String name) {
        return functions.get(name);
    }

    private void init() {
        add("len", new BuiltinFunc((AnyFunc)(this::len)));
        add("print", new BuiltinFunc((AnyFunc)(this::print)));
        add("println", new BuiltinFunc((AnyFunc)(this::println)));
        add("type", new BuiltinFunc((AnyFunc)(this::type)));
        add("append", new BuiltinFunc((AnyFunc)(this::append)));
        add("readString", new BuiltinFunc((AnyFunc)(this::readString)));
        add("readLine", new BuiltinFunc((AnyFunc)(this::readLine)));
        add("readInt", new BuiltinFunc((AnyFunc)(this::readInt)));
    }

    private void add(String name, BuiltinFunc func) {
        int index = functions.size();
        functions.put(name, func);
        indices.put(name, index);
        flatted.add(func);
    }

    public int getIndex(String name) {
        return indices.get(name);
    }

    public BuiltinFunc get(int index) {
        return flatted.get(index);
    }

    private Obj readString(ArrayList<Obj> args) {
        if (args.size() > 0) {
            return new Error("readString takes no arguments");
        }
        Scanner scanner = new Scanner(System.in);
        String str = scanner.next();

        return new Str(str);
    }

    private Obj readLine(ArrayList<Obj> args) {
        if (args.size() > 0) {
            return new Error("readLine takes no arguments");
        }
        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine();

        return new Str(str);
    }

    private Obj readInt(ArrayList<Obj> args) {
        if (args.size() > 0) {
            return new Error("readInt takes no arguments");
        }
        Scanner scanner = new Scanner(System.in);
        int num = scanner.nextInt();
        return new Int(num);
    }

    private Obj len(ArrayList<Obj> args) {
        if (args.get(0).type() == ObjType.ARRAY) {
            Array arr = (Array) args.get(0);
            return new Int(arr.length());
        } else if (args.get(0).type() == ObjType.STRING) {
            Str str = (Str) args.get(0);
            return new Int(str.length());
        } else if (args.get(0).type() == ObjType.HASH) {
            Hash hash = (Hash) args.get(0);
            return new Int(hash.size());
        } else {
            return new Error("can't get length of: " + args.get(0).type());
        }
    }

    private Obj print(ArrayList<Obj> args) {
        for (Obj obj : args) {
            System.out.print(obj.inspect() + " ");
        }
        return new NULL();
    }
    
    private Obj println(ArrayList<Obj> args) {
        for (Obj obj : args) {
            System.out.print(obj.inspect() + " ");
        }
        System.out.println();
        return new NULL();
    }

    private Obj type(ArrayList<Obj> args) {
        return new Str(args.get(0).type().toString().toLowerCase());
    }

    private Obj append(ArrayList<Obj> args) {
        if (args.size() < 2) {
            return new Error("not enough arguments for append");
        }
        Obj arr = args.get(0);
        if (arr.type() == ObjType.ARRAY) {
            Array array = (Array) arr;
            for (int i = 1; i < args.size(); i++) {
                array.add(args.get(i));
            }
            return array;
        }
        return new Error("can only append to arrays");
    }
    
}
