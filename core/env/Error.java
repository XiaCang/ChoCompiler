package core.env;

public class Error extends Obj {
    private String message;
    public Error(String message) {
        this.message = message;
    }

    @Override
    public String inspect() {
        return "Error: " + message;
    }

    @Override
    public ObjType type() {
        return ObjType.ERROR;
    }
}
