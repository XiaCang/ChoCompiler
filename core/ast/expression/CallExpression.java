package core.ast.expression;

import java.util.ArrayList;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class CallExpression extends Expression {
    private Token token;
    private Expression function;
    private ArrayList<Expression> arguments;

    public CallExpression(Token t,Expression fn) {
        function = fn;
        token = t;
        arguments = new ArrayList<>();
    }

    public void setArguments(ArrayList<Expression> args) {
        arguments = args;
    }

    public Expression getFunction() {
        return function;
    }

    public ArrayList<Expression> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        String str = function.toString() + "(";
        for (int i = 0; i < arguments.size(); i++) {
            str += arguments.get(i).toString();
            if (i < arguments.size() - 1) {
                str += ", ";
            }
        }
        str += ")";
        return str;
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.CallExpression;
    }
}
