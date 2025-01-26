package core.parser;

import core.ast.Program;
import core.ast.base.ASTNodeType;
import core.ast.expression.ArrayLiteral;
import core.ast.expression.AssignExpression;
import core.ast.expression.BooleanLiteral;
import core.ast.expression.CallExpression;
import core.ast.expression.Expression;
import core.ast.expression.ForExpression;
import core.ast.expression.FunctionLiteral;
import core.ast.expression.HashLiteral;
import core.ast.expression.Identifier;
import core.ast.expression.IfExpression;
import core.ast.expression.IndexExpression;
import core.ast.expression.InfixExpression;
import core.ast.expression.IntegerLiteral;
import core.ast.expression.PrefixExpression;
import core.ast.expression.StringLiteral;
import core.ast.expression.WhileExpression;
import core.ast.statement.BlockStatement;
import core.ast.statement.BreakStatement;
import core.ast.statement.ContinueStatement;
import core.ast.statement.ExpressionStatement;
import core.ast.statement.ReturnStatement;
import core.ast.statement.Statement;
import core.ast.statement.VarStatement;
import core.lexer.Lexer;
import core.token.Token;
import core.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;


public class Parser {
    private Lexer lexer;

    private ArrayList<String> errors = new ArrayList<>();

    private Token curToken;
    private Token peekToken;

    private static HashMap<TokenType, Precedence> tokenPrecedence = new HashMap<>();

