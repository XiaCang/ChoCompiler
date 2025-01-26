package core.ast.expression;

import core.ast.base.ASTNodeType;
import core.token.Token;
import java.util.HashMap;
import java.util.Map;
public class HashLiteral extends Expression {
    private Token token;
    private HashMap<Expression, Expression> pairs;

    public HashLiteral(Token t) {
        token = t;
        pairs = new HashMap<>();
    }

    public void setPairs(HashMap<Expression, Expression> pairs) {
        this.pairs = pairs;
    }

    public HashMap<Expression, Expression> getPairs() {
        return pairs;
    }

    @Override
    public String toString() {
        String str = "{";
        for (Map.Entry<Expression, Expression> entry : pairs.entrySet()) {
            str += entry.getKey().toString() + ":" 
                    + entry.getValue().toString() + ",";
        }
        str = str.substring(0, str.length() - 1);
        str += "}";
        return str;
    }

    @Override
    public String value() {
        return token.value();
    }

    @Override
    public ASTNodeType type() {
        return ASTNodeType.HashLiteral;
    }
}
