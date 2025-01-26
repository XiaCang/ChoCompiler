package core.env.builtin;

import core.env.Obj;
import java.util.ArrayList;

@FunctionalInterface
public interface AnyFunc {
    Obj apply(ArrayList<Obj> args);
}
