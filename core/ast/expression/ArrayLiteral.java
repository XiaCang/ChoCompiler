package core.ast.expression;

import java.util.ArrayList;

import core.ast.base.ASTNodeType;
import core.token.Token;

public class ArrayLiteral extends Expression{
    
    private Token token;
    private ArrayList<Expression> elements;

    public ArrayLiteral(Token t) {
        token = t;
    }

    public void setElements(ArrayList<Expression> elements) {
        this.elements = elements;
    }

    public ArrayList<Expression> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        String str = "[";
        for (int i = 0; i < elements.size(); i++) {
            str += elements.get(i).toString();
            if (i < elements.size() - 1) {
                str += ", ";
            }
        }
        str += "]";
        return str;
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.ArrayLiteral;
    }

}
