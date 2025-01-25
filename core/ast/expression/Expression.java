package core.ast.expression;

import core.ast.base.ASTNode;
import core.ast.base.ASTNodeType;

public class Expression extends ASTNode {
    @Override
    public ASTNodeType type() {
        return ASTNodeType.Expression;
    }

    @Override
    public String value() {
        return "";
    }
}
