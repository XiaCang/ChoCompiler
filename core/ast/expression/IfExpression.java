package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.ast.statement.Statement;
import core.token.Token;

public class IfExpression extends Expression {
    private Token token;
    private Expression condition;
    private Statement consequence;
    private Statement alternative;

    public IfExpression(Token t) {
        token = t;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public void setConsequence(Statement consequence) {
        this.consequence = consequence;
    }

    public void setAlternative(Statement alternative) {
        this.alternative = alternative;
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getConsequence() {
        return consequence;
    }

    public Statement getAlternative() {
        return alternative;
    }

    @Override
    public String toString() {
        String str = "if (" + condition.toString() + ") {" 
                            + consequence.toString() + "}";
        if (alternative != null) {
            str += " else {" 
                    + alternative.toString() 
                    + "}";
        }
        return str;
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.IfExpression;
    }

}
