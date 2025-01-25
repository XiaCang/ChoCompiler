package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class AssignExpression extends Expression {
    private Token token;
    private Expression left;
    private Expression right;
    public AssignExpression(Token t, Expression left) {
        token = t;
        this.left = left;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return left.toString() + " = " + right.toString();
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.AssignExpression;
    }
}
