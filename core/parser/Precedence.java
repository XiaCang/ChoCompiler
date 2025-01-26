package core.parser;

/**
 * Precedence 用于表示运算符优先级
 * 
 */
public enum Precedence {
    /**
     * 最低优先级
     */
    LOWEST,

    /** 
     * 赋值
     */
    ASSIGN,

    /** 
     * == !=的优先级
     */
    EQUALS,

    /** 
     * < > <= >=的优先级
     */
    LESSGREATER,
    
    /**
     * + -的优先级
     */
    SUM,

    /**
     * * /的优先级
     */
    PRODUCT,

    /**
     * !的优先级
     */
    PREFIX,

    /**
     * ()的优先级
     */
    CALL,

    /**
     * []的优先级
     */
    INDEX
}
