package core.ast.statement;

import java.util.ArrayList;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class BlockStatement extends Statement {
    private Token token;
    private ArrayList<Statement> statements;

    public BlockStatement(Token t) {
        token = t;
        statements = new ArrayList<>();
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
    }

    public ArrayList<Statement> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        String str = "{\n";
        for (int i = 0; i < statements.size(); i++) {
            str += "\t" + statements.get(i).toString() + "\n";
        }
        str += "}";
        return str;
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type(){
        return ASTNodeType.BlockStatement;
    }
}
