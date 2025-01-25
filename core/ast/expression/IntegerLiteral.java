package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class IntegerLiteral extends Expression{
    private Token token;
    private int value;

    public IntegerLiteral(Token t, int value) {
        token = t;
        this.value = value;
    }

    public int getValue() {
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
        return ASTNodeType.IntegerLiteral;
    }

}
