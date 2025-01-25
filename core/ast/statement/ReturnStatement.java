package core.ast.statement;

import core.ast.base.ASTNodeType;
import core.ast.expression.Expression;
import core.token.Token;

public class ReturnStatement extends Statement {
    private Token token;
    private Expression ret;

    public ReturnStatement(Token t) {
        token = t;
    }

    public void setRet(Expression ret) {
        this.ret = ret;
    }

    public Expression getRet() {
        return ret;
    }

    @Override
    public String toString() {
        return value() +" " + ret.toString() + ";";
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.ReturnStatement;
    }
}


