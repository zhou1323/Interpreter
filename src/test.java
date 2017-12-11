import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * Created by superman on 2017/10/12.
 */
public class test {
    public static void main(String[] args) throws Exception{
        String path;
        if(args.length==1){
            path = args[0];
            
           Parser parser=new Parser(path);
           //语义分析
           sematics(path);
           //词法分析
           //List<Token> tokenList=parser.getTokenList();
           //for (Token token : tokenList) {
			//System.out.println(token.toString());
			//}
           
           //语法
			//TreeNode treeNode = parser.parseProgram();
	        //parse(treeNode);
		
           
        }else{
            System.out.println("arguement missing");
        }
//        String path;
//        Scanner sc = new Scanner(System.in);
//        path = sc.next();

    }

    public static void sematics(String path) throws Exception {
    	Sematics s=new Sematics(path);
    	SymbolTable symbols=s.getTable();
    	symbols.printTable();
    	//TreeNode treeNode = s.getParser().parseProgram();
        //parse(treeNode);
    }
    public static void parse(TreeNode treeNode) {
    	System.out.print("    "+treeNode.getValue());
        printValue(treeNode,0);;
    }
    public static void printValue(TreeNode treeNode,int offset){
        if(!treeNode.getChildren().isEmpty()){
            for(TreeNode t:treeNode.getChildren()){
                System.out.print("\n");
                for(int i = 0 ;i <=offset; i++){
                    System.out.print("    ");
                }
                System.out.print("+--- ");
                System.out.print(t.getValue());
                printValue(t,offset+1);
            }
        }
    }
}
