package core.compiler.utils;

public class Symbol {
    public String name;
    public String scope;
    public int index;

    public Symbol(String name, String scope, int index) {
        this.name = name;
        this.scope = scope;
        this.index = index;
    }

    public String name() {
        return name;
    }

    public String scope() {
        return scope;
    }

    public int index() {
        return index;
    }
}
