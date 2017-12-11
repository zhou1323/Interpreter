/**
 * Created by superman on 2017/10/10.
 */
//定义token的类型
public enum TokenType {
    //算数运算符 + - * / % =
    PLUS, MINUS, MULTI, DIV, MOD, ASSIGN,
    //逻辑运算符 < <= > >= == !=
    LESS, LE_EQ, GREATER, GR_EQ, EQUAL, NOT_EQUAL,
    //特殊符号 { } [ ] ( ) ; ,
    L_BRACE, R_BRACE, L_BRACKET, R_BRACKET, L_PAREN, R_PAREN, SEMICOLON, COMMA,
    //评论符号 // /* */
    SINGLE_COMMENT, MUL_COMMENT,
    //保留字
    IF, ELSE, FOR,WHILE, READ, WRITE, INT, DOUBLE, BREAK, TRUE, FALSE, BOOL,FUNCTION,
    //变量 文档结束符 数字
    IDENT, END_OF_DOC, INT_NUM, DOUBLE_NUM,RETURN,
    //错误字符
    ERR_TOKEN
}
