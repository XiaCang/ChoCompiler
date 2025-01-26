package core.token;


/**
 * Token 类型
 */
public enum TokenType {
    ILLEGAL, // 非法
    EOF,    // 文件结束


    IDENT,  // 标识符.
    INT,    // 整数
    STRING, // 字符串

    ASSIGN,   // "="
    PLUS,     // "+"
    MINUS,    // "-"
    BANG,     // "!"
    ASTERISK, // "*"
    SLASH,    // "/"

    LT, // "<"
    LE, // "<="
    GE, // ">="
    GT, // ">"

    EQ,     // "=="
    NOT_EQ, // "!="

    COMMA,     // ","
    SEMICOLON, // ";"
    COLON,     // ":"

    LPAREN,   // "("
    RPAREN,   // ")"
    LBRACE,   // "{"
    RBRACE,   // "}"
    LBRACKET, // "["
    RBRACKET, // "]"

    FUNCTION, // 函数
    VAR,      // 定义变量
    TRUE,    // "TRUE"
    FALSE,   // "FALSE"
    IF,       // "IF"
    WHILE,    // "WHILE"
    FOR,      // "FOR"
    BREAK,    // "BREAK"
    CONTINUE,    // "CONTINUE"
    ELSE,     // "ELSE"
    RETURN    // "RETURN"
}
