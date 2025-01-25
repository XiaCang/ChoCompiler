package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class PrefixExpression extends Expression {
    private Token token;
    private Expression right;
    private String op;

    public PrefixExpression(Token t, String op) {
        token = t;
        this.op = op;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public String getOp() {
        return op;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public String toString() {
        return op + right.toString();
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.PrefixExpression;
    }
}