    private HashMap<TokenType, Supplier<Expression>> prefixParseFns = new HashMap<>();
    private HashMap<TokenType, Function<Expression, Expression>> infixParseFns = new HashMap<>();

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        init();
    }

    private void init() {
        tokenPrecedence.put(TokenType.EQ, Precedence.EQUALS);
        tokenPrecedence.put(TokenType.NOT_EQ, Precedence.EQUALS);
        tokenPrecedence.put(TokenType.LE, Precedence.LESSGREATER);
        tokenPrecedence.put(TokenType.LT, Precedence.LESSGREATER);
        tokenPrecedence.put(TokenType.GE, Precedence.LESSGREATER);
        tokenPrecedence.put(TokenType.GT, Precedence.LESSGREATER);
        tokenPrecedence.put(TokenType.PLUS, Precedence.SUM);
        tokenPrecedence.put(TokenType.MINUS, Precedence.SUM);
        tokenPrecedence.put(TokenType.SLASH, Precedence.PRODUCT);
        tokenPrecedence.put(TokenType.ASTERISK, Precedence.PRODUCT);
        tokenPrecedence.put(TokenType.LPAREN, Precedence.CALL);
        tokenPrecedence.put(TokenType.LBRACKET, Precedence.INDEX);
        tokenPrecedence.put(TokenType.ASSIGN, Precedence.ASSIGN);

        RegisterPrefix(TokenType.IDENT, this::parseIdentifier);
        RegisterPrefix(TokenType.INT, this::parseIntegerLiteral);
        RegisterPrefix(TokenType.BANG, this::parsePrefixExpression);
        RegisterPrefix(TokenType.MINUS, this::parsePrefixExpression);
        RegisterPrefix(TokenType.TRUE, this::parseBooleanLiteral);
        RegisterPrefix(TokenType.FALSE, this::parseBooleanLiteral);
        RegisterPrefix(TokenType.LPAREN, this::parseGroupedExpression);
        RegisterPrefix(TokenType.IF, this::parseIfExpression);
        RegisterPrefix(TokenType.WHILE, this::parseWhileExpression);
        RegisterPrefix(TokenType.FOR, this::parseForExpression);

        RegisterPrefix(TokenType.FUNCTION, this::parseFunctionLiteral);
        RegisterPrefix(TokenType.STRING, this::parseStringLiteral);
        RegisterPrefix(TokenType.LBRACKET, this::parseArrayLiteral);
        RegisterPrefix(TokenType.LBRACE, this::parseHashLiteral);

        RegisterInfix(TokenType.EQ, this::parseInfixExpression);
        RegisterInfix(TokenType.LE, this::parseInfixExpression);
        RegisterInfix(TokenType.GE, this::parseInfixExpression);
        RegisterInfix(TokenType.LT, this::parseInfixExpression);
        RegisterInfix(TokenType.GT, this::parseInfixExpression);
        RegisterInfix(TokenType.NOT_EQ, this::parseInfixExpression);
        RegisterInfix(TokenType.PLUS, this::parseInfixExpression);
        RegisterInfix(TokenType.MINUS, this::parseInfixExpression);
        RegisterInfix(TokenType.SLASH, this::parseInfixExpression);
        RegisterInfix(TokenType.ASTERISK, this::parseInfixExpression);
        RegisterInfix(TokenType.LPAREN, this::parseCallExpression);
        RegisterInfix(TokenType.LBRACKET, this::parseIndexExpression);
        RegisterInfix(TokenType.ASSIGN, this::parseAssignExpression);

        nextToken();
        nextToken();
    }

    private void RegisterPrefix(TokenType type, Supplier<Expression> fn) {
        prefixParseFns.put(type, fn);
    }

    private void RegisterInfix(TokenType type, Function<Expression, Expression> fn) {
        infixParseFns.put(type, fn);
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    private void addError(String msg) {
        errors.add(msg);
    }

    private void nextToken() {
        curToken = peekToken;
        peekToken = lexer.nextToken();
    }

    private boolean curTokenIs(TokenType t) {
        return curToken.type() == t;
    }

    private boolean peekTokenIs(TokenType t) {
        return peekToken.type() == t;
    }

    private boolean expectPeek(TokenType t) {
        if (peekTokenIs(t)) {
            nextToken();
            return true;
        }
        return false;
    }

    private Precedence peekPrecedence() {
        if (tokenPrecedence.containsKey(peekToken.type())) {
            return tokenPrecedence.get(peekToken.type());
        }
        return Precedence.LOWEST;
    }

    private Precedence curPrecedence() {
        if (tokenPrecedence.containsKey(curToken.type())) {
            return tokenPrecedence.get(curToken.type());
        }
        return Precedence.LOWEST;
    }

    public Program parseProgram() {
        Program program = new Program();

        while (!curTokenIs(TokenType.EOF)) {
            Statement statement = parseStatement();
            if (statement != null) {
                program.addStatement(statement);
            }
            nextToken();
        }
        return program;
    }

    private Statement parseStatement() {
        switch (curToken.type()) {
            case VAR:
                return parseVarStatement();
            case RETURN:
                return parseReturnStatement();
            case BREAK:
                return parseBreakStatement();
            case CONTINUE:
                return parseContinueStatement();
            default:
                return parseExpressionStatement();
        }
    }

    private BreakStatement parseBreakStatement() {
        BreakStatement breakStatement = new BreakStatement(curToken);
        nextToken();
        return breakStatement;
    }

    private ContinueStatement parseContinueStatement() {
        ContinueStatement continueStatement = new ContinueStatement(curToken);
        nextToken();
        return continueStatement;
    }

    private VarStatement parseVarStatement() {
        VarStatement varStatement = new VarStatement(curToken);

        if (!expectPeek(TokenType.IDENT)) {
            addError("expected identifier but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        varStatement.setIdentifier(
                new Identifier(curToken, curToken.value()));

        if (peekTokenIs(TokenType.SEMICOLON)) {
            return varStatement;
        }

        if (!expectPeek(TokenType.ASSIGN)) {
            addError("expected \"=\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        nextToken();

        Expression expr = parseExpression(Precedence.LOWEST);

        if (expr == null) {
            addError("expected expression but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        if (expr.type() == ASTNodeType.FunctionLiteral) {
            ((FunctionLiteral) expr).setName(varStatement.getIdentifier().value());
        }

        varStatement.setValue(expr);

        if (!expectPeek(TokenType.SEMICOLON)) {
            addError("expected \";\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        return varStatement;
    }

    private ReturnStatement parseReturnStatement() {
        ReturnStatement returnStatement = new ReturnStatement(curToken);

        nextToken();

        Expression expr = parseExpression(Precedence.LOWEST);

        if (expr == null) {
            addError("expected expression but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        returnStatement.setRet(expr);

        if (!expectPeek(TokenType.SEMICOLON)) {
            addError("expected \";\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        return returnStatement;
    }

    private ExpressionStatement parseExpressionStatement() {
        ExpressionStatement es = new ExpressionStatement(curToken);

        Expression expr = parseExpression(Precedence.LOWEST);

        if (expr == null) {
            addError("expected expression but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        es.setExpression(expr);

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return es;
    }

    private Expression parseExpression(Precedence precedence) {
        if (!prefixParseFns.containsKey(curToken.type())) {
            addError("expected expression but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        Supplier<Expression> prefixFn = prefixParseFns.get(curToken.type());

        Expression left = prefixFn.get();

        while (!curTokenIs(TokenType.SEMICOLON) && precedence.compareTo(peekPrecedence()) < 0) {
            if (!infixParseFns.containsKey(peekToken.type())) {
                addError("expected expression but got " + peekToken.value()
                        + " in line " + peekToken.line() + " column " + peekToken.column());
                return left;
            }
            Function<Expression, Expression> infixFn = infixParseFns.get(peekToken.type());
            nextToken();
            Expression right = infixFn.apply(left);
            left = right;
        }
        return left;
    }

    private Expression parseInfixExpression(Expression left) {
        InfixExpression expr = new InfixExpression(curToken, left, curToken.value());

        Precedence precedence = curPrecedence();
        nextToken();

        expr.setRight(parseExpression(precedence));

        return expr;
    }

    private Expression parsePrefixExpression() {
        PrefixExpression expr = new PrefixExpression(curToken, curToken.value());
        nextToken();

        expr.setRight(parseExpression(Precedence.PREFIX));

        return expr;
    }

    private Expression parseIdentifier() {
        return new Identifier(curToken, curToken.value());
    }

    private Expression parseIntegerLiteral() {
        return new IntegerLiteral(curToken, Integer.parseInt(curToken.value()));
    }

    private Expression parseBooleanLiteral() {
        return new BooleanLiteral(curToken, curTokenIs(TokenType.TRUE));
    }

    private Expression parseStringLiteral() {
        return new StringLiteral(curToken, curToken.value());
    }

    private Expression parseArrayLiteral() {
        ArrayLiteral expr = new ArrayLiteral(curToken);
        expr.setElements(parseExpressionList(TokenType.RBRACKET));
        return expr;
    }

    private Expression parseGroupedExpression() {
        nextToken();

        Expression expr = parseExpression(Precedence.LOWEST);

        if (!expectPeek(TokenType.RPAREN)) {
            addError("expected \")\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        return expr;
    }

    private Expression parseWhileExpression() {
        WhileExpression expr = new WhileExpression(curToken);

        if (!expectPeek(TokenType.LPAREN)) {
            addError("expected \"(\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        nextToken();

        expr.setCondition(parseExpression(Precedence.LOWEST));

        if (!expectPeek(TokenType.RPAREN)) {
            addError("expected \")\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        if (expectPeek(TokenType.LBRACE)) {
            expr.setBody(parseBlockStatement());
        } else {
            nextToken();
            BlockStatement block = new BlockStatement(curToken);
            block.addStatement(parseStatement());
            expr.setBody(block);
        }

        return expr;
    }

    private Expression parseForExpression() {
        ForExpression expr = new ForExpression(curToken);

        if (!expectPeek(TokenType.LPAREN)) {
            addError("expected \"(\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }
        nextToken();

        expr.setInit(parseStatement());

        if (!curTokenIs(TokenType.SEMICOLON)) {
            addError("expected \";\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }
        nextToken();

        expr.setCondition(parseExpression(Precedence.LOWEST));

        if (!expectPeek(TokenType.SEMICOLON)) {
            addError("expected \";\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }
        nextToken();

        expr.setIncrement(parseStatement());

        if (!expectPeek(TokenType.RPAREN)) {
            addError("expected \")\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        if (expectPeek(TokenType.LBRACE)) {
            expr.setBody(parseBlockStatement());
        } else {
            nextToken();
            BlockStatement block = new BlockStatement(curToken);
            block.addStatement(parseStatement());
            expr.setBody(block);
        }

        return expr;
    }

    private Expression parseIfExpression() {
        IfExpression expr = new IfExpression(curToken);

        if (!expectPeek(TokenType.LPAREN)) {
            addError("expected \"(\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        nextToken();

        expr.setCondition(parseExpression(Precedence.LOWEST));

        if (!expectPeek(TokenType.RPAREN)) {
            addError("expected \")\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        if (expectPeek(TokenType.LBRACE)) {
            expr.setConsequence(parseBlockStatement());
        } else {
            nextToken();
            expr.setConsequence(parseStatement());

        }

        if (peekTokenIs(TokenType.ELSE)) {
            nextToken();

            if (expectPeek(TokenType.LBRACE)) {
                expr.setAlternative(parseBlockStatement());
            } else {
                nextToken();
                expr.setAlternative(parseStatement());
            }
        }

        return expr;
    }

    private BlockStatement parseBlockStatement() {
        BlockStatement block = new BlockStatement(curToken);

        nextToken();

        while (!curTokenIs(TokenType.RBRACE) && !curTokenIs(TokenType.EOF)) {
            Statement statement = parseStatement();
            if (statement != null) {
                block.addStatement(statement);
            }
            nextToken();
        }

        return block;
    }

    private Expression parseFunctionLiteral() {
        FunctionLiteral function = new FunctionLiteral(curToken);

        if (!expectPeek(TokenType.LPAREN)) {
            addError("expected \"(\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        function.setParameters(parseFunctionParameters());

        if (!expectPeek(TokenType.LBRACE)) {
            addError("expected \"{\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        function.setBody(parseBlockStatement());

        return function;
    }

    private ArrayList<Identifier> parseFunctionParameters() {
        ArrayList<Identifier> parameters = new ArrayList<>();

        if (peekTokenIs(TokenType.RPAREN)) {
            nextToken();
            return parameters;
        }

        nextToken();

        Identifier ident = new Identifier(curToken, curToken.value());
        parameters.add(ident);

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken();
            nextToken();
            Identifier parameter = new Identifier(curToken, curToken.value());
            parameters.add(parameter);
        }

        if (!expectPeek(TokenType.RPAREN)) {
            addError("expected \")\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        return parameters;
    }

    private Expression parseCallExpression(Expression function) {
        CallExpression expr = new CallExpression(curToken, function);

        expr.setArguments(parseExpressionList(TokenType.RPAREN));

        return expr;
    }

    private ArrayList<Expression> parseExpressionList(TokenType end) {
        ArrayList<Expression> args = new ArrayList<>();

        if (peekTokenIs(end)) {
            nextToken();
            return args;
        }

        nextToken();
        Expression arg = parseExpression(Precedence.LOWEST);
        args.add(arg);

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken();
            nextToken();
            arg = parseExpression(Precedence.LOWEST);
            args.add(arg);
        }

        if (!expectPeek(end)) {
            addError("expected " + end + " but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        return args;
    }

    private Expression parseIndexExpression(Expression left) {
        IndexExpression expr = new IndexExpression(curToken, left);
        nextToken();
        expr.setIndex(parseExpression(Precedence.LOWEST));

        if (!expectPeek(TokenType.RBRACKET)) {
            addError("expected \"]\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }
        return expr;
    }

    private Expression parseHashLiteral() {
        HashLiteral hash = new HashLiteral(curToken);
        HashMap<Expression, Expression> pairs = new HashMap<>();

        while (!peekTokenIs(TokenType.RBRACE) && !peekTokenIs(TokenType.EOF)) {
            nextToken();

            Expression key = parseExpression(Precedence.LOWEST);

            if (!expectPeek(TokenType.COLON)) {
                addError("expected \":\" but got " + peekToken.value()
                        + " in line " + peekToken.line() + " column " + peekToken.column());
                return null;
            }

            nextToken();

            Expression value = parseExpression(Precedence.LOWEST);

            pairs.put(key, value);

            if (!peekTokenIs(TokenType.RBRACE) & !expectPeek(TokenType.COMMA)) {
                addError("expected \",\" or \"}\" but got " + peekToken.value()
                        + " in line " + peekToken.line() + " column " + peekToken.column());
                return null;
            }
        }

        if (!expectPeek(TokenType.RBRACE)) {
            addError("expected \"}\" but got " + peekToken.value()
                    + " in line " + peekToken.line() + " column " + peekToken.column());
            return null;
        }

        hash.setPairs(pairs);
        return hash;
    }

    private Expression parseAssignExpression(Expression left) {
        AssignExpression expr = new AssignExpression(curToken, left);
        nextToken();
        expr.setRight(parseExpression(Precedence.LOWEST));
        return expr;
    }

}
