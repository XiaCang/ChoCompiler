package core.compiler.utils;

import java.util.ArrayList;
import java.util.HashMap;

import core.env.builtin.Builtins;

public class SymbolTable {
    private HashMap<String, Symbol> symbols = new HashMap<>();
    private SymbolTable outer = null;
    private ArrayList<Symbol> frees = new ArrayList<>();
    private Builtins builtins = new Builtins();
    private Symbol curFunc = null;
    public SymbolTable() {}

    public SymbolTable(SymbolTable outer) {
        this.outer = outer;
    }


    public Symbol defineBuiltin(String name) {
        Symbol symbol = new Symbol(name, "builtin", size());
        symbols.put(name, symbol);
        return symbol;
    }

    public Symbol define(String name) {
        String scope = "global";
        if (outer != null) {
            scope = "local";
        }
        Symbol symbol = new Symbol(name, scope, size());
        symbols.put(name, symbol);
        return symbol;
    }

    public Symbol defineFree(Symbol ori) {
        Symbol symbol = new Symbol(ori.name(), "free", frees.size());
        frees.add(ori);
        symbols.put(ori.name(), symbol);
        return symbol;
    }

    public Symbol defineFunction(String name) {
        curFunc = new Symbol(name, "function", size());
        return curFunc;
    }

    public ArrayList<Symbol> frees() {
        return frees;
    }

    public int size() {
        return symbols.size();
    }

    public SymbolTable outer() {
        return outer;
    }

    public boolean contains(String name) {
        if (curFunc != null) {
            if (curFunc.name().equals(name)) {
                return true;
            }
        }


        if (symbols.containsKey(name)) {
            return true;
        }
        if (outer != null) {
            return outer.contains(name);
        }
        return false;
    }


    public Symbol get(String name) {

        if (builtins.get(name) != null) {
            return new Symbol(name, "builtin", builtins.getIndex(name));
        }

        Symbol s = symbols.get(name);
        if (s != null) {
            return s;
        }
        if (outer != null) {
            Symbol s1 = outer.get(name);
            if (s1 == null) {
                return s1;
            }

            if (s1.scope().equals("global")) {
                return s1;
            }

            return defineFree(s1);
        }

        if (curFunc != null) {
            if (curFunc.name().equals(name)) {
                return curFunc;
            }
        }
    

        return null;
    }


}
