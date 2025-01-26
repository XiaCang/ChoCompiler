package core.env;

import java.util.HashMap;

public class Environment extends Obj {
    private HashMap<String, Obj> vars = new HashMap<>();

    private Environment outer;

    public Environment(Environment outer) {
        this.outer = outer;
    }

    public Environment() {
        outer = null;
    }

    public Obj get(String name) {
        if (vars.containsKey(name)) {
            return vars.get(name);
        }
        if (outer != null) {
            return outer.get(name);
        }
        return null;
    }

    public boolean has(String name) {
        if (vars.containsKey(name)) {
            return true;
        }
        if (outer != null) {
            return outer.has(name);
        }
        return false;
    }

    public void set(String name, Obj value) {
        if (vars.containsKey(name)) {
            vars.put(name, value);
            return;
        }
        else {
            outer.set(name, value);
        }   
    }

    public void define(String name, Obj value) {
        vars.put(name, value);
    }
}
