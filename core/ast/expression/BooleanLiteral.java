package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class BooleanLiteral extends Expression{
    private Token token;
    private boolean value;

    public BooleanLiteral(Token t,boolean value) {
        this.value = value;
        token = t;
    }

    public boolean getValue() {
        return value;
    }

    @Override 
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.BooleanLiteral;
    }
}
