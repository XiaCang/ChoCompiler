package core.vm;

import core.env.Obj;

public class VmStack {
    private int sp = 0;
    private Obj[] stack;
    private int maxSize;
    public VmStack(int size) {
        stack = new Obj[size];
        maxSize = size;
    }

    public Obj peek() {
        if (sp == 0) {
            System.out.println("stack underflow");
            return null;
        }
        return stack[sp - 1];
    }

    

    public void push(Obj obj) {
        if (sp == maxSize){
            System.out.println("stack overflow");
            return;
        }
        stack[sp] = obj;
        sp += 1;
    }

    public Obj pop() {
        if (sp == 0) {
            System.out.println("stack underflow");
            return null;
        }
        sp -= 1;
        return stack[sp];
    }

    public int size() {
        return sp;
    }

    public Obj get(int index) {
        if (index >= sp || index < 0) {
            System.out.println("invalid index");
            return null;
        }
        return stack[index];
    }

    public void set(int index, Obj obj) {
        if (index >= sp || index < 0) {
            System.out.println("invalid index");
            return;
        }
        stack[index] = obj;
    }

    public int sp() {
        return sp;
    }

    public void setSp(int sp) {
        if (sp < 0) {
            System.out.println("invalid sp");
            return;
        }
        this.sp = sp;
    }

    
}
