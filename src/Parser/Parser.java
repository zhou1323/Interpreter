package Parser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import Lexer.Lexer;
import Lexer.Token;
import Lexer.TokenType;
import Utils.Redirector;


/**
 * Created by superman on 2017/10/24.
 */
public class Parser{
    private Lexer lexer;
    private List<Token> tokenList = new ArrayList<>();
    private ListIterator<Token> tokenListIterator;
    //构造函数，初始化Lexer,tokenList,迭代器
    public Parser(String path) throws FileNotFoundException,IOException {
        this.lexer = new Lexer(path);
        Token token = this.lexer.nextToken();
        while(token.getTokenType()!=TokenType.END_OF_DOC){
            if(token.getTokenType()!=TokenType.MUL_COMMENT&&token.getTokenType()!=TokenType.SINGLE_COMMENT) {
                tokenList.add(token);
            }
//            System.out.println(token.getValue());
            token = this.lexer.nextToken();
        }
        this.tokenListIterator = tokenList.listIterator();
    }

    public Lexer getLexer() {
        return lexer;
    }

    public void setLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    public List<Token> getTokenList() {
        return tokenList;
    }

    public void setTokenList(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public ListIterator<Token> getTokenListIterator() {
        return tokenListIterator;
    }

    public void setTokenListIterator(ListIterator<Token> tokenListIterator) {
        this.tokenListIterator = tokenListIterator;
    }

    private String getNextTokenValue(){
        if(this.tokenListIterator.hasNext()){
            String value = this.tokenListIterator.next().getValue();
            this.tokenListIterator.previous();
            return value;
        }
        return null;
    }

    private Token getCurrToken(){
        if(this.tokenListIterator.hasNext()){
            this.tokenListIterator.next();
            Token token=this.tokenListIterator.previous();
            return token;
        }
        else{
            this.tokenListIterator.previous();
            Token currToken=this.tokenListIterator.next();
            return currToken;
        }
       // return null;
    }

    /**
     * 获取下一个token的类型，迭代器游标不移动
     * @return tokenType
     */
    private TokenType  getNextTokenType(){
        if(this.tokenListIterator.hasNext()){
            TokenType tokenType = this.tokenListIterator.next().getTokenType();
            this.tokenListIterator.previous();
            return tokenType;
        }
        return TokenType.END_OF_DOC;
    }

    /**
     * 消耗掉下一个token(并且忽略注释token)
     * @param tokenType
     * @throws ParserException
     */
    private void consumeNextToken(TokenType tokenType)throws ParserException{
        if(getNextTokenType()==TokenType.MUL_COMMENT||getNextTokenType()== TokenType.SINGLE_COMMENT){
            getTokenListIterator().next();
        }
        if(getNextTokenType()==tokenType){
            getTokenListIterator().next();
        }else{
            Token token = getTokenListIterator().next();
            try{throw new ParserException("Line:"+token.getLineNum()+
                    ", Position:"+token.getPosition()+"; unexpected token");}
            catch (Exception e){
            	Redirector.updateErrorPane(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 解析program
     * Program 	--> Stmt { Stmt }
     * @return
     * @throws ParserException
     */
    public TreeNode parseProgram() throws ParserException{
        TreeNode progNode = new TreeNode(NodeType.PROGRAM,"program",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> progNodeChildren = progNode.getChildren();
        while(getNextTokenType()!=TokenType.END_OF_DOC){
            while(getNextTokenType()==TokenType.MUL_COMMENT||getNextTokenType()==TokenType.SINGLE_COMMENT){
                getTokenListIterator().next();
            }
            TreeNode treeNode = parseStmt();
            treeNode.setParent(progNode);
            progNodeChildren.add(treeNode);
        }
        return progNode;
    }
    /**
     * 解析STMT
     * Stmt	 --> VarDecl | IfStmt | WhileStmt | BreakStmt | AssignStmt |
     ReadStmt | WriteStmt | StmtBlock |FuncStmt
     * @return
     * @throws ParserException
     */
    private TreeNode parseStmt() throws ParserException{
            TreeNode stmtNode = new TreeNode(NodeType.STMT, "stmt", null, getCurrToken().getLineNum(), getCurrToken().getPosition());
            List<TreeNode> stmtNodeChildren = stmtNode.getChildren();
            TreeNode childNode;
            switch (getNextTokenType()) {
                case IF:
                    childNode = parseIfStmt();
                    break;
                case WHILE:
                    childNode = parseWhileStmt();
                    break;
                case FOR:
                    childNode = parseForStmt();
                    break;
                case READ:
                    childNode = parseReadStmt();
                    break;
                case WRITE:
                    childNode = parseWriteStmt();
                    break;
                case BREAK:
                    childNode = parseBreakStmt();
                    break;
                case RETURN:
                    childNode=parseReturnStmt();
                    break;
                case L_BRACE:
                    childNode = parseStmtBlock();
                    break;
                case INT:
                case DOUBLE:
                case BOOL:
                    childNode = parseVarDecl();
                    break;
                case IDENT:
                    String value = getNextTokenValue();
                    consumeNextToken(TokenType.IDENT);
                    if (getNextTokenType().equals(TokenType.L_PAREN)) {
                        childNode = parseCallStmt(value);
                    } else {
                        childNode = parseAssignStmt(value);
                    }
                    break;
                case FUNCTION:
                    childNode = parseFuncStmt();
                    break;
                case END_OF_DOC:

                default:
                   // throw new ParserException("");
                	Token token = getTokenListIterator().next();
                	throw new ParserException("Line:"+token.getLineNum()+
                            ", Position:"+token.getPosition()+"; unexpected token");
                    
            }

            childNode.setParent(stmtNode);
            stmtNodeChildren.add(childNode);
            return stmtNode;
          
    }

    private TreeNode parseReturnStmt()throws ParserException{
        TreeNode returnStmtNode = new TreeNode(NodeType.RETURN_STMT,"return_stmt",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> children = returnStmtNode.getChildren();
        consumeNextToken(TokenType.RETURN);
        String value=getNextTokenValue();
        consumeNextToken(TokenType.IDENT);
        consumeNextToken(TokenType.SEMICOLON);
        children.add(new TreeNode(NodeType.RETURN,"return",returnStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        children.add(new TreeNode(NodeType.IDENT,value,returnStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        children.add(new TreeNode(NodeType.SEMICOLON,";",returnStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        return returnStmtNode;
    }
    //ForStmt-> for ( Decl|Assign;LogExpr;var++|var--)Stmt
    private TreeNode parseForStmt() throws ParserException{
        TreeNode forStmtNode = new TreeNode(NodeType.FOR_STMT,"for_stmt",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> children = forStmtNode.getChildren();
        consumeNextToken(TokenType.FOR);
        consumeNextToken(TokenType.L_PAREN);
        children.add(new TreeNode(NodeType.FOR,"for",forStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        children.add(new TreeNode(NodeType.L_PAREN,"(",forStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        if(getNextTokenType()==TokenType.IDENT){
        	String v = getNextTokenValue();
            consumeNextToken(TokenType.IDENT);
            TreeNode expr=parseAssignStmt(v);  //for(assignStmt
            expr.setParent(forStmtNode);
            children.add(expr);}
        else if((getNextTokenType()==TokenType.INT)||(getNextTokenType()==TokenType.DOUBLE)){
            TreeNode expr=parseVarDecl();  //for(assignStmt
            expr.setParent(forStmtNode);
            children.add(expr);}
        TreeNode exprlog=parseLogExpr(); // for(assignstmt LogExpr
        exprlog.setParent(forStmtNode);
        children.add(exprlog);
        consumeNextToken(TokenType.SEMICOLON);
        children.add(new TreeNode(NodeType.SEMICOLON,";",forStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        if(getNextTokenType()==TokenType.IDENT) {
            TokenType tokenType = this.tokenListIterator.next().getTokenType();
            TokenType tokenType1 = this.tokenListIterator.next().getTokenType();
            this.tokenListIterator.previous();
            this.tokenListIterator.previous();
            if(tokenType1==TokenType.ASSIGN){
                TreeNode value = parseAddStmt();
                value.setParent(forStmtNode);
                children.add(value);}
          /*  else if((tokenType1==TokenType.PLUS)||(tokenType1==TokenType.MINUS)){
                TreeNode value = parseAddStmt();
                value.setParent(forStmtNode);
                children.add(value);}*/
        }
        consumeNextToken(TokenType.R_PAREN);
        children.add(new TreeNode(NodeType.R_PAREN,")",forStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        TreeNode stmt = parseStmt();
        stmt.setParent(forStmtNode);
        children.add(stmt);


        return forStmtNode;
    }

    private TreeNode parseAddStmt()throws ParserException{
        TreeNode addStmtNode = new TreeNode(NodeType.ADD_STMT,"add_stmt",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> children = addStmtNode.getChildren();
        String v = getNextTokenValue();
        consumeNextToken(TokenType.IDENT);
        TreeNode value = parseValue(v);    // i
        value.setParent(addStmtNode);
        children.add(value);
        consumeNextToken(TokenType.ASSIGN);
        children.add(new TreeNode(NodeType.ASSIGN,"=",addStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        TreeNode expr = parseAriExpr();
        expr.setParent(addStmtNode);
        children.add(expr);
    /*    if(getNextTokenType()==TokenType.PLUS) {
            consumeNextToken(TokenType.PLUS);
            consumeNextToken(TokenType.PLUS);
            children.add(new TreeNode(NodeType.PLUS,"+",addStmtNode));
            children.add(new TreeNode(NodeType.PLUS,"+",addStmtNode));
        }
        else if(getNextTokenType()==TokenType.MINUS) {
            consumeNextToken(TokenType.MINUS);
            consumeNextToken(TokenType.MINUS);
            children.add(new TreeNode(NodeType.MINUS,"-",addStmtNode));
            children.add(new TreeNode(NodeType.MINUS,"-",addStmtNode));
        }*/

        return addStmtNode;
    }
    /**
     * 解析if语句并生成树节点
     * IfStmt --> if (LogExpr) Stmt [ else Stmt ]
     * @return TreeNode
     * @throws ParserException
     */
    private TreeNode parseIfStmt()throws ParserException{
        TreeNode ifStmtNode = new TreeNode(NodeType.IF_STMT,"if_stmt",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> ifStmtNodeChildren = ifStmtNode.getChildren();
        consumeNextToken(TokenType.IF);
        consumeNextToken(TokenType.L_PAREN);
        ifStmtNodeChildren.add(new TreeNode(NodeType.IF,"if",ifStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        ifStmtNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",ifStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        TreeNode expr = parseLogExpr();
        expr.setParent(ifStmtNode);
        ifStmtNodeChildren.add(expr);
        consumeNextToken(TokenType.R_PAREN);
        ifStmtNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",ifStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        TreeNode stmt = parseStmt();
        stmt.setParent(ifStmtNode);
        ifStmtNodeChildren.add(stmt);
        if(getNextTokenType()== TokenType.ELSE){
            consumeNextToken(TokenType.ELSE);
            ifStmtNodeChildren.add(new TreeNode(NodeType.ELSE,"else",ifStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            TreeNode stmtt = parseStmt();
            stmtt.setParent(ifStmtNode);
            ifStmtNodeChildren.add(stmtt);
        }
        return ifStmtNode;
    }

    /**
     *  解析while语句
     *  WhileStmt --> while (LogExpr) Stmt
     * @return TreeNode
     * @throws ParserException
     */
    private TreeNode parseWhileStmt()throws ParserException{
        TreeNode whileStmtNode = new TreeNode(NodeType.WHILE_STMT,"while_stmt",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> whileStmtNodeChildren = whileStmtNode.getChildren();
        consumeNextToken(TokenType.WHILE);
        whileStmtNodeChildren.add(new TreeNode(NodeType.WHILE,"while",whileStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        consumeNextToken(TokenType.L_PAREN);
        whileStmtNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",whileStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        TreeNode expr = parseLogExpr();
        expr.setParent(whileStmtNode);
        whileStmtNodeChildren.add(expr);
        consumeNextToken(TokenType.R_PAREN);
        whileStmtNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",whileStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        TreeNode stmt = parseStmt();
        stmt.setParent(whileStmtNode);
        whileStmtNodeChildren.add(stmt);
        return whileStmtNode;
    }

    /**
     * 解析赋值语句
     * AssignStmt --> Value = AriExpr ;
     * @return
     * @throws ParserException
     */
    private TreeNode parseAssignStmt(String v)throws ParserException{
        TreeNode assignNode = new TreeNode(NodeType.ASSIGN_STMT,"assignStmt",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> assignNodeChildren = assignNode.getChildren();
        TreeNode value = parseValue(v);
        value.setParent(assignNode);
        assignNodeChildren.add(value);
        consumeNextToken(TokenType.ASSIGN);
        assignNodeChildren.add(new TreeNode(NodeType.ASSIGN,"=",assignNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        TreeNode expr = parseAriExpr();
        expr.setParent(assignNode);
        assignNodeChildren.add(expr);
        consumeNextToken(TokenType.SEMICOLON);
        assignNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",assignNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        return assignNode;
    }
    
    /**
     * 解析函数调用语句
     * CallStmt --> ident ( ) ;
     * @return
     * @throws ParserException
     */
    private TreeNode parseCallStmt(String v) throws ParserException{
    	TreeNode callNode=new TreeNode(NodeType.CALL_STMT, "callStmt", null,getCurrToken().getLineNum(), getCurrToken().getPosition());
    	List<TreeNode> callNodeChildren = callNode.getChildren();
    	
    	callNodeChildren.add(new TreeNode(NodeType.IDENT,v,callNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
    	if(getNextTokenType()==TokenType.L_PAREN){
            consumeNextToken(TokenType.L_PAREN);
            callNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",callNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            while(getNextTokenType()!=TokenType.R_PAREN){
                TreeNode varList = parseAriExpr();
                varList.setParent(callNode);
                callNodeChildren.add(varList);
                if(getNextTokenType()!=TokenType.R_PAREN){
                consumeNextToken(TokenType.COMMA);
                callNodeChildren.add(new TreeNode(NodeType.COMMA,",",callNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));}
            }
            consumeNextToken(TokenType.R_PAREN);
            callNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",callNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            consumeNextToken(TokenType.SEMICOLON);
            callNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",callNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }
        return callNode;
    }
    /**
     * 解析break语句
     * BreakStmt		break ;
     * @return TreeNode
     * @throws ParserException
     */
    private TreeNode parseBreakStmt()throws ParserException{
        TreeNode breakStmtNode = new TreeNode(NodeType.BREAK_STMT,"break_stmt",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> breakStmtNodeChildren = breakStmtNode.getChildren();
        consumeNextToken(TokenType.BREAK);
        consumeNextToken(TokenType.SEMICOLON);
        breakStmtNodeChildren.add(new TreeNode(NodeType.BREAK,"break",breakStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        breakStmtNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",breakStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        return breakStmtNode;
    }

    /**
     * 解析read语句
     * ReadStmt	 	read (Value);
     * @return TreeNode
     * @throws ParserException
     */
    private TreeNode parseReadStmt()throws ParserException{
        TreeNode readStmtNode = new TreeNode(NodeType.READ_STMT,"read_stmt",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> readStmtNodeChildren = readStmtNode.getChildren();
        consumeNextToken(TokenType.READ);
        readStmtNodeChildren.add(new TreeNode(NodeType.READ,"read",readStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        consumeNextToken(TokenType.L_PAREN);
        readStmtNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",readStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        String v = getNextTokenValue();
        consumeNextToken(TokenType.IDENT);
        TreeNode value = parseValue(v);
        value.setParent(readStmtNode);
        readStmtNodeChildren.add(value);
        consumeNextToken(TokenType.R_PAREN);
        readStmtNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",readStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        consumeNextToken(TokenType.SEMICOLON);
        readStmtNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",readStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        return readStmtNode;
    }

    /**
     * 解析write语句
     * WriteStmt	 	write(AriExpr);
     * @return TreeNode
     * @throws ParserException
     */
    private TreeNode parseWriteStmt()throws ParserException{
        TreeNode writeStmtNode = new TreeNode(NodeType.WRITE_STMT,"write_stmt",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> writeStmtNodeChildren = writeStmtNode.getChildren();
        consumeNextToken(TokenType.WRITE);
        writeStmtNodeChildren.add(new TreeNode(NodeType.WRITE,"write",writeStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        consumeNextToken(TokenType.L_PAREN);
        writeStmtNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",writeStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        TreeNode expr = parseAriExpr();
        expr.setParent(writeStmtNode);
        writeStmtNodeChildren.add(expr);
        consumeNextToken(TokenType.R_PAREN);
        writeStmtNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",writeStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        consumeNextToken(TokenType.SEMICOLON);
        writeStmtNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",writeStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        return writeStmtNode;
    }

    /**
     * 解析语句块
     * @return TreeNode
     * @throws ParserException
     */
    private TreeNode parseStmtBlock() throws ParserException{
        TreeNode stmtBlockNode = new TreeNode(NodeType.STMT_BLOCK,"stmt_block",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> stmtBlokNodeChildren = stmtBlockNode.getChildren();
        consumeNextToken(TokenType.L_BRACE);
        stmtBlokNodeChildren.add(new TreeNode(NodeType.L_BRACE,"{",stmtBlockNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        while(getNextTokenType()!= TokenType.R_BRACE){
            TreeNode block = parseStmt();
            block.setParent(stmtBlockNode);
            stmtBlokNodeChildren.add(block);
        }
        consumeNextToken(TokenType.R_BRACE);
        stmtBlokNodeChildren.add(new TreeNode(NodeType.R_BRACE,"}",stmtBlockNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        return stmtBlockNode;
    }

    /**
     * 解析函数
     * @return TreeNode
     * @throws ParserException
     */
    private TreeNode parseFuncStmt() throws ParserException{
    	TreeNode funcStmtNode = new TreeNode(NodeType.FUNCTION_STMT,"func_stmt",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> funcStmtNodeChildren = funcStmtNode.getChildren();
        consumeNextToken(TokenType.FUNCTION);
        funcStmtNodeChildren.add(new TreeNode(NodeType.FUNCTION,"function",funcStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        
        String v = getNextTokenValue();
        consumeNextToken(TokenType.IDENT);
        TreeNode funcHead=new TreeNode(NodeType.IDENT,v,funcStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition());
        funcStmtNodeChildren.add(funcHead);
        consumeNextToken(TokenType.L_PAREN);
        funcStmtNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",funcStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        TreeNode funVarNode=new TreeNode(NodeType.FUN_VARLIST,"func_varList",funcStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> funcVarNodeChildren = funVarNode.getChildren();
        while(getNextTokenType()==TokenType.INT||getNextTokenType()==TokenType.DOUBLE
                ||getNextTokenType()==TokenType.BOOL){
            TreeNode type = parseType();
            type.setParent(funVarNode);
            funcVarNodeChildren.add(type);
            String value=getNextTokenValue();
            consumeNextToken(TokenType.IDENT);
            funcVarNodeChildren.add(new TreeNode(NodeType.IDENT,value,funcStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            if(getNextTokenType()==TokenType.R_PAREN){}
            else{
                consumeNextToken(TokenType.COMMA);
                funcVarNodeChildren.add(new TreeNode(NodeType.COMMA,",",funcStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            }
        }
        funVarNode.setParent(funcStmtNode);
        funcStmtNodeChildren.add(funVarNode);

        consumeNextToken(TokenType.R_PAREN);
        funcStmtNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",funcStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        if(getNextTokenType()==TokenType.L_BRACE){
        TreeNode funcBody = parseStmtBlock();
        funcBody.setParent(funcStmtNode);
        funcStmtNodeChildren.add(funcBody);}
        else if(getNextTokenType()==TokenType.SEMICOLON){
            consumeNextToken(TokenType.SEMICOLON);
            funcStmtNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",funcStmtNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }
        
        return funcStmtNode;
    }
    /**
     * 解析value语句
     * Value --> ident [intconstant] | ident
     * @return
     * @throws ParserException
     */
    private TreeNode parseValue(String value) throws ParserException{
        TreeNode valueNode = new TreeNode(NodeType.VALUE,"value",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> valueNodeChildren = valueNode.getChildren();
        
        valueNodeChildren.add(new TreeNode(NodeType.IDENT,value,valueNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        if(getNextTokenType()==TokenType.L_BRACKET){
            consumeNextToken(TokenType.L_BRACKET);
            valueNodeChildren.add(new TreeNode(NodeType.L_BRACKET,"[",valueNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            TreeNode ariExpr = parseAriExpr();
            ariExpr.setParent(valueNode);
            valueNodeChildren.add(ariExpr);
            consumeNextToken(TokenType.R_BRACKET);
            valueNodeChildren.add(new TreeNode(NodeType.R_BRACKET,"]",valueNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }
        return valueNode;
    }

    /**
     * 解析arrayValue语句
     * arrayValue --> { Value (,Value)* }
     * @return
     * @throws ParserException
     */
    private TreeNode parseArrayValue() throws ParserException{
    	TreeNode arrayValueNode = new TreeNode(NodeType.VALUE,"arrayValue",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> arrayValueNodeChildren = arrayValueNode.getChildren();
        
        consumeNextToken(TokenType.L_BRACE);
        arrayValueNodeChildren.add(new TreeNode(NodeType.L_BRACE,"{",arrayValueNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        
        TreeNode ariExpr = parseAriExpr();
        ariExpr.setParent(arrayValueNode);
        arrayValueNodeChildren.add(ariExpr);
        
        while(getNextTokenType()==TokenType.COMMA){
            consumeNextToken(TokenType.COMMA);
            arrayValueNodeChildren.add(new TreeNode(NodeType.COMMA,",",arrayValueNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            
            TreeNode ariExpr0 = parseAriExpr();
            ariExpr0.setParent(arrayValueNode);
            arrayValueNodeChildren.add(ariExpr0);
            
        }
        
        consumeNextToken(TokenType.R_BRACE);
        arrayValueNodeChildren.add(new TreeNode(NodeType.R_BRACE,"}",arrayValueNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        
        return arrayValueNode;
    }
//    private TreeNode parseConstant()throws ParserException{
//        TreeNode constantNode = new TreeNode(NodeType.CONSTANT,"constant",null);
//        List<TreeNode> constNodeChildren = constantNode.getChildren();
//        String value = getNextTokenValue();
//        if(getNextTokenType()==TokenType.INT_NUM){
//            consumeNextToken(TokenType.INT_NUM);
//            constNodeChildren.add(new TreeNode(NodeType.INT_NUM,value,constantNode));
//        }else if(getNextTokenType()==TokenType.DOUBLE_NUM){
//            consumeNextToken(TokenType.DOUBLE_NUM);
//            constNodeChildren.add(new TreeNode(NodeType.DOUBLE_NUM,value,constantNode));
//        }else if(getNextTokenType()==TokenType.TRUE){
//            consumeNextToken(TokenType.TRUE);
//            constNodeChildren.add(new TreeNode(NodeType.TRUE,value,constantNode));
//        }else if(getNextTokenType()==TokenType.FALSE){
//            consumeNextToken(TokenType.FALSE);
//            constNodeChildren.add(new TreeNode(NodeType.FALSE,value,constantNode));
//        }
//        return constantNode;
//    }

    /**
     * 解析算术表达式
     *  E --> TE'
     *  E'--> +TE'| -TE'| null
     *  T --> FT'
     *  T'--> *FT'|/FT'|%FT'|null
     *  F --> (E)|value|intconstant|doubleconstant
     * @return
     * @throws ParserException
     */
    private TreeNode parseAriExpr() throws ParserException {
        TreeNode ariExprNode = new TreeNode(NodeType.ARI_EXPR, "E", null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> ariExprNodeChildren = ariExprNode.getChildren();
//        if(getNextTokenType()==TokenType.MINUS) {
//            consumeNextToken(TokenType.MINUS);
//            ariExprNodeChildren.add(new TreeNode(NodeType.MINUS,"-",ariExprNode));
//            TreeNode term1 = parseTerm();
//            term1.setParent(ariExprNode);
//            ariExprNodeChildren.add(term1);
//        }
//        if(getNextTokenType()== TokenType.IDENT||getNextTokenType()== TokenType.INT_NUM
//                ||getNextTokenType()==TokenType.DOUBLE_NUM||getNextTokenType()==TokenType.L_PAREN||
//                getNextTokenType()==TokenType.MINUS) {
//            TreeNode term = parseTerm();
//            term.setParent(ariExprNode);
//            ariExprNodeChildren.add(term);
//            TreeNode ariExprPrime = parseAriExprPrime();
//            ariExprPrime.setParent(ariExprNode);
//            ariExprNodeChildren.add(ariExprPrime);
//        }
        TreeNode term = parseTerm();
        term.setParent(ariExprNode);
        ariExprNodeChildren.add(term);
        while(getNextTokenType()==TokenType.MINUS||getNextTokenType()==TokenType.PLUS){
            if(getNextTokenType()==TokenType.MINUS){
                consumeNextToken(TokenType.MINUS);
                ariExprNodeChildren.add(new TreeNode(NodeType.MINUS,"-",ariExprNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            }else{
                consumeNextToken(TokenType.PLUS);
                ariExprNodeChildren.add(new TreeNode(NodeType.PLUS,"+",ariExprNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            }
            TreeNode term1 = parseTerm();
            term1.setParent(ariExprNode);
            ariExprNodeChildren.add(term1);
        }
        return ariExprNode;
    }

//    private TreeNode parseAriExprPrime()throws  ParserException{
//        TreeNode ariExprPrimeNode = new TreeNode(NodeType.ARI_EXPR_PRIME,"E'",null);
//        List<TreeNode> ariPrimeChildren = ariExprPrimeNode.getChildren();
//        if(getNextTokenType()==TokenType.PLUS||getNextTokenType()==TokenType.MINUS){
//           if(getNextTokenType()==TokenType.PLUS){
//               consumeNextToken(TokenType.PLUS);
//               ariPrimeChildren.add(new TreeNode(NodeType.PLUS,"+",ariExprPrimeNode));
//           }else{
//               consumeNextToken(TokenType.MINUS);
//               ariPrimeChildren.add(new TreeNode(NodeType.MINUS,"-",ariExprPrimeNode));
//           }
//           TreeNode term = parseTerm();
//           term.setParent(ariExprPrimeNode);
//           ariPrimeChildren.add(term);
//           TreeNode ariPrime = parseAriExprPrime();
//           ariPrime.setParent(ariExprPrimeNode);
//           ariPrimeChildren.add(ariPrime);
//        }else if(getNextTokenType()==TokenType.R_PAREN){
//            ariPrimeChildren.add(new TreeNode(NodeType.NULL,"mull",ariExprPrimeNode));
//        }else{
//            ariPrimeChildren.add(new TreeNode(NodeType.NULL,"mull",ariExprPrimeNode));
//        }
//        return ariExprPrimeNode;
//    }

    private TreeNode parseTerm() throws ParserException{
        TreeNode termNode = new TreeNode(NodeType.TERM,"T",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> termNodeChildren = termNode.getChildren();
        TreeNode factor = parseFactor();
        factor.setParent(termNode);
        termNodeChildren.add(factor);
        while(getNextTokenType()==TokenType.MOD||getNextTokenType()==TokenType.DIV||getNextTokenType()==TokenType.MULTI){
            if(getNextTokenType()==TokenType.MOD){
                consumeNextToken(TokenType.MOD);
                termNodeChildren.add(new TreeNode(NodeType.MOD,"%",termNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            }else if(getNextTokenType()==TokenType.DIV){
                consumeNextToken(TokenType.DIV);
                termNodeChildren.add(new TreeNode(NodeType.DIV,"/",termNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            }else{
                consumeNextToken(TokenType.MULTI);
                termNodeChildren.add(new TreeNode(NodeType.MULTI,"*",termNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            }
            TreeNode factor1 = parseFactor();
            factor1.setParent(termNode);
            termNodeChildren.add(factor1);
        }
//        if(getNextTokenType()== TokenType.IDENT||getNextTokenType()== TokenType.INT_NUM
//                ||getNextTokenType()==TokenType.DOUBLE_NUM||getNextTokenType()==TokenType.L_PAREN) {
//            TreeNode factor = parseFactor();
//            factor.setParent(termNode);
//            termNodeChildren.add(factor);
//            TreeNode termPrime = parseTermPrime();
//            termPrime.setParent(termNode);
//            termNodeChildren.add(termPrime);
//        }
        return termNode;
    }

//    private TreeNode parseTermPrime()throws ParserException{
//        TreeNode termPrimeNode = new TreeNode(NodeType.TERM_PRIME,"T'",null);
//        List<TreeNode> termPrimeChildren = termPrimeNode.getChildren();
//        if(getNextTokenType()==TokenType.MULTI||getNextTokenType()==TokenType.DIV||getNextTokenType()==TokenType.MOD){
//            if(getNextTokenType()==TokenType.MULTI){
//                consumeNextToken(TokenType.MULTI);
//                termPrimeChildren.add(new TreeNode(NodeType.MULTI,"*",termPrimeNode));
//            }else if(getNextTokenType()==TokenType.DIV){
//                consumeNextToken(TokenType.DIV);
//                termPrimeChildren.add(new TreeNode(NodeType.DIV,"/",termPrimeNode));
//            }else{
//                consumeNextToken(TokenType.MOD);
//                termPrimeChildren.add(new TreeNode(NodeType.MOD,"%",termPrimeNode));
//            }
//            TreeNode factor = parseTerm();
//            factor.setParent(termPrimeNode);
//            termPrimeChildren.add(factor);
//            TreeNode termPrime = parseAriExprPrime();
//            termPrime.setParent(termPrimeNode);
//            termPrimeChildren.add(termPrime);
//        }else if(getNextTokenType()==TokenType.R_PAREN||getNextTokenType()== TokenType.PLUS
//                ||getNextTokenType()==TokenType.MINUS){
//            termPrimeChildren.add(new TreeNode(NodeType.NULL,"null",termPrimeNode));
//        }else{
//            termPrimeChildren.add(new TreeNode(NodeType.NULL,"null",termPrimeNode));
//        }
//        return termPrimeNode;
//    }

    private TreeNode parseFactor()throws ParserException{
        TreeNode factorNode = new TreeNode(NodeType.FACTOR,"F",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> facNodeChildren = factorNode.getChildren();
        switch (getNextTokenType()){
            case TRUE:
                consumeNextToken(TokenType.TRUE);
                facNodeChildren.add(new TreeNode(NodeType.TRUE,"true",factorNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
                break;
            case FALSE:
                consumeNextToken(TokenType.FALSE);
                facNodeChildren.add(new TreeNode(NodeType.FALSE,"false",factorNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
                break;

            case IDENT:
            	String v = getNextTokenValue();
                consumeNextToken(TokenType.IDENT);
                TreeNode value = parseValue(v);
                value.setParent(factorNode);
                facNodeChildren.add(value);
                break;

            case INT_NUM:
                String intvalue = getNextTokenValue();
                consumeNextToken(TokenType.INT_NUM);
                facNodeChildren.add(new TreeNode(NodeType.INT_NUM,intvalue,factorNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
                break;
            case DOUBLE_NUM:
                String douvalue = getNextTokenValue();
                consumeNextToken(TokenType.DOUBLE_NUM);
                facNodeChildren.add(new TreeNode(NodeType.DOUBLE_NUM,douvalue,factorNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
                break;
            case L_PAREN:
                consumeNextToken(TokenType.L_PAREN);
                facNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",factorNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
                TreeNode ariExpr = parseAriExpr();
                ariExpr.setParent(factorNode);
                facNodeChildren.add(ariExpr);
                consumeNextToken(TokenType.R_PAREN);
                facNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",factorNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
                break;
            case MINUS:
                consumeNextToken(TokenType.MINUS);
                facNodeChildren.add(new TreeNode(NodeType.MINUS,"-",factorNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
                TreeNode factor = parseFactor();
                factor.setParent(factorNode);
                facNodeChildren.add(factor);
        }
        return factorNode;
    }

    /**
     * 解析逻辑表达式
     * LogExpr      AriExpr LogOp AriExpr | true | false
     * @return
     * @throws ParserException
     */
    private TreeNode parseLogExpr()throws ParserException{
        TreeNode logExprNode = new TreeNode(NodeType.LOG_EXPR,"logicalExpr",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> logExprChildren = logExprNode.getChildren();
        if(getNextTokenType()==TokenType.TRUE){
            consumeNextToken(TokenType.TRUE);
            logExprChildren.add(new TreeNode(NodeType.TRUE,"true",logExprNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }else if(getNextTokenType()==TokenType.FALSE){
            consumeNextToken(TokenType.FALSE);
            logExprChildren.add(new TreeNode(NodeType.FALSE,"false",logExprNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }else{
            TreeNode ariExpr1 = parseAriExpr();
            ariExpr1.setParent(logExprNode);
            logExprChildren.add(ariExpr1);
            if(getNextTokenType()==TokenType.R_PAREN) {
                ;
            }
            else {
                TreeNode logOp = parseLogOp();
                logOp.setParent(logExprNode);
                logExprChildren.add(logOp);
                TreeNode ariExpr2 = parseAriExpr();
                ariExpr2.setParent(logExprNode);
                logExprChildren.add(ariExpr2);
            }
        }
        return logExprNode;
    }

    /**
     * 解析逻辑运算符
     * LogOp       > | < | >= |<= | == | !=
     * @return
     * @throws ParserException
     */
    private TreeNode parseLogOp() throws ParserException{
        TreeNode logOpNode = new TreeNode(NodeType.LOG_OP,"logicalOp",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> logOpChildren = logOpNode.getChildren();
        if(getNextTokenType()==TokenType.GREATER){
            consumeNextToken(TokenType.GREATER);
            logOpChildren.add(new TreeNode(NodeType.GREATER,">",logOpNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }else if(getNextTokenType()== TokenType.LESS){
            consumeNextToken(TokenType.LESS);
            logOpChildren.add(new TreeNode(NodeType.LESS,"<",logOpNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }else if(getNextTokenType()== TokenType.LE_EQ){
            consumeNextToken(TokenType.LE_EQ);
            logOpChildren.add(new TreeNode(NodeType.LE_EQ,"<=",logOpNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }else if(getNextTokenType()== TokenType.EQUAL){
            consumeNextToken(TokenType.EQUAL);
            logOpChildren.add(new TreeNode(NodeType.EQUAL,"==",logOpNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }else if(getNextTokenType()== TokenType.GR_EQ){
            consumeNextToken(TokenType.GR_EQ);
            logOpChildren.add(new TreeNode(NodeType.GR_EQ,">=",logOpNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }else if(getNextTokenType()== TokenType.NOT_EQUAL){
            consumeNextToken(TokenType.NOT_EQUAL);
            logOpChildren.add(new TreeNode(NodeType.NOT_EQUAL,"!=",logOpNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }else{
            Token token = getTokenListIterator().next();
            throw new ParserException("Line:"+token.getLineNum()+
                    ", Position:"+token.getPosition()+"; should be logical operator");
        }
        return logOpNode;
    }

    /**
     * 解析变量列表
     * @return
     * @throws ParserException
     */
    private TreeNode parseVarList() throws ParserException{
        TreeNode varListNode = new TreeNode(NodeType.VARLIST,"varList",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> varListNodeChildren = varListNode.getChildren();
        String value = getNextTokenValue();
        consumeNextToken(TokenType.IDENT);
        varListNodeChildren.add(new TreeNode(NodeType.IDENT,value,varListNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        if(getNextTokenType()== TokenType.ASSIGN){
            consumeNextToken(TokenType.ASSIGN);
            varListNodeChildren.add(new TreeNode(NodeType.ASSIGN,"=",varListNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            if(getNextTokenType()==TokenType.L_BRACE) {
            	TreeNode arrValue=parseArrayValue();
            	arrValue.setParent(varListNode);
            	varListNodeChildren.add(arrValue);
            }
            else {
            	TreeNode ariExpr = parseAriExpr();
                ariExpr.setParent(varListNode);
                varListNodeChildren.add(ariExpr);
            }
            
        }
        while(getNextTokenType()==TokenType.COMMA){
            consumeNextToken(TokenType.COMMA);
            varListNodeChildren.add(new TreeNode(NodeType.COMMA,",",varListNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            String nextValue = getNextTokenValue();
            consumeNextToken(TokenType.IDENT);
            varListNodeChildren.add(new TreeNode(NodeType.IDENT,nextValue,varListNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            if(getNextTokenType()== TokenType.ASSIGN){
                consumeNextToken(TokenType.ASSIGN);
                varListNodeChildren.add(new TreeNode(NodeType.ASSIGN,"=",varListNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
                TreeNode ariExpr = parseAriExpr();
                ariExpr.setParent(varListNode);
                varListNodeChildren.add(ariExpr);
            }
        }
        return varListNode;
    }

    /**
     * 解析type语句
     * Type			int ArrayType| double ArrayType  //数组里面可以是表达式
     ArrayType    [AriExpr] ArrayType | null

     * @return
     * @throws ParserException
     */
    private TreeNode parseType() throws ParserException{
        TreeNode typeNode = new TreeNode(NodeType.TYPE,"type",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> typeNodeChildren = typeNode.getChildren();
        if(getNextTokenType()==TokenType.INT){
            consumeNextToken(TokenType.INT);
            typeNodeChildren.add(new TreeNode(NodeType.INT,"int",typeNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }else if(getNextTokenType()==TokenType.DOUBLE){
            consumeNextToken(TokenType.DOUBLE);
            typeNodeChildren.add(new TreeNode(NodeType.DOUBLE,"double",typeNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }else if(getNextTokenType()==TokenType.BOOL) {
            consumeNextToken(TokenType.BOOL);
            typeNodeChildren.add(new TreeNode(NodeType.BOOL, "bool", typeNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }
        TreeNode array = parseArrayType();
        array.setParent(typeNode);
        typeNodeChildren.add(array);
        return typeNode;
    }

    private TreeNode parseArrayType()throws ParserException{
        TreeNode arrNode = new TreeNode(NodeType.ARRAY_TYPE,"arrayNum",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> arrChildren = arrNode.getChildren();
        if(getNextTokenType()==TokenType.L_BRACKET){
            consumeNextToken(TokenType.L_BRACKET);
            arrChildren.add(new TreeNode(NodeType.L_BRACKET,"[",arrNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
            TreeNode ariExpr = parseAriExpr();
            ariExpr.setParent(arrNode);
            arrChildren.add(ariExpr);
            consumeNextToken(TokenType.R_BRACKET);
            arrChildren.add(new TreeNode(NodeType.R_BRACKET,"]",arrNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }else{
            arrChildren.add(new TreeNode(NodeType.NULL,"null",arrNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        }
        return arrNode;
    }

    /**
     * 解析变量声明语句
     * VarDecl			Type VarList;
     * @return
     * @throws ParserException
     */
    private TreeNode parseVarDecl()throws ParserException{
        TreeNode varDeclNode = new TreeNode(NodeType.VAR_DECL,"varDecl",null,getCurrToken().getLineNum(), getCurrToken().getPosition());
        List<TreeNode> varDeclNodeChildren = varDeclNode.getChildren();
        TreeNode type = parseType();
        type.setParent(varDeclNode);
        varDeclNodeChildren.add(type);
        TreeNode varlist = parseVarList();
        varlist.setParent(varDeclNode);
        varDeclNodeChildren.add(varlist);
        consumeNextToken(TokenType.SEMICOLON);
        varDeclNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",varDeclNode,getCurrToken().getLineNum(), getCurrToken().getPosition()));
        return varDeclNode;
    }

 /*
    private TreeNode parseFuncList()throws ParserException{
        TreeNode func_varNode = new TreeNode(NodeType.FUN_VARLIST,"Func_varList",null);
        List<TreeNode> children = func_varNode.getChildren();
        TreeNode type = parseType();
        type.setParent(func_varNode);
        children.add(type);
        TreeNode varlist = parseVarList();
        varlist.setParent(func_varNode);
        children.add(varlist);
        consumeNextToken(TokenType.SEMICOLON);
        children.add(new TreeNode(NodeType.SEMICOLON,";",func_varNode));
        return func_varNode;
    }*/
}
