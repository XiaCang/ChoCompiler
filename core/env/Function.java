package core.env;

import java.util.ArrayList;

import core.ast.expression.Identifier;
import core.ast.statement.BlockStatement;

public class Function extends Obj {
    private Environment env;
    private BlockStatement body;
    private ArrayList<Identifier> params;

    public Function(Environment env, BlockStatement body, ArrayList<Identifier> params) {
        this.env = env;
        this.body = body;
        this.params = params;
    }

    public Environment getEnv() {
        return env;
    }

    public BlockStatement getBody() {
        return body;
    }

    public ArrayList<Identifier> getParams() {
        return params;
    }

    @Override
    public ObjType type() {
        return ObjType.FUNCTION;
    }

    @Override
    public String inspect() {
        return "<function>";
    }
}
