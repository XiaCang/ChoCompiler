package core.ast.statement;

import core.ast.base.ASTNodeType;
import core.ast.expression.Expression;
import core.token.Token;

public class ExpressionStatement extends Statement {
    private Token token;
    private Expression expression;

    public ExpressionStatement(Token t) {
        token = t;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return expression.toString();
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.ExpressionStatement;
    }

}
