/**
 * Created by superman on 2017/10/24.
 */
public enum NodeType {
    NULL,       //�ս��
    PROGRAM,    //�������
    STMT,       //���
    STMT_BLOCK,//����
    VAR_DECL,   //��������
    TYPE,   //      ����
    ARRAY_TYPE,
    VARLIST,   //�����б�
    IF_STMT,   //if���
    WHILE_STMT,   //while���
    FOR_STMT, 	//for���
    ADD_STMT,
    BREAK_STMT,   //break���
    WRITE_STMT,   //write���
    READ_STMT,   //read���
    FUNCTION_STMT,
    FUN_VARLIST,
    CALL_STMT,
    ASSIGN_STMT,   //��ֵ���
    VALUE,   // value���
    ARI_EXPR,   //  �������ʽ
    ARI_EXPR_PRIME,
    TERM,   //
    TERM_PRIME,   //
    FACTOR,   //
    LOG_EXPR,   //�߼����ʽ
    LOG_OP,   //
    //��������� + - * / % =
    PLUS, MINUS, MULTI, DIV, MOD, ASSIGN,
    //�߼������ < <= > >= == !=
    LESS, LE_EQ, GREATER, GR_EQ, EQUAL, NOT_EQUAL,
    //������� { } [ ] ( ) ; ,
    L_BRACE, R_BRACE, L_BRACKET, R_BRACKET, L_PAREN, R_PAREN, SEMICOLON, COMMA,
    //������
    IF, ELSE, FOR,WHILE, READ, WRITE, INT, DOUBLE, BREAK, TRUE, FALSE,BOOL,FUNCTION,
    //����  ����
    IDENT, INT_NUM, DOUBLE_NUM,
    //�����ַ�
    ERR_TOKEN;

}
