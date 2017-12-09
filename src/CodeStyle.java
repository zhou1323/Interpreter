

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by superman on 2017/12/7.
 */
public class CodeStyle {
    private ColorType colorType;
    private String path;
    private Lexer lexer;
    private List<Token> tokenList;
    private String []keyWords = {"int","bool","double","read","if","esle","write","true","false","break","function","for","while"};
    private static final String DELIM = "[] (); {}, \n \t";
    private static final String FONT_FAMILT = "Courier New";
    private static final int FONT_SIZE = 14;
    //鐧戒富棰�
    private static final Color WHITE_COMMENT_COLOR = new Color(63,127,95);
    private static final Color WHITE_KEYWORD_COLOR  = new Color(127,0,85);
    private static final Color WHITE_VAR_COLOR = new Color(0,0,192);
    private static  final Color WHITE_DEFAULT_COLOR = new Color(0,0,0);
    //榛戜富棰�
    private static final Color BLACK_COMMENT_COLOR = new Color(63,127,95);
    private static final Color BLACK_KEYWORD_COLOR  = new Color(127,0,85);
    private static final Color BLACK_VAR_COLOR = new Color(0,0,192);
    private static  final Color BLACK_DEFAULT_COLOR = new Color(0,0,0);
    // 鏍峰紡涓婁笅鏂�
    private static final StyleContext styleContext = new StyleContext();
    public CodeStyle(ColorType colorType,String path)throws FileNotFoundException,IOException {
        this.colorType = colorType;
        this.path = path;
        if(path != null) {
            this.lexer = new Lexer(path);
            Token token = lexer.nextToken();
            while (token.getTokenType() != TokenType.END_OF_DOC) {
                tokenList.add(token);
                token = lexer.nextToken();
            }
        }
        if(colorType==ColorType.WHITE){
            addStyle("none");
            addStyle("ident",WHITE_VAR_COLOR,FONT_SIZE,FONT_FAMILT);
            addStyle("keyword",WHITE_KEYWORD_COLOR,FONT_SIZE,FONT_FAMILT);
            addStyle("comment",WHITE_COMMENT_COLOR,FONT_SIZE,FONT_FAMILT);
        }else{
            addStyle("none");
            addStyle("ident",BLACK_VAR_COLOR,FONT_SIZE,FONT_FAMILT);
            addStyle("keyword",BLACK_KEYWORD_COLOR,FONT_SIZE,FONT_FAMILT);
            addStyle("comment",BLACK_COMMENT_COLOR,FONT_SIZE,FONT_FAMILT);
        }
    }
    protected void addStyle(String key) {
        if(colorType==ColorType.WHITE)
            addStyle(key,WHITE_DEFAULT_COLOR,FONT_SIZE,FONT_FAMILT);
        else
            addStyle(key,BLACK_DEFAULT_COLOR,FONT_SIZE,FONT_FAMILT);
    }

    protected void addStyle(String key, Color color, int size, String fam) {
        Style s = styleContext.addStyle(key, null);
        if (color != null)
            StyleConstants.setForeground(s, color);
        if (size > 0)
            StyleConstants.setFontSize(s, size);
        if (fam != null)
            StyleConstants.setFontFamily(s, fam);
    }
    public void initCodeStyle(String text,StyledDocument styledDocument) throws BadLocationException{
       if(text!=null){
            unUseLexer(text,styledDocument);
       }else{
           useLexer(styledDocument);
       }
    }

    private void useLexer(StyledDocument sd) throws BadLocationException{
        for(Token t:tokenList){
            Style s = null;
            if(isKeyWord(t.getValue())){
                s = styleContext.getStyle("keywords");
                StyleConstants.setBold(s,true);
            }else if(t.getTokenType()==TokenType.IDENT){
                s = styleContext.getStyle("ident");
                StyleConstants.setBold(s,true);
            }else if(t.getTokenType()==TokenType.MUL_COMMENT||t.getTokenType()==TokenType.SINGLE_COMMENT){
                s = styleContext.getStyle("comment");
            }else{
                s = styleContext.getStyle("none");
            }
            sd.insertString(sd.getLength(),t.getValue(),s);
        }
    }
    private void unUseLexer(String text,StyledDocument sd) throws BadLocationException{
        StringTokenizer stringTokenizer = new StringTokenizer(text,DELIM,true);
        while (stringTokenizer.hasMoreTokens()) {
            String str = stringTokenizer.nextToken();
            Style s = null;
            if (isKeyWord(str.trim())) {
                s = styleContext.getStyle("keywords");
            } else if (str.trim().matches(
                    "^[a-zA-Z](\\w*[a-zA-Z0-9]$)?")) {
                s = styleContext.getStyle("variable");
            } else {
                s = styleContext.getStyle("none");
            }
            sd.insertString(sd.getLength(), str, s);
        }
    }

    private boolean isKeyWord(String s){
        for(String str:keyWords){
            if(str.equals(s))
                return true;
        }
        return false;
    }
}
