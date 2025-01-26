package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class InfixExpression extends Expression {

    private Token token;
    private Expression left;
    private Expression right;
    private String op;

    public InfixExpression(Token t, Expression left, String op) {
        token = t;
        this.left = left;
        this.op = op;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public String getOp() {
        return op;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public String toString() {
        return left.toString() + " " 
                + op + " " 
                + right.toString();
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.InfixExpression;
    }
}
