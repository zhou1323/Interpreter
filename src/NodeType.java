/**
 * Created by superman on 2017/10/24.
 */
public enum NodeType {
    NULL,       //空结点
    PROGRAM,    //程序起点
    STMT,       //语句
    STMT_BLOCK,//语句块
    VAR_DECL,   //变量声明
    TYPE,   //      类型
    ARRAY_TYPE,
    VARLIST,   //变量列表
    IF_STMT,   //if语句
    WHILE_STMT,   //while语句
    FOR_STMT, 	//for语句
    ADD_STMT,
    BREAK_STMT,   //break语句
    WRITE_STMT,   //write语句
    READ_STMT,   //read语句
    FUNCTION_STMT,
    FUN_VARLIST,
    CALL_STMT,
    ASSIGN_STMT,   //赋值语句
    VALUE,   // value语句
    ARI_EXPR,   //  算术表达式
    ARI_EXPR_PRIME,
    TERM,   //
    TERM_PRIME,   //
    FACTOR,   //
    LOG_EXPR,   //逻辑表达式
    LOG_OP,   //
    //算数运算符 + - * / % =
    PLUS, MINUS, MULTI, DIV, MOD, ASSIGN,
    //逻辑运算符 < <= > >= == !=
    LESS, LE_EQ, GREATER, GR_EQ, EQUAL, NOT_EQUAL,
    //特殊符号 { } [ ] ( ) ; ,
    L_BRACE, R_BRACE, L_BRACKET, R_BRACKET, L_PAREN, R_PAREN, SEMICOLON, COMMA,
    //保留字
    IF, ELSE, FOR,WHILE, READ, WRITE, INT, DOUBLE, BREAK, TRUE, FALSE,BOOL,FUNCTION,
    //变量  数字
    IDENT, INT_NUM, DOUBLE_NUM,
    //错误字符
    ERR_TOKEN;

}
