package core.ast.statement;

import core.ast.base.ASTNode;
import core.ast.base.ASTNodeType;

public class Statement extends ASTNode {
    @Override
    public ASTNodeType type() {
        return ASTNodeType.Statement;
    }

    @Override
    public String value() {
        return "";
    }
}
