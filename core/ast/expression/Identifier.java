package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class Identifier extends Expression {
    private Token token;
    private String value;

    public Identifier(Identifier identifier) {
        this.value = identifier.value;
    }

    public Identifier(Token token,String value) {
        this.value = value;
        this.token = token;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.Identifier;
    }
}
