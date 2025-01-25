package core.token;

/**
 * Token 类用于表示单个 token 的类型和值
 * <p> 用法和示例 </p>
 * 1.生成一个表示整数的 token，值为 123，位于原文件的行1，列 1
 * <pre>
 *   Token token = new Token(TokenType.INT, "123", 1, 1);
 * </pre>
 * 2.生成一个表示字符串的 token，值为 "hello"
 * <pre>
 *   Token token = new Token(TokenType.STRING);
 */
public class Token {
    private TokenType type;
    private String value;

    private int line;
    private int column;

    /**
     * 构造函数
     * @param type token 类型
     * @param value token 值
     * @param line 行号
     * @param column 列号
     */
    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    /**
     * 获取 token 行号
     * @return token 行号
     */
    public int line() {
        return line;
    }

    /**
     * 获取 token 列号
     * @return token 列号
     */
    public int column() {
        return column;
    }

    /**
     * 获取 token 类型
     * @return TokenType
     */
    public TokenType type() {
        return type;
    }

    /**
     * 获取 token 值
     * @return token 值
     */
    public String value() {
        return value;
    }

    /**
     * 获取 token 字符串表示
     * @return token 字符串表示
     */
    public String toString() {
        return value;
    }

}
