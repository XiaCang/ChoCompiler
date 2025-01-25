package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class IndexExpression extends Expression {

    private Token token;
    private Expression left;
    private Expression index;

    public IndexExpression(Token t) {
        token = t;
    }

    public IndexExpression(Token t, Expression left) {
        token = t;
        this.left = left;
    }

    public void setIndex(Expression index) {
        this.index = index;
    }

    public Expression getIndex() {
        return index;
    }

    public Expression getLeft() {
        return left;
    }
    
    @Override
    public String toString() {
        return left.toString() 
            + "[" + index.toString() + "]";
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.IndexExpression;
    }
}
