package core.vm;

import core.env.Closure;

public class Frame {
    
    private Closure closure;
    private int base;
    private int ip;

    public Frame(int base, int ip, Closure closure) {
        this.base = base;
        this.ip = ip;
        this.closure = closure;
    }

    public int basePtr() {
        return base;
    }

    public int ip() {
        return ip;
    }

    public Closure closure() {
        return closure;
    }

    public void inc(int in) {
        this.ip += in;
    }
}
