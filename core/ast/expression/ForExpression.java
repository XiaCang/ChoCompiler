package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.ast.statement.BlockStatement;
import core.ast.statement.Statement;
import core.token.Token;

public class ForExpression extends Expression {
    private Token token;
    private Statement init;
    private Expression condition;
    private Statement increment;
    private BlockStatement body;
    
    public ForExpression(Token t) {
        this.token = t;
    }

    public void setInit(Statement init) {
        this.init = init;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public void setIncrement(Statement increment) {
        this.increment = increment;
    }

    public void setBody(BlockStatement body) {
        this.body = body;
    }

    public Statement getInit() {
        return init;
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getIncrement() {
        return increment;
    }

    public BlockStatement getBody() {
        return body;
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.ForExpression;
    }

    @Override    
    public String toString()  {
        String str = "for (" + init.toString() + "; " + condition.toString() + "; " 
            + increment.toString() + ") " + body.toString();
        return str;
    }

    @Override
    public String value() {
        return "for";
    }
}
