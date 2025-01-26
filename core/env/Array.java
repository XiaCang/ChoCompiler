package core.env;

import java.util.ArrayList;
import java.util.List;

public class Array extends Obj {
    private ArrayList<Obj> elements;
    
    public Array() {
        elements = new ArrayList<>();
    }

    public Array(ArrayList<Obj> elems) {
        this.elements = elems;
    }

    public ArrayList<Obj> getElements() {
        return elements;
    }

    public Array inverse() {
        ArrayList<Obj> reversed = new ArrayList<>();
        for (int i = elements.size() - 1; i >= 0; i--) {
            reversed.add(elements.get(i));
        }
        return new Array(reversed);
    }

    public int length() {
        return elements.size();
    }

    public void set(int index, Obj value) {
        elements.set(index, value);
    }

    public void add(Obj value) {
        elements.add(value);
    }

    @Override
    public ObjType type() {
        return ObjType.ARRAY;
    }
    
    @Override
    public String toString() {
        String str = "[";
        for (int i = 0; i < elements.size(); i++) {
            str += elements.get(i).toString();
            if (i < elements.size() - 1) {
                str += ", ";
            }
        }
        str += "]";
        return str;
    }

    @Override
    public String inspect() {
        String str = "[";
        for (int i = 0; i < elements.size(); i++) {
            str += elements.get(i).inspect();
            if (i < elements.size() - 1) {
                str += ", ";
            }
        }
        str += "]";
        return str;
    }
}
