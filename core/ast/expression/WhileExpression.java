package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.ast.statement.BlockStatement;
import core.ast.statement.Statement;
import core.token.Token;

public class WhileExpression extends Expression {
    private Token token;
    private Expression condition;
    private BlockStatement body;
    
    public WhileExpression(Token t) {
        token = t;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public void setBody(BlockStatement body) {
        this.body = body;
    }

    public Expression getCondition() {
        return condition;
    }

    public BlockStatement getBody() {
        return body;
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.WhileExpression;
    }
}
