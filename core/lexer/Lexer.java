package core.lexer;

import core.token.Token;
import core.token.TokenType;
import java.util.HashMap;

/**
 * Lexer 类用于对字符串进行词法分析，通过nextToken 方法获取下一个 token
 * <p> 用法和示例 </p>
 * 1.生成一个词法分析器
 * <pre>
 *   Lexer lexer = new Lexer("1 + 1");
 * </pre>
 * 2.获取下一个 token
 * <pre>
 *   Token token = lexer.nextToken();
 * </pre>
 */
public class Lexer {
    /**
     * 用于存储要分析的代码字符串
     */
    private String str;

    /**
     * 用于记录当前分析的位置
     */
    private int curpos;

    /**
     * 用于记录下一个分析的位置
     */
    private int nextpos;

    /** 
     * 用于记录当前分析的字符
    */
    private char ch;

    /**
     * 用于记录当前分析的行号
     */
    private int line;

    /**
     * 用于记录当前分析的列号
     */
    private int column;

    /**
     * 关键字表，用于判断是否为关键字，并返回对应的 TokenType
     */
    private static HashMap<String, TokenType> keywords = new HashMap<>();

    /**
     * 构造函数
     * @param str 要分析的代码字符串
     */
    public Lexer(String str) {
        this.str = str;
        this.curpos = 0;
        this.nextpos = 0;
        this.line = 1;
        this.column = 1;
        this.ch = 0;

        if (str.length() > 0) {
            this.ch = str.charAt(0);
            this.nextpos = 1;
        }

        initKeywords();
    }

    /**
     * 复制构造函数
     * @param lexer 词法分析器
     */
    public Lexer(Lexer lexer) {
        this.str = lexer.str;
        this.curpos = lexer.curpos; 
        this.nextpos = lexer.nextpos;
        this.line = lexer.line;
        this.column = lexer.column;
        this.ch = lexer.ch;

        initKeywords();
    }

    /**
     * 初始化关键字表
     */
    private void initKeywords() {
        keywords.put("function", TokenType.FUNCTION);
        keywords.put("var", TokenType.VAR);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("return", TokenType.RETURN);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
    }

    /**
     * 读取下一个字符，并更新ch，curpos，nextpos，column，line
     */
    private void readChar() {
        if (nextpos < str.length()) {
            ch = str.charAt(nextpos);
        } else {
            ch = 0;
        }

        curpos = nextpos;
        nextpos++;
        column++;
        if (ch == '\n') {
            line++;
            column = 1;
        }
    }

    /**
     * 获取下一个字符，但不更新相关变量
     * @return 下一个字符
     */
    private char peekChar() {
        if (nextpos >= str.length()) { 
            return 0;
        }

        return str.charAt(nextpos);
    }

    /**
     * 从str的当前位置curpos读取指定长度的字符串
     * @param len 要读取的长度
     * @return 读取的字符串
     */
    private String readString(int len) {
        String res = "";
        for (int i = 0; i < len; i++) {
            res += ch;
            readChar();
        }
        return res;
    }

    /**
     *  从str的当前位置curpos读取一个字符串常量
     * @return 读取的字符串
     */
    private String readString() {
        String res = "";
        readChar();
        while (ch != '\"' && ch != 0) {
            res += ch;
            readChar();
        }
        readChar();
        return res;
    }

    /**
     * 从str的当前位置curpos读取一个标识符
     * @return 读取的标识符
     */
    private String readIdentifier() {
        String res = "";
        while (isLetter() || isDigit()) {
            res += ch;
            readChar();
        }
        return res;
    }

    /**
     * 从str的当前位置curpos读取一个数字
     * @return 读取的数字
     */
    private String readNumber() {
        String res = "";
        while (isDigit()) {
            res += ch;
            readChar();
        }
        return res;
    }

    /**
     * 跳过注释
     */
    private void skipComment() {
        while (ch != '\n' && ch != 0) {
            readChar();
        }
    }
    
    /**
     * 跳过空白字符
     */
    private void skipWhitespace() {
        while (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
            readChar();
        }
    }

    /**
     * 判断当前字符ch是否是字母或者下划线
     * @return 是否是字母或者下划线
     */
    private boolean isLetter() {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }
    
    /**
     * 判断当前字符ch是否是数字
     * @return 是否是数字
     */
    private boolean isDigit() {
        return ch >= '0' && ch <= '9';
    }

    /**
     * 获取下一个 token
     * @return 下一个 token
     */
    public Token nextToken() {
        skipWhitespace();
        Token token = null;
        switch (ch) {
            case '+':
                token = new Token(TokenType.PLUS, readString(1), line, column);
                break;
            case '-':
                token = new Token(TokenType.MINUS, readString(1), line, column);
                break;
            case '*':
                token = new Token(TokenType.ASTERISK, readString(1), line, column);
                break;
            case '/':
                if (peekChar() == '/') {
                    skipComment();
                    token = nextToken();
                } else {
                    token = new Token(TokenType.SLASH, readString(1), line, column);
                }
                break;
            case '(':
                token = new Token(TokenType.LPAREN, readString(1), line, column);
                break;
            case ')':
                token = new Token(TokenType.RPAREN, readString(1), line, column);
                break;
            case '{':
                token = new Token(TokenType.LBRACE, readString(1), line, column);
                break;
            case '}':
                token = new Token(TokenType.RBRACE, readString(1), line, column);
                break;
            case '[':
                token = new Token(TokenType.LBRACKET, readString(1), line, column);
                break;
            case ']':
                token = new Token(TokenType.RBRACKET, readString(1), line, column);
                break;
            case ';':
                token = new Token(TokenType.SEMICOLON, readString(1), line, column);
                break;
            case ',':
                token = new Token(TokenType.COMMA, readString(1), line, column);
                break;
            case ':':
                token = new Token(TokenType.COLON, readString(1), line, column);
                break;
            case '=':
                if (peekChar() == '=') {
                    token = new Token(TokenType.EQ, readString(2), line, column);
                } else {
                    token = new Token(TokenType.ASSIGN, readString(1), line, column);
                }
                break;
            case '!':
                if (peekChar() == '=') {
                    token = new Token(TokenType.NOT_EQ, readString(2), line, column);
                } else {
                    token = new Token(TokenType.BANG, readString(1), line, column);
                }
                break;
            case '<':
                if (peekChar() == '=') {
                    token = new Token(TokenType.LE, readString(2), line, column);
                } else {
                    token = new Token(TokenType.LT, readString(1), line, column);
                }
                break;
            case '>':
                if (peekChar() == '=') {
                    token = new Token(TokenType.GE, readString(2), line, column);
                } else {
                    token = new Token(TokenType.GT, readString(1), line, column);
                }
                break;
            case '\"':
                token = new Token(TokenType.STRING, readString(), line, column);
                break;
            case 0:
                token = new Token(TokenType.EOF, "", line, column);
                break;
            default:
                if (isLetter()) {
                    String ident = readIdentifier();
                    if (keywords.containsKey(ident)) {
                        token = new Token(keywords.get(ident), ident, line, column);
                    } else {
                        token = new Token(TokenType.IDENT, ident, line, column);
                    }
                } else if (isDigit()) {
                    token = new Token(TokenType.INT, readNumber(), line, column);
                } else {
                    token = new Token(TokenType.ILLEGAL, readString(1), line, column);
                }
                break;
        }

        return token;
    }
}


