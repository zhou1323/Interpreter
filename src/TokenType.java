/**
 * Created by superman on 2017/10/10.
 */
//����token������
public enum TokenType {
    //��������� + - * / % =
    PLUS, MINUS, MULTI, DIV, MOD, ASSIGN,
    //�߼������ < <= > >= == !=
    LESS, LE_EQ, GREATER, GR_EQ, EQUAL, NOT_EQUAL,
    //������� { } [ ] ( ) ; ,
    L_BRACE, R_BRACE, L_BRACKET, R_BRACKET, L_PAREN, R_PAREN, SEMICOLON, COMMA,
    //���۷��� // /* */
    SINGLE_COMMENT, MUL_COMMENT,
    //������
    IF, ELSE, FOR,WHILE, READ, WRITE, INT, DOUBLE, BREAK, TRUE, FALSE, BOOL,FUNCTION,
    //���� �ĵ������� ����
    IDENT, END_OF_DOC, INT_NUM, DOUBLE_NUM,RETURN,
    //�����ַ�
    ERR_TOKEN
}
