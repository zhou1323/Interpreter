import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


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
            throw new ParserException("Line:"+token.getLineNum()+
                    ", Position:"+token.getPosition()+"; unexpected token");
        }
    }

    /**
     * 解析program
     * Program 	--> Stmt { Stmt }
     * @return
     * @throws ParserException
     */
    public TreeNode parseProgram() throws ParserException{
        TreeNode progNode = new TreeNode(NodeType.PROGRAM,"program",null);
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
                    ReadStmt | WriteStmt | StmtBlock
     * @return
     * @throws ParserException
     */
    private TreeNode parseStmt() throws ParserException{
        TreeNode stmtNode = new TreeNode(NodeType.STMT,"stmt",null);
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
                	childNode=parseForStmt();
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
                case L_BRACE:
                    childNode = parseStmtBlock();
                    break;
                case INT:
                case DOUBLE:
                case BOOL:
                    childNode = parseVarDecl();
                    break;
                case IDENT:
                    childNode = parseAssignStmt();
                    break;
                case END_OF_DOC:

                default:
                    throw new ParserException("");
            }
            childNode.setParent(stmtNode);
            stmtNodeChildren.add(childNode);
            return stmtNode;
    }

  //ForStmt-> for ( Decl|Assign;LogExpr;var++|var--)Stmt
    private TreeNode parseForStmt() throws ParserException{
        TreeNode forStmtNode = new TreeNode(NodeType.FOR_STMT,"for_stmt",null);
        List<TreeNode> children = forStmtNode.getChildren();
        consumeNextToken(TokenType.FOR);
        consumeNextToken(TokenType.L_PAREN);
        children.add(new TreeNode(NodeType.FOR,"for",forStmtNode));
        children.add(new TreeNode(NodeType.L_PAREN,"(",forStmtNode));
        if(getNextTokenType()==TokenType.IDENT){
            TreeNode expr=parseAssignStmt();  //for(assignStmt
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
        children.add(new TreeNode(NodeType.SEMICOLON,";",forStmtNode));
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
        children.add(new TreeNode(NodeType.R_PAREN,")",forStmtNode));
        TreeNode stmt = parseStmt();
        stmt.setParent(forStmtNode);
        children.add(stmt);


        return forStmtNode;
    }

    private TreeNode parseAddStmt()throws ParserException{
        TreeNode addStmtNode = new TreeNode(NodeType.ADD_STMT,"add_stmt",null);
        List<TreeNode> children = addStmtNode.getChildren();
        TreeNode value = parseValue();    // i
        value.setParent(addStmtNode);
        children.add(value);
        consumeNextToken(TokenType.ASSIGN);
        children.add(new TreeNode(NodeType.ASSIGN,"=",addStmtNode));
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
        TreeNode ifStmtNode = new TreeNode(NodeType.IF_STMT,"if_stmt",null);
        List<TreeNode> ifStmtNodeChildren = ifStmtNode.getChildren();
        consumeNextToken(TokenType.IF);
        consumeNextToken(TokenType.L_PAREN);
        ifStmtNodeChildren.add(new TreeNode(NodeType.IF,"if",ifStmtNode));
        ifStmtNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",ifStmtNode));
        TreeNode expr = parseLogExpr();
        expr.setParent(ifStmtNode);
        ifStmtNodeChildren.add(expr);
        consumeNextToken(TokenType.R_PAREN);
        ifStmtNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",ifStmtNode));
        TreeNode stmt = parseStmt();
        stmt.setParent(ifStmtNode);
        ifStmtNodeChildren.add(stmt);
        if(getNextTokenType()== TokenType.ELSE){
            consumeNextToken(TokenType.ELSE);
            ifStmtNodeChildren.add(new TreeNode(NodeType.ELSE,"else",ifStmtNode));
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
        TreeNode whileStmtNode = new TreeNode(NodeType.WHILE_STMT,"while_stmt",null);
        List<TreeNode> whileStmtNodeChildren = whileStmtNode.getChildren();
        consumeNextToken(TokenType.WHILE);
        whileStmtNodeChildren.add(new TreeNode(NodeType.WHILE,"while",whileStmtNode));
        consumeNextToken(TokenType.L_PAREN);
        whileStmtNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",whileStmtNode));
        TreeNode expr = parseLogExpr();
        expr.setParent(whileStmtNode);
        whileStmtNodeChildren.add(expr);
        consumeNextToken(TokenType.R_PAREN);
        whileStmtNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",whileStmtNode));
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
    private TreeNode parseAssignStmt()throws ParserException{
        TreeNode assignNode = new TreeNode(NodeType.ASSIGN_STMT,"assignStmt",null);
        List<TreeNode> assignNodeChildren = assignNode.getChildren();
        TreeNode value = parseValue();
        value.setParent(assignNode);
        assignNodeChildren.add(value);
        consumeNextToken(TokenType.ASSIGN);
        assignNodeChildren.add(new TreeNode(NodeType.ASSIGN,"=",assignNode));
        TreeNode expr = parseAriExpr();
        expr.setParent(assignNode);
        assignNodeChildren.add(expr);
        consumeNextToken(TokenType.SEMICOLON);
        assignNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",assignNode));
        return assignNode;
    }
    /**
     * 解析break语句
     * BreakStmt		break ;
     * @return TreeNode
     * @throws ParserException
     */
    private TreeNode parseBreakStmt()throws ParserException{
        TreeNode breakStmtNode = new TreeNode(NodeType.BREAK_STMT,"break_stmt",null);
        List<TreeNode> breakStmtNodeChildren = breakStmtNode.getChildren();
        consumeNextToken(TokenType.BREAK);
        consumeNextToken(TokenType.SEMICOLON);
        breakStmtNodeChildren.add(new TreeNode(NodeType.BREAK,"break",breakStmtNode));
        breakStmtNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",breakStmtNode));
        return breakStmtNode;
    }

    /**
     * 解析read语句
     * ReadStmt	 	read (Value);
     * @return TreeNode
     * @throws ParserException
     */
    private TreeNode parseReadStmt()throws ParserException{
        TreeNode readStmtNode = new TreeNode(NodeType.READ_STMT,"read_stmt",null);
        List<TreeNode> readStmtNodeChildren = readStmtNode.getChildren();
        consumeNextToken(TokenType.READ);
        readStmtNodeChildren.add(new TreeNode(NodeType.READ,"read",readStmtNode));
        consumeNextToken(TokenType.L_PAREN);
        readStmtNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",readStmtNode));
        TreeNode value = parseValue();
        value.setParent(readStmtNode);
        readStmtNodeChildren.add(value);
        consumeNextToken(TokenType.R_PAREN);
        readStmtNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",readStmtNode));
        consumeNextToken(TokenType.SEMICOLON);
        readStmtNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",readStmtNode));
        return readStmtNode;
    }

    /**
     * 解析write语句
     * WriteStmt	 	write(AriExpr);
     * @return TreeNode
     * @throws ParserException
     */
    private TreeNode parseWriteStmt()throws ParserException{
        TreeNode writeStmtNode = new TreeNode(NodeType.WRITE_STMT,"write_stmt",null);
        List<TreeNode> writeStmtNodeChildren = writeStmtNode.getChildren();
        consumeNextToken(TokenType.WRITE);
        writeStmtNodeChildren.add(new TreeNode(NodeType.WRITE,"write",writeStmtNode));
        consumeNextToken(TokenType.L_PAREN);
        writeStmtNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",writeStmtNode));
        TreeNode expr = parseAriExpr();
        expr.setParent(writeStmtNode);
        writeStmtNodeChildren.add(expr);
        consumeNextToken(TokenType.R_PAREN);
        writeStmtNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",writeStmtNode));
        consumeNextToken(TokenType.SEMICOLON);
        writeStmtNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",writeStmtNode));
        return writeStmtNode;
    }

    /**
     * 解析语句块
     * @return TreeNode
     * @throws ParserException
     */
    private TreeNode parseStmtBlock() throws ParserException{
        TreeNode stmtBlockNode = new TreeNode(NodeType.STMT_BLOCK,"stmt_block",null);
        List<TreeNode> stmtBlokNodeChildren = stmtBlockNode.getChildren();
        consumeNextToken(TokenType.L_BRACE);
        stmtBlokNodeChildren.add(new TreeNode(NodeType.L_BRACE,"{",stmtBlockNode));
        while(getNextTokenType()!= TokenType.R_BRACE){
            TreeNode block = parseStmt();
            block.setParent(stmtBlockNode);
            stmtBlokNodeChildren.add(block);
        }
        consumeNextToken(TokenType.R_BRACE);
        stmtBlokNodeChildren.add(new TreeNode(NodeType.R_BRACE,"}",stmtBlockNode));
        return stmtBlockNode;
    }

    /**
     * 解析value语句
     * Value --> ident [intconstant] | ident
     * @return
     * @throws ParserException
     */
    private TreeNode parseValue() throws ParserException{
        TreeNode valueNode = new TreeNode(NodeType.VALUE,"value",null);
        List<TreeNode> valueNodeChildren = valueNode.getChildren();
        String value = getNextTokenValue();
        consumeNextToken(TokenType.IDENT);
        valueNodeChildren.add(new TreeNode(NodeType.IDENT,value,valueNode));
        if(getNextTokenType()==TokenType.L_BRACKET){
            consumeNextToken(TokenType.L_BRACKET);
            valueNodeChildren.add(new TreeNode(NodeType.L_BRACKET,"[",valueNode));
            TreeNode ariExpr = parseAriExpr();
            ariExpr.setParent(valueNode);
            valueNodeChildren.add(ariExpr);
            consumeNextToken(TokenType.R_BRACKET);
            valueNodeChildren.add(new TreeNode(NodeType.R_BRACKET,"]",valueNode));
        }
        return valueNode;
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
        TreeNode ariExprNode = new TreeNode(NodeType.ARI_EXPR, "E", null);
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
                ariExprNodeChildren.add(new TreeNode(NodeType.MINUS,"-",ariExprNode));
            }else{
                consumeNextToken(TokenType.PLUS);
                ariExprNodeChildren.add(new TreeNode(NodeType.PLUS,"+",ariExprNode));
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
        TreeNode termNode = new TreeNode(NodeType.TERM,"T",null);
        List<TreeNode> termNodeChildren = termNode.getChildren();
        TreeNode factor = parseFactor();
        factor.setParent(termNode);
        termNodeChildren.add(factor);
        while(getNextTokenType()==TokenType.MOD||getNextTokenType()==TokenType.DIV||getNextTokenType()==TokenType.MULTI){
            if(getNextTokenType()==TokenType.MOD){
                consumeNextToken(TokenType.MOD);
                termNodeChildren.add(new TreeNode(NodeType.MOD,"%",termNode));
            }else if(getNextTokenType()==TokenType.DIV){
                consumeNextToken(TokenType.DIV);
                termNodeChildren.add(new TreeNode(NodeType.DIV,"/",termNode));
            }else{
                consumeNextToken(TokenType.MULTI);
                termNodeChildren.add(new TreeNode(NodeType.MULTI,"*",termNode));
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
        TreeNode factorNode = new TreeNode(NodeType.FACTOR,"F",null);
        List<TreeNode> facNodeChildren = factorNode.getChildren();
        switch (getNextTokenType()){
	        case TRUE:
	            consumeNextToken(TokenType.TRUE);
	            facNodeChildren.add(new TreeNode(NodeType.TRUE,"true",factorNode));
	            break;
	        case FALSE:
	            consumeNextToken(TokenType.FALSE);
	            facNodeChildren.add(new TreeNode(NodeType.FALSE,"false",factorNode));
	            break;
            
        	case IDENT:
                TreeNode value = parseValue();
                value.setParent(factorNode);
                facNodeChildren.add(value);
                break;
            
            case INT_NUM:
                String intvalue = getNextTokenValue();
                consumeNextToken(TokenType.INT_NUM);
                facNodeChildren.add(new TreeNode(NodeType.INT_NUM,intvalue,factorNode));
                break;
            case DOUBLE_NUM:
                String douvalue = getNextTokenValue();
                consumeNextToken(TokenType.DOUBLE_NUM);
                facNodeChildren.add(new TreeNode(NodeType.DOUBLE_NUM,douvalue,factorNode));
                break;
            case L_PAREN:
                consumeNextToken(TokenType.L_PAREN);
                facNodeChildren.add(new TreeNode(NodeType.L_PAREN,"(",factorNode));
                TreeNode ariExpr = parseAriExpr();
                ariExpr.setParent(factorNode);
                facNodeChildren.add(ariExpr);
                consumeNextToken(TokenType.R_PAREN);
                facNodeChildren.add(new TreeNode(NodeType.R_PAREN,")",factorNode));
                break;
            case MINUS:
                consumeNextToken(TokenType.MINUS);
                facNodeChildren.add(new TreeNode(NodeType.MINUS,"-",factorNode));
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
        TreeNode logExprNode = new TreeNode(NodeType.LOG_EXPR,"logicalExpr",null);
        List<TreeNode> logExprChildren = logExprNode.getChildren();
        if(getNextTokenType()==TokenType.TRUE){
            consumeNextToken(TokenType.TRUE);
            logExprChildren.add(new TreeNode(NodeType.TRUE,"true",logExprNode));
        }else if(getNextTokenType()==TokenType.FALSE){
            consumeNextToken(TokenType.FALSE);
            logExprChildren.add(new TreeNode(NodeType.FALSE,"false",logExprNode));
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
        TreeNode logOpNode = new TreeNode(NodeType.LOG_OP,"logicalOp",null);
        List<TreeNode> logOpChildren = logOpNode.getChildren();
        if(getNextTokenType()==TokenType.GREATER){
            consumeNextToken(TokenType.GREATER);
            logOpChildren.add(new TreeNode(NodeType.GREATER,">",logOpNode));
        }else if(getNextTokenType()== TokenType.LESS){
            consumeNextToken(TokenType.LESS);
            logOpChildren.add(new TreeNode(NodeType.LESS,"<",logOpNode));
        }else if(getNextTokenType()== TokenType.LE_EQ){
            consumeNextToken(TokenType.LE_EQ);
            logOpChildren.add(new TreeNode(NodeType.LE_EQ,"<=",logOpNode));
        }else if(getNextTokenType()== TokenType.EQUAL){
            consumeNextToken(TokenType.EQUAL);
            logOpChildren.add(new TreeNode(NodeType.EQUAL,"==",logOpNode));
        }else if(getNextTokenType()== TokenType.GR_EQ){
            consumeNextToken(TokenType.GR_EQ);
            logOpChildren.add(new TreeNode(NodeType.GR_EQ,">=",logOpNode));
        }else if(getNextTokenType()== TokenType.NOT_EQUAL){
            consumeNextToken(TokenType.NOT_EQUAL);
            logOpChildren.add(new TreeNode(NodeType.NOT_EQUAL,"!=",logOpNode));
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
        TreeNode varListNode = new TreeNode(NodeType.VARLIST,"varList",null);
        List<TreeNode> varListNodeChildren = varListNode.getChildren();
        String value = getNextTokenValue();
        consumeNextToken(TokenType.IDENT);
        varListNodeChildren.add(new TreeNode(NodeType.IDENT,value,varListNode));
        if(getNextTokenType()== TokenType.ASSIGN){
            consumeNextToken(TokenType.ASSIGN);
            varListNodeChildren.add(new TreeNode(NodeType.ASSIGN,"=",varListNode));
            TreeNode ariExpr = parseAriExpr();
            ariExpr.setParent(varListNode);
            varListNodeChildren.add(ariExpr);
        }
        while(getNextTokenType()==TokenType.COMMA){
            consumeNextToken(TokenType.COMMA);
            varListNodeChildren.add(new TreeNode(NodeType.COMMA,",",varListNode));
            String nextValue = getNextTokenValue();
            consumeNextToken(TokenType.IDENT);
            varListNodeChildren.add(new TreeNode(NodeType.IDENT,nextValue,varListNode));
            if(getNextTokenType()== TokenType.ASSIGN){
                consumeNextToken(TokenType.ASSIGN);
                varListNodeChildren.add(new TreeNode(NodeType.ASSIGN,"=",varListNode));
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
        TreeNode typeNode = new TreeNode(NodeType.TYPE,"type",null);
        List<TreeNode> typeNodeChildren = typeNode.getChildren();
        if(getNextTokenType()==TokenType.INT){
            consumeNextToken(TokenType.INT);
            typeNodeChildren.add(new TreeNode(NodeType.INT,"int",typeNode));
        }else if(getNextTokenType()==TokenType.DOUBLE){
            consumeNextToken(TokenType.DOUBLE);
            typeNodeChildren.add(new TreeNode(NodeType.DOUBLE,"double",typeNode));
        }else if(getNextTokenType()==TokenType.BOOL) {
            consumeNextToken(TokenType.BOOL);
            typeNodeChildren.add(new TreeNode(NodeType.BOOL, "bool", typeNode));
        }
        TreeNode array = parseArrayType();
        array.setParent(typeNode);
        typeNodeChildren.add(array);
        return typeNode;
    }

    private TreeNode parseArrayType()throws ParserException{
        TreeNode arrNode = new TreeNode(NodeType.ARRAY_TYPE,"arrayNum",null);
        List<TreeNode> arrChildren = arrNode.getChildren();
        if(getNextTokenType()==TokenType.L_BRACKET){
            consumeNextToken(TokenType.L_BRACKET);
            arrChildren.add(new TreeNode(NodeType.L_BRACKET,"[",arrNode));
            TreeNode ariExpr = parseAriExpr();
            ariExpr.setParent(arrNode);
            arrChildren.add(ariExpr);
            consumeNextToken(TokenType.R_BRACKET);
            arrChildren.add(new TreeNode(NodeType.R_BRACKET,"]",arrNode));
        }else{
            arrChildren.add(new TreeNode(NodeType.NULL,"null",arrNode));
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
        TreeNode varDeclNode = new TreeNode(NodeType.VAR_DECL,"varDecl",null);
        List<TreeNode> varDeclNodeChildren = varDeclNode.getChildren();
        TreeNode type = parseType();
        type.setParent(varDeclNode);
        varDeclNodeChildren.add(type);
        TreeNode varlist = parseVarList();
        varlist.setParent(varDeclNode);
        varDeclNodeChildren.add(varlist);
        consumeNextToken(TokenType.SEMICOLON);
        varDeclNodeChildren.add(new TreeNode(NodeType.SEMICOLON,";",varDeclNode));
        return varDeclNode;
    }

}
