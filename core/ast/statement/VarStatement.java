package core.ast.statement;

import core.ast.base.ASTNodeType;
import core.ast.expression.Expression;
import core.ast.expression.Identifier;
import core.token.Token;

public class VarStatement extends Statement {
    private Token token;
    private Identifier identifier;
    private Expression value;

    public VarStatement(Token t) {
        token = t;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value() + " "
               + identifier.toString() + "="
               + value.toString() + ";";
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.VarStatement;
    }
}
