package core.ast.statement;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class ContinueStatement extends Statement {
    private Token token;

    public ContinueStatement(Token t) {
        token = t;
    }

    @Override
    public String toString() {
        return "continue;";
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.ContinueStatement;
    }
}
