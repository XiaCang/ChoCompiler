package core.ast.statement;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class BreakStatement extends Statement {
    private Token token;

    public BreakStatement(Token t) {
        token = t;
    }

    @Override
    public String toString() {
        return "break;";
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.BreakStatement;
    }
}
