package core.ast;

import core.ast.base.ASTNode;
import core.ast.base.ASTNodeType;
import core.ast.statement.Statement;

import java.util.ArrayList;

public class Program extends ASTNode {
    private ArrayList<Statement> statements;

    public Program() {
        statements = new ArrayList<>();
    }

    public Program(ArrayList<Statement> statements) {
        this.statements = statements;
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
    }

    public ArrayList<Statement> getStatements() {
        return statements;
    }

    @Override 
    public String toString() {
        String str = "";
        for (int i = 0; i < statements.size(); i++) {
            str += statements.get(i).toString() + "\n";
        }
        return str;
    }

    @Override
    public String value() {
        if(statements.size() > 0) {
            return statements.get(0).value();
        }
        return "";
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.Program;
    }

}
