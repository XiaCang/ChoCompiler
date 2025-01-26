package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.ast.statement.BlockStatement;
import core.token.Token;

import java.util.ArrayList;

public class FunctionLiteral extends Expression {
    private Token token;
    private ArrayList<Identifier> parameters;
    private BlockStatement body;
    private String name;

    public FunctionLiteral(Token t) {
        token = t;
    }

    public void setParameters(ArrayList<Identifier> parameters) {
        this.parameters = parameters;
    }

    public void setBody(BlockStatement body) {
        this.body = body;
    }

    public ArrayList<Identifier> getParameters() {
        return parameters;
    }

    public BlockStatement getBody() {
        return body;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        String str = "";
        str += value() + "(";
        for (int i = 0; i < parameters.size(); i++) {
            str += parameters.get(i).toString();
            if (i < parameters.size() - 1) {
                str += ", ";
            }
        }
        str += ") ";
        str += body.toString();
        return str;
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.FunctionLiteral;
    }
}
