import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ListIterator;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class Debugger {
	private Lexer lexer;
	private Parser parser;
	private Sematics sematics;
	private BufferedReader bReader;
	private BufferedWriter bWriter;
	private List<Token> tokenList;
	private List<String> stmtList;
	String tempFile="./temp";
	
	public void run(String path) throws IOException {
		bReader=new BufferedReader(new FileReader(path));
		bWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(tempFile)), "UTF-8"));
        
        read();
        write();
        
        parser=new Parser(tempFile);
        
        tokenList=parser.getTokenList();
	}
	
	public void read() throws IOException {
		String line="";
		int lineNum=0;
        if ((line=bReader.readLine())!=null) {
            lineNum++;
        	stmtList.add(line);
        }
	}
	
	public void write() throws IOException{
		for(String arr:stmtList){
            bWriter.write(arr+"\n");
        }
	}
	
	public void close() throws IOException{
		if(bReader!=null&&bWriter!=null) {
			bReader.close();
			bWriter.close();
		}
	}
}
