package Lexer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by superman on 2017/10/10.
 */
public class CharReader {
    private BufferedReader bReader;
    private String line;
    public static final char EOF = (char)0;
    public static final char EOL = '\n';
    private int lineNum = 0;
    private int position;

    public CharReader(String path) throws FileNotFoundException {
        this.bReader = new BufferedReader(new FileReader(path));
        this.line = readNextLine();
    }
    //获取当前行号
    public int getLineNum() {
        return lineNum;
    }
    //获取当前位置
    public int getPosition() {
        return position;
    }
    //下一行
    public String readNextLine() {
        String s = null;
        try {
            s = bReader.readLine();
        } catch (IOException e) {
            System.out.println(e);
        }
        position = -1;
        if(s != null){
            lineNum = getLineNum() + 1;
            s = s + CharReader.EOL;
        }
        return s;
    }
    //下一个字符，指针移动
    public char nextChar() throws IOException{
        position ++;
        if(line == null) {
            return CharReader.EOF;
        }
        if(position>=line.length()){
            line = readNextLine();
            return nextChar();
        }
        return line.charAt(position);
    }
    //第几个字符
    public char peekChar(int offset) throws IOException{
        if(line==null||"".equals(line)){
            return nextChar();
        }
        int index= position+offset;
        if(index>=line.length()||index<=-1){
            line = readNextLine();
            if(line!=null)
                return line.charAt(position+1);
            return EOF;
        }
        return line.charAt(index);
    }
    //下一个字符但指针不移动
    public char peekChar() throws IOException{
        return this.peekChar(1);
    }
    //关闭流
    public void charReaderClose() throws IOException{
        if(bReader != null){
            bReader.close();
        }
    }
}
