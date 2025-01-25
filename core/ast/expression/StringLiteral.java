package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class StringLiteral extends Expression {
    
    private Token token;
    private String value;
    
    public StringLiteral(Token t, String value) {
        token = t;
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    @Override
    public String value() {
        return token.value();
    }

    @Override
    public String toString() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.StringLiteral;
    }
}
