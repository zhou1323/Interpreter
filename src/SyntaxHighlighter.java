import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Created by superman on 2017/12/10.
 */
public class SyntaxHighlighter implements DocumentListener {
    private Set<String> keywords;
    private Style keywordStyle;
    private Style normalStyle;
    private Style variableStyle;
    private Style numberStyle;
    private Style commentStyle;

    public SyntaxHighlighter(JTextPane editor) {
        // 准备着色使用的样式
        keywordStyle = ((StyledDocument) editor.getDocument()).addStyle("Keyword_Style", null);
        variableStyle = ((StyledDocument) editor.getDocument()).addStyle("Keyword_Style", null);
        normalStyle = ((StyledDocument) editor.getDocument()).addStyle("Keyword_Style", null);
        numberStyle = ((StyledDocument) editor.getDocument()).addStyle("Keyword_Style", null);
        commentStyle = ((StyledDocument) editor.getDocument()).addStyle("Keyword_Style", null);
        StyleConstants.setForeground(keywordStyle, new Color(0,0,128));
        StyleConstants.setBold(keywordStyle,true);
        StyleConstants.setForeground(variableStyle,new Color(101,13,146));
        StyleConstants.setBold(variableStyle,true);
        StyleConstants.setForeground(normalStyle, Color.BLACK);
        StyleConstants.setForeground(numberStyle,new Color(70,70,255));
        StyleConstants.setForeground(commentStyle,new Color(0,127,70));
        StyleConstants.setItalic(commentStyle,true);

        // 准备关键字
        keywords = new HashSet<String>();
        keywords.add("int");
        keywords.add("double");
        keywords.add("bool");
        keywords.add("if");
        keywords.add("else");
        keywords.add("for");
        keywords.add("while");
        keywords.add("break");
        keywords.add("read");
        keywords.add("write");
        keywords.add("function");
        keywords.add("true");
        keywords.add("false");


    }

    public void colouring(StyledDocument doc, int pos, int len) throws BadLocationException {
        // 取得插入或者删除后影响到的单词.
        // 例如"public"在b后插入一个空格, 就变成了:"pub lic", 这时就有两个单词要处理:"pub"和"lic"
        // 这时要取得的范围是pub中p前面的位置和lic中c后面的位置
        int start = indexOfWordStart(doc, pos);
        int end = indexOfWordEnd(doc, pos + len);

        char ch;
        while (start < end) {
            ch = getCharAt(doc, start);
            if (Character.isLetter(ch) || ch == '_'||ch=='/'||Character.isDigit(ch)) {
                // 如果是以字母或者下划线开头, 说明是单词
                // pos为处理后的最后一个下标
                start = colouringWord(doc, start);
            } else{
                SwingUtilities.invokeLater(new ColouringTask(doc, start, 1, normalStyle));
                ++start;
            }
        }
    }

    /**
     * 对单词进行着色, 并返回单词结束的下标.
     *
     * @param doc
     * @param pos
     * @return
     * @throws BadLocationException
     */
    public int colouringWord(StyledDocument doc, int pos) throws BadLocationException {
        int wordEnd = indexOfWordEnd(doc, pos);
        String word = doc.getText(pos, wordEnd - pos);
        if(word.matches("//[^\n]*")||word.matches("/\\*(\\s|.)*?\\*/")){
            SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd - pos, commentStyle));
        }else if (keywords.contains(word)) {
            SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd - pos, keywordStyle));
        } else if(word.matches("0[xX][0-9a-fA-F]{1,4}||[0-9]+||[0-9]+.[0-9]+")){
            SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd - pos, numberStyle));
        }else if(word.matches("[\\S]+")){
            SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd - pos, variableStyle));
        }else{
            SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd - pos, normalStyle));
        }

        return wordEnd;
    }
    /**
     * 取得在文档中下标在pos处的字符.
     *
     * 如果pos为doc.getLength(), 返回的是一个文档的结束符, 不会抛出异常. 如果pos<0, 则会抛出异常.
     * 所以pos的有效值是[0, doc.getLength()]
     *
     * @param doc
     * @param pos
     * @return
     * @throws BadLocationException
     */
    public char getCharAt(Document doc, int pos) throws BadLocationException {
        return doc.getText(pos, 1).charAt(0);
    }

    /**
     * 取得下标为pos时, 它所在的单词开始的下标. Â±wor^dÂ± (^表示pos, Â±表示开始或结束的下标)
     *
     * @param doc
     * @param pos
     * @return
     * @throws BadLocationException
     */
    public int indexOfWordStart(Document doc, int pos) throws BadLocationException {
        // 从pos开始向前找到第一个非单词字符.
        for (; pos > 0 && isWordCharacter(doc, pos - 1); --pos);

        return pos;
    }

    /**
     * 取得下标为pos时, 它所在的单词结束的下标. Â±wor^dÂ± (^表示pos, Â±表示开始或结束的下标)
     *
     * @param doc
     * @param pos
     * @return
     * @throws BadLocationException
     */
    public int indexOfWordEnd(Document doc, int pos) throws BadLocationException {
        // 从pos开始向前找到第一个非单词字符.
        for (; isWordCharacter(doc, pos); ++pos);

        return pos;
    }

    /**8
     * 如果一个字符是字母, 数字, 下划线,/,*, 则返回true.
     *
     * @param doc
     * @param pos
     * @return
     * @throws BadLocationException
     */
    public boolean isWordCharacter(Document doc, int pos) throws BadLocationException {
        char ch = getCharAt(doc, pos);
        if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '_'||ch=='/'||ch=='*') { return true; }
        return false;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        try {
            colouring((StyledDocument) e.getDocument(), e.getOffset(), e.getLength());
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        try {
            // 因为删除后光标紧接着影响的单词两边, 所以长度就不需要了
            colouring((StyledDocument) e.getDocument(), e.getOffset(), 0);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 完成着色任务
     *
     * @author Biao
     *
     */
    private class ColouringTask implements Runnable {
        private StyledDocument doc;
        private Style style;
        private int pos;
        private int len;

        public ColouringTask(StyledDocument doc, int pos, int len, Style style) {
            this.doc = doc;
            this.pos = pos;
            this.len = len;
            this.style = style;
        }

        public void run() {
            try {
                // 这里就是对字符进行着色
                doc.setCharacterAttributes(pos, len, style, true);
            } catch (Exception e) {}
        }
    }
}

