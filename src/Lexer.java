import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by superman on 2017/10/10.
 */
public class Lexer{
    private CharReader charReader;
    public Lexer(String path) throws FileNotFoundException {
        this.charReader = new CharReader(path);
    }
    //获取下一个token
    public Token nextToken() throws IOException{
        char tempChar = charReader.peekChar();
        //忽略空白符
        while(isWhiteSpace(tempChar)){
            charReader.nextChar();
            tempChar = charReader.peekChar();
        }
        if(tempChar==charReader.EOF){
            charReader.nextChar();
            charReader.charReaderClose();
            return new Token(TokenType.END_OF_DOC,"EOF",charReader.getLineNum(),charReader.getPosition());
        }
        switch (tempChar){
            case '(':
                charReader.nextChar();
                return new Token(TokenType.L_PAREN,"(",charReader.getLineNum(),charReader.getPosition());
            case ')':
                charReader.nextChar();
                return new Token(TokenType.R_PAREN,")",charReader.getLineNum(),charReader.getPosition());
            case '[':
                charReader.nextChar();
                return new Token(TokenType.L_BRACKET,"[",charReader.getLineNum(),charReader.getPosition());
            case ']':
                charReader.nextChar();
                return new Token(TokenType.R_BRACKET,"]",charReader.getLineNum(),charReader.getPosition());
            case '{':
                charReader.nextChar();
                return new Token(TokenType.L_BRACE,"{",charReader.getLineNum(),charReader.getPosition());
            case '}':
                charReader.nextChar();
                return new Token(TokenType.R_BRACE,"}",charReader.getLineNum(),charReader.getPosition());
            case ';':
                charReader.nextChar();
                return new Token(TokenType.SEMICOLON,";",charReader.getLineNum(),charReader.getPosition());
            case ',':
                charReader.nextChar();
                return new Token(TokenType.COMMA,",",charReader.getLineNum(),charReader.getPosition());
            case '+':
                charReader.nextChar();
                return new Token(TokenType.PLUS,"+",charReader.getLineNum(),charReader.getPosition());
            case '-':
                charReader.nextChar();
                return new Token(TokenType.MINUS,"-",charReader.getLineNum(),charReader.getPosition());
            case '*':
                charReader.nextChar();
                return new Token(TokenType.MULTI,"*",charReader.getLineNum(),charReader.getPosition());
            case '%':
                charReader.nextChar();
                return new Token(TokenType.MOD,"%",charReader.getLineNum(),charReader.getPosition());
            case '=':
                charReader.nextChar();
                if(charReader.peekChar()=='='){
                    charReader.nextChar();
                    return new Token(TokenType.EQUAL,"==",charReader.getLineNum(),charReader.getPosition());
                }else{
                    return new Token(TokenType.ASSIGN,"=",charReader.getLineNum(),charReader.getPosition());
                }
            case '/':
                charReader.nextChar();
                char ch = charReader.peekChar();
                switch (ch){
                   case '/':
                        charReader.nextChar();
                        String valueOfComment = "";
                       while(charReader.peekChar()!='\n'){
                           valueOfComment += charReader.nextChar();
                       }
                        return new Token(TokenType.SINGLE_COMMENT,"//"+valueOfComment,charReader.getLineNum(),charReader.getPosition()-valueOfComment.length()-1);
                    case '*':
                        charReader.nextChar();
                        int comLineNo = charReader.getLineNum();
                        int composition = charReader.getPosition();
                        String comment = "";
                        while(true){
                            if(charReader.peekChar()=='*'){
                                comment+=charReader.nextChar();
                                if(charReader.peekChar() == '/'){
                                    comment+=charReader.nextChar();
                                    return new Token(TokenType.MUL_COMMENT,"/*"+comment,comLineNo,composition-1);
                                }else{
                                    comment += charReader.nextChar();
                                }
                            }else if(charReader.peekChar()==charReader.EOF){
                                break;
                            }else{
                                comment += charReader.nextChar();
                            }
                        }
                    default:
                        return new Token(TokenType.DIV,"/",charReader.getLineNum(),charReader.getPosition());
                }
            case '!':
                charReader.nextChar();
                if(charReader.peekChar()!='='){
                    System.err.println("Line:"+ charReader.getLineNum()+",Position:"+charReader.getPosition()+",Invalid Character");
                    String s ="!";
                    while(!isWhiteSpace(charReader.peekChar())&&!isOp(charReader.peekChar())&&!isSpecialSymbol(charReader.peekChar())){
                        s+=charReader.nextChar();
                    }
                    return new Token(TokenType.ERR_TOKEN,s,charReader.getLineNum(),charReader.getPosition()-s.length()+1);
                }else{
                    charReader.nextChar();
                    return new Token(TokenType.NOT_EQUAL,"!=",charReader.getLineNum(),charReader.getPosition());
                }
            case '<':
                charReader.nextChar();
                if(charReader.peekChar()=='='){
                    charReader.nextChar();
                    return new Token(TokenType.LE_EQ,"<=",charReader.getLineNum(),charReader.getPosition());
                }else{
                    return new Token(TokenType.LESS,"<",charReader.getLineNum(),charReader.getPosition());
                }
            case '>':
                charReader.nextChar();
                if(charReader.peekChar()=='='){
                    charReader.nextChar();
                    return new Token(TokenType.GR_EQ,">=",charReader.getLineNum(),charReader.getPosition());
                }else{
                    return new Token(TokenType.GREATER,">",charReader.getLineNum(),charReader.getPosition());
                }
            default:
                if(Character.isLetter(tempChar)){
                    String value = ""+ charReader.nextChar();
                    while(Character.isLetter(charReader.peekChar())||Character.isDigit(charReader.peekChar())||charReader.peekChar()=='_'){
                        value += charReader.nextChar();
                    }
                    switch (value){
                        case "if":
                            return new Token(TokenType.IF,"if",charReader.getLineNum(),charReader.getPosition()-1);
                        case "else":
                            return new Token(TokenType.ELSE,"else",charReader.getLineNum(),charReader.getPosition()-3);
                        case "for":
                            return new Token(TokenType.FOR,"for",charReader.getLineNum(),charReader.getPosition()-2);
                        case "while":
                            return new Token(TokenType.WHILE,"while",charReader.getLineNum(),charReader.getPosition()-4);
                        case "break":
                            return new Token(TokenType.BREAK,"break",charReader.getLineNum(),charReader.getPosition()-4);
                        case "read":
                            return new Token(TokenType.READ,"read",charReader.getLineNum(),charReader.getPosition()-3);
                        case "write":
                            return new Token(TokenType.WRITE,"write",charReader.getLineNum(),charReader.getPosition()-4);
                        case "int":
                            return new Token(TokenType.INT,"int",charReader.getLineNum(),charReader.getPosition()-2);
                        case "double":
                            return new Token(TokenType.DOUBLE,"double",charReader.getLineNum(),charReader.getPosition()-5);
                        case "true":
                            return new Token(TokenType.TRUE,"true",charReader.getLineNum(),charReader.getPosition()-3);
                        case "false":
                            return new Token(TokenType.FALSE,"false",charReader.getLineNum(),charReader.getPosition()-4);
                        case "bool":
                            return new Token(TokenType.BOOL,"bool",charReader.getLineNum(),charReader.getPosition()-3);
                        case "function":
                        	return new Token(TokenType.FUNCTION,"function",charReader.getLineNum(),charReader.getPosition()-7);
                        default:
                            return new Token(TokenType.IDENT, value,charReader.getLineNum(),charReader.getPosition()-value.length()+1);
                    }

                }else if(Character.isDigit(charReader.peekChar())){
                    if(charReader.peekChar()=='0'){
                        String s = ""+ charReader.nextChar();
                        if(charReader.peekChar()=='x'||charReader.peekChar()=='X'){
                            s += charReader.nextChar();
                            for(int i = 1; i<=4; i++){
                                if(isHex(charReader.peekChar())){
                                    s += charReader.nextChar();
                                }else {
                                    System.err.println("Line:"+ charReader.getLineNum()+",Position:"+charReader.getPosition()+",Not a Hex Character");
                                    while(!isWhiteSpace(charReader.peekChar())&&!isOp(charReader.peekChar())&&!isSpecialSymbol(charReader.peekChar()))
                                        s+=charReader.nextChar();
                                    return new Token(TokenType.ERR_TOKEN,s,charReader.getLineNum(),charReader.getPosition()-s.length()+1);
                                }
                            }
                            return new Token(TokenType.INT_NUM,s,charReader.getLineNum(),charReader.getPosition()-s.length()+1);
                        }else{
                            while(Character.isDigit(charReader.peekChar())||charReader.peekChar()=='.'){
                                s += charReader.nextChar();
                            }
                            if(s.matches("[0-9]+")){
                                return new Token(TokenType.INT_NUM,s,charReader.getLineNum(),charReader.getPosition()-s.length()+1);
                            }else if(s.matches("[0-9]+.[0-9]+")){
                                return new Token(TokenType.DOUBLE_NUM,s,charReader.getLineNum(),charReader.getPosition()-s.length()+1);
                            }else{
                                System.err.println("Line:"+charReader.getLineNum()+", Position:"+ (charReader.getPosition()-s.length()+s.lastIndexOf(".")+", Invalid Number Format"));
                                while(!isWhiteSpace(charReader.peekChar())&&!isOp(charReader.peekChar())&&!isSpecialSymbol(charReader.peekChar())){
                                    s+=charReader.nextChar();
                                }
                                return new Token(TokenType.ERR_TOKEN,s,charReader.getLineNum(),charReader.getPosition()-s.length()+1);
                            }
                        }
                    }else{
                        String value = ""+ charReader.nextChar();
                        while(Character.isDigit(charReader.peekChar())||charReader.peekChar()=='.'){
                            value += charReader.nextChar();
                        }
                        if(value.matches("[0-9]+")){
                            return new Token(TokenType.INT_NUM,value,charReader.getLineNum(),charReader.getPosition()-value.length()+1);
                        }else if(value.matches("[0-9]+.[0-9]+")){
                            return new Token(TokenType.DOUBLE_NUM,value,charReader.getLineNum(),charReader.getPosition()-value.length()+1);
                        }else{
                            System.err.println("Line:"+charReader.getLineNum()+", Position:"+ (charReader.getPosition()-value.length()+value.lastIndexOf(".")+", Invalid Number Format"));
                            while(!isWhiteSpace(charReader.peekChar())&&!isOp(charReader.peekChar())&&!isSpecialSymbol(charReader.peekChar())){
                                value+=charReader.nextChar();
                            }
                            return new Token(TokenType.ERR_TOKEN,value,charReader.getLineNum(),charReader.getPosition()-value.length()+1);
                        }
                    }

                }else{
                    String s = ""+charReader.nextChar();
                    System.err.println("Line:"+ charReader.getLineNum()+",Position:"+(charReader.getPosition()+1)+",Invalid Character");
                    while(true){
                        if(!isWhiteSpace(charReader.peekChar())&&!isOp(charReader.peekChar())&&!isSpecialSymbol(charReader.peekChar())){
                            s += charReader.nextChar();
                        }else{
                            break;
                        }
                    }
                    return new Token(TokenType.ERR_TOKEN,s,charReader.getLineNum(),charReader.getPosition()-s.length()+1);
                }
        }

    }
    //判断是否是空白符
    private boolean isWhiteSpace(char c){
        return(c == ' '||c == '\t'||c == '\n');
    }

    //是否是十六进制数
    private boolean isHex(char c){
        if(c=='0'||c=='1'||c=='2'||c=='3'||c=='4'||c=='5'||c=='6'||c=='7'||c=='8'||
                c=='9'||c=='a'||c=='b'||c=='c'||c=='d'||c=='e'||c=='f')
            return true;
        return false;
    }
    private boolean isSpecialSymbol(char c){
        return (c=='{'||c=='}'||c=='['||c==']'||c=='('||c==')'||c==','||c==';');
    }

    private boolean isOp(char c){
        return (c=='+'||c=='-'||c=='*'||c=='/'||c=='%'||c=='<'||c=='>'||c=='='||c=='!');
    }


}
