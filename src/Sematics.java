import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.sun.corba.se.impl.orbutil.graph.Node;
import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import sun.reflect.generics.tree.Tree;

public class Sematics {
	private SymbolTable table;
	private String errMsg;
	private int scale = 0;

	public Sematics(Parser parser) throws Exception {
		TreeNode root = parser.parseProgram();
		this.table = new SymbolTable();
		travel(root);
	}

	public SymbolTable getTable() {
		return table;
	}

	private static boolean matchInt(String input) {
		if (input.matches("^-?\\d+$") && !input.matches("^-?0{1,}\\d+$"))
			return true;
		else
			return false;
	}

	private static boolean matchDouble(String input) {
		if (input.matches("^(-?\\d+)(\\.\\d+)+$") && !input.matches("^(-?0{2,}+)(\\.\\d+)+$"))
			return true;
		else
			return false;
	}

	public void travel(TreeNode treeNode) throws Exception {
		if (!treeNode.getChildren().isEmpty()) {
			for (TreeNode t : treeNode.getChildren()) {
				stmt(t);
			}
		}
	}

	private static int isInLoop = 0;

	public void stmt(TreeNode t) throws Exception {
		TreeNode temp = t.getChildren().get(0);
		if (temp.getNodeType().equals(NodeType.VAR_DECL)) {
			decl_stmt(temp);
		} else if (temp.getNodeType().equals(NodeType.ASSIGN_STMT)) {
			assign_stmt(temp);
		} else if (temp.getNodeType().equals(NodeType.IF_STMT)) {
			scale++;
			if_stmt(temp);
			scale--;
		} else if (temp.getNodeType().equals(NodeType.FOR_STMT)) {
			scale++;
			for_stmt(temp);
			scale--;
		} else if (temp.getNodeType().equals(NodeType.WHILE_STMT)) {
			scale++;
			while_stmt(temp);
			scale--;
		} /*
			 * else if(t.getNodeType().equals(NodeType.STMT_BLOCK)){ for(TreeNode
			 * child:t.getChildren()){ travel(child); } }
			 */
		else if (temp.getNodeType().equals(NodeType.READ_STMT)) {
			read_stmt(temp);
		}else if (temp.getNodeType().equals(NodeType.WRITE_STMT)) {
			write_stmt(temp);
		}else if (temp.getNodeType().equals(NodeType.STMT_BLOCK)) {
			block_stmt(temp);
		} else if (temp.getNodeType().equals(NodeType.BREAK_STMT)) {
			if (isInLoop > 0) {
				break_stmt(temp);
			} else {
				errMsg = "break只能用在循环语句中";
				System.out.println(errMsg);
			}
		}
	}

	// 表示在第几层循环中，0表示不再循环中
	private void break_stmt(TreeNode breakNode) {

	}

	private static int isbreak = 0;

	public void block_stmt(TreeNode t) throws Exception {
		if (!t.getChildren().isEmpty()) {
			for (int i = 1; i < t.getChildren().size() - 1; i++) {
				if (t.getNthChild(i).getFirstChild().getNodeType() == NodeType.BREAK_STMT && isInLoop > 0) {
					TreeNode sorceStmt = t.getParent().getParent();
					if (sorceStmt.getNodeType() == NodeType.WHILE_STMT
							|| sorceStmt.getNodeType() == NodeType.FOR_STMT) {
						isbreak++;
						break;
					} else if (sorceStmt.getNodeType() == NodeType.IF_STMT) {
						isbreak++; // if_stmt-->stmt-->stmt_block-->while_stmt
						TreeNode stmt = sorceStmt.getParent();
						TreeNode loopNode = sorceStmt.getParent().getParent();
						int index = loopNode.getChildren().indexOf(stmt);
						for (int j = index + 1; j < loopNode.getChildren().size() - 1; j++) {
							loopNode.getChildren().remove(j);
						}
						break;
					}

				}
				stmt(t.getNthChild(i));
			}
		}
	}

//	public boolean compare(TreeNode left,TreeNode right){
//
//	}

	public boolean judgeCondition(TreeNode conditionNode) throws Exception {
		if (conditionNode.getChildren().size() > 1) {
			double left, right;
			TreeNode leftNode = conditionNode.getFirstChild();
			TreeNode logicOp = conditionNode.getNthChild(1);
			TreeNode rightNode = conditionNode.getNthChild(2);
			String leftValue = calAriExpr(leftNode).getValue();
			String rightValue = calAriExpr(rightNode).getValue();
			if (leftNode.getNodeType() == NodeType.INT_NUM) {
				left = (double)Integer.parseInt(leftValue);
				if (rightNode.getNodeType() == NodeType.INT_NUM) {
					right = (double)Integer.parseInt(rightValue);
				} else {
					right = Double.parseDouble(rightValue);
				}
			} else {
				left = Double.parseDouble(leftValue);
				if (rightNode.getNodeType() == NodeType.INT_NUM) {
					right = (double)Integer.parseInt(rightValue);
				} else {
					right = Double.parseDouble(rightValue);
				}
			}
			switch (logicOp.getFirstChild().getNodeType()) {
				case EQUAL:
					return (left == right);
				case NOT_EQUAL:
					return (left != right);
				case LE_EQ:
					return (left <= right);
				case GR_EQ:
					return (left >= right);
				case GREATER:
					return (left > right);
				case LESS:
					return (left < right);
				default:
					break;
			}
		} else {
			TreeNode boolNode = calAriExpr(conditionNode.getFirstChild());
			if (boolNode.getNodeType() == NodeType.BOOL) {
				if(boolNode.getValue().equals("false"))
					return false;
				else if (boolNode.getValue().equals("true")) {
					return true;
				}
			}
			else if(boolNode.getNodeType()==NodeType.INT_NUM) {
				return Integer.parseInt(boolNode.getValue())!=0;
			}
			else if(boolNode.getNodeType()==NodeType.DOUBLE_NUM) {
				return Double.parseDouble(boolNode.getValue())!=0;
			}

		}
		return false;
	}

	public void if_stmt(TreeNode t) throws Exception {
		if (!t.getChildren().isEmpty()) {

			TreeNode conditionNode = t.getChildren().get(2);
			int childNumber = t.getChildren().size();
			TreeNode ifStmtNode = t.getNthChild(4);
			// if(conditionNode)
			boolean c = judgeCondition(conditionNode);
			if (c) {
				stmt(ifStmtNode);
			} else{
				if (childNumber == 7) {
					TreeNode elseStmtNode = t.getChildren().get(6);
					stmt(elseStmtNode);
				}
			}
		}
	}

	public boolean isFirst=true;

	public void for_stmt(TreeNode t) throws Exception {
		if (!t.getChildren().isEmpty()) {
			TreeNode assignNode = t.getChildren().get(2);

			TreeNode conditionNode = t.getChildren().get(3);
			TreeNode addNode = t.getChildren().get(5);
			TreeNode forStmtNode = t.getChildren().get(7);
			if (assignNode.getChildren().size() == 4 || assignNode.getChildren().size()==3) {
				if(assignNode.getChildren().size() == 4)
					assign_stmt(assignNode);
				else
					decl_stmt(assignNode);
				boolean c = judgeCondition(conditionNode);
				isInLoop++;
				scale++;
				while (c) {
					stmt(forStmtNode);
					if(isbreak>0)
						break;
					if(isFirst) {
						isFirst=false;
					}
					else {
						table.removeAtLevel(scale);
					}
					assign_stmt(addNode);
					c = judgeCondition(conditionNode);
				}
				scale--;
				isInLoop--;
				isbreak--;

			} else {
				System.err.println("The operation on variable is invalid!");
			}
		}
	}

	public void while_stmt(TreeNode t) throws Exception {
		if (!t.getChildren().isEmpty()) {
			TreeNode conditionNode = t.getNthChild(2);
			boolean istrue = judgeCondition(conditionNode);
			TreeNode whileStmtNode = t.getChildren().get(4);
			// TreeNode stmtBlockNode=whileStmtNode.getFirstChild();
			// boolean c=judgeCondition(conditionNode);
			isInLoop++;

			while (istrue) {
				if (isbreak > 0)
					break;
				if(isFirst) {
					isFirst=false;
				}
				else {
					table.removeAtLevel(scale);
				}
				stmt(whileStmtNode);
				istrue = judgeCondition(conditionNode);
			}
			isInLoop--;
			isbreak--;
		}
	}

	public void read_stmt(TreeNode t) throws Exception {
		if (!t.getChildren().isEmpty()) {

			TreeNode variable = t.getChildren().get(2); // value

			if (variable.getNodeType() == NodeType.VALUE) {

				if (table.contains(variable.getFirstChild().getValue())) {
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					String str = null;
					System.out.println("Enter your value:");
					str = br.readLine();
					// 变量已经被声明
					if (variable.getChildren().size() == 1) { // 输入变量
						TreeNode v = variable.getFirstChild(); // 变量

						if (((table.get(v.getValue()).getType().equals("double")) && matchDouble(str))
								|| (table.get(v.getValue()).getType().equals("int")) && matchInt(str)) {
							List<Object> pre = table.get(v.getValue()).getValue();
							pre.add(0, str);
							Symbol symbol = table.get(v.getValue());
							symbol.setName(v.getValue());
							symbol.setType(table.get(v.getValue()).getType());
							symbol.setValue(pre);
							table.put(symbol.getName(), symbol);
						}else if((table.get(v.getValue()).getType().equals("double")) && matchInt(str)){
							List<Object> pre = table.get(v.getValue()).getValue();
							Double strDou=Double.parseDouble(str);
							String s=strDou.toString();
							pre.add(0, s);
							Symbol symbol = table.get(v.getValue());
							symbol.setName(v.getValue());
							symbol.setType(table.get(v.getValue()).getType());
							symbol.setValue(pre);
							table.put(symbol.getName(), symbol);
						}

						else {
							System.err.println("You input value must match with the identifier!");
						}
					} else if (variable.getChildren().size() == 4) {
						TreeNode v1 = variable.getFirstChild();
						TreeNode eindex = variable.getChildren().get(2);
						TreeNode tindex = eindex.getFirstChild();
						TreeNode findex = tindex.getFirstChild();
						String index = findex.getFirstChild().getValue();
						int i = Integer.parseInt(index);
						if (((table.get(v1.getValue()).getType().equals("double")) && matchDouble(str))
								|| (table.get(v1.getValue()).getType().equals("int")) && matchInt(str)) {
							List<Object> pre = table.get(v1.getValue()).getValue();
							pre.set(i, str);
							Symbol symbol = table.get(v1.getValue());
							symbol.setName(v1.getValue());
							symbol.setType(table.get(v1.getValue()).getType());
							symbol.setValue(pre);
							table.put(symbol.getName(), symbol);
						}

						else {
							System.err.println("You input value must match with the identifier!");
						}
					}
				} else {
					System.err.println("The identifier has not be declared!");
				}
			} else {
				System.err.println("The read statement is invalid!");
			}
		}
	}

	public void write_stmt(TreeNode t) throws Exception {
		if (t.getChildren().size()>0) {
			TreeNode variable = t.getChildren().get(2);
			if (variable.getNodeType() == NodeType.ARI_EXPR) {
				String s=calAriExpr(variable).getValue();
				System.out.println(calAriExpr(variable).getValue());
			} else {
				System.err.println("The read statement is invalid!");
			}
		}
	}

	public void decl_stmt(TreeNode t) {
		if (!t.getChildren().isEmpty()) {
			Symbol symbol = new Symbol();
			String type;
			int arrayIndex;

			for (TreeNode temp : t.getChildren()) {

				if (temp.getNodeType().equals(NodeType.TYPE)) {
					TreeNode typeNode = temp.getChildren().get(0);
					TreeNode arrayNode = temp.getChildren().get(1);

					type = typeNode.getValue();
					symbol.setType(type);
					symbol.setScale(scale);

					List<Object> iniValue = new ArrayList<Object>();
					if(type.equals("double")) {
						iniValue.add("0.0");
					}
					else if(type.equals("int")) {
						iniValue.add(0);
					}
					// 数组情况
					if (arrayNode.getChildren().size() > 1) {
						TreeNode exprNode = arrayNode.getChildren().get(1);
						TreeNode factor = calAriExpr(exprNode);
						arrayIndex = Integer.parseInt(factor.getValue());
						for (int i = 0; i < arrayIndex - 1; i++) {
							if(type.equals("double")) {
								iniValue.add(i,"0.0");
							}
							else if(type.equals("int")) {
								iniValue.add(i,0);
							}
						}
						symbol.setArrayIndex(arrayIndex);
					}
					symbol.setValue(iniValue);
				} else if (temp.getNodeType().equals(NodeType.VARLIST)) {
					for (int i = 0; i < temp.getChildren().size(); i++) {
						TreeNode node = temp.getChildren().get(i);
						if (node.getNodeType().equals(NodeType.IDENT)) {
							String name = node.getValue();

							// 符号表中存在该符号 并且 当前函数未超出该符号的作用域
							if (table.contains(name) && table.get(name).getScale() == scale) {
								errMsg = "变量" + name + "已存在!";
								System.out.println(errMsg);
								return;
							}

							else if (!table.contains(name)
									|| ( (table.contains(name) && 
											(table.get(name).getScale() != scale||table.get(name).getType()!=symbol.getType())
											)
											)
									) {
								symbol.setName(name);
							}

							// 声明时赋值
							if (i + 1 >= temp.getChildren().size()) {
								table.put(symbol.getName(), symbol);
								symbol = new Symbol(symbol.getType(), null, scale, null, symbol.getArrayIndex());
								return;
							}
							TreeNode nextSibling = temp.getChildren().get(i + 1);
							if (nextSibling.getNodeType().equals(NodeType.ASSIGN)) {
								TreeNode exprNode = temp.getChildren().get(i + 2);
								TreeNode assignValue = calAriExpr(exprNode);
								String IDType = getIDType(assignValue);
								if (symbol.getType().equals(IDType)) {
									symbol.setValue(matchType(assignValue));
								}
								else if(symbol.getType().equals("double")&&assignValue.getNodeType()==NodeType.INT_NUM){
									symbol.setValue(matchType(assignValue));
								}
								else {
									errMsg = "变量类型错误!";
									System.out.println(errMsg);
									return;
								}
								i += 2;
							}
							table.put(symbol.getName(), symbol);
							symbol = new Symbol(symbol.getType(), null, 0, symbol.getValue(), symbol.getArrayIndex());
						}

					}
				}
			}
		}
	}

	public void assign_stmt(TreeNode t) {
		if (!t.getChildren().isEmpty()) {
			Symbol symbol = new Symbol();
			int sub = 0;

			for (TreeNode child : t.getChildren()) {
				if (child.getNodeType().equals(NodeType.VALUE)) {
					TreeNode value = child.getFirstChild();
					String variable = value.getValue();

					if (!table.contains(variable)
							|| ( (table.contains(variable) && table.get(variable).getScale() > scale)) ) {
						errMsg = "变量"+variable+"未声明!";
						System.out.println(errMsg);
						return;
					}

					Symbol temp = table.get(variable);
					symbol = temp;

					// 数组
					if (child.getChildren().size() > 1) {
						TreeNode index = child.getChildren().get(2);

						if (!calAriExpr(index).getNodeType().equals(NodeType.INT_NUM)) {
							errMsg = "数组长度必须为整数!";
							System.out.println(errMsg);
							return;
						}

						// 下标
						sub = Integer.parseInt(calAriExpr(index).getValue());
						if (sub > temp.getArrayIndex()) {
							errMsg = "数组长度大于声明长度!";
							System.out.println(errMsg);
							return;
						}

					}
				} else if (child.getNodeType().equals(NodeType.ARI_EXPR)) {
					// TreeNode assignFactor=readExpr(child);
					TreeNode factor = calAriExpr(child);
					String symbolType = getIDType(factor);

					if (symbol.getType().equals(symbolType)||((symbol.getType().equals("double"))&&symbolType.equals("int"))) {
						if (symbol.getArrayIndex() == 0) {
							//symbol.setValue(matchType(factor));
							if(symbol.getType().equals("double")&&factor.getNodeType()==NodeType.INT_NUM){
								String s=factor.getValue();
								Double d=Double.parseDouble(s);
								List<Object> lo=symbol.getValue();
								lo.set(0,d);
								symbol.setValue(lo);
							}else{
								symbol.setValue(matchType(factor));
							}

							
						} 
						//数组情况
						else {
							List<Object> temp = symbol.getValue();
							if (sub >= 0 && sub < temp.size()) {
								Object value=matchType(factor).get(0);
								if(symbol.getType().equals("double")) {
									value=Double.parseDouble((String)value);
								}
								temp.set(sub, value);
								symbol.setValue(temp);
							} else {
								errMsg = "数组越界!";
								System.out.println(errMsg);
								return;
							}
						}
						table.put(symbol.getName(), symbol);
					} else {
						errMsg = "变量类型错误!";
						System.out.println(errMsg);
						return;
					}
				}
			}
		}
	}

	// 符号表中存储的变量和节点变量类型相同时,返回节点的值.
	public List<Object> matchType(TreeNode factor) {
		List<Object> result = new ArrayList<Object>();
		// 通过参数赋值
		if (factor.getNodeType().equals(NodeType.IDENT)) {
			List<Object> value = table.get(factor.getValue()).getValue();
			result = value;
		}
		// 直接赋值
		else
			result.add(factor.getValue());

		return result;
	}

	// 获得identify节点的内容的节点类型
	public String getIDType(TreeNode t) {
		NodeType type = t.getNodeType();
		String symbolType = null;
		if (type.equals(NodeType.INT_NUM)) {
			symbolType = "int";
		} else if (type.equals(NodeType.DOUBLE_NUM)) {
			symbolType = "double";
		} else if (type.equals(NodeType.BOOL)) {
			symbolType = "bool";
		} else if (type.equals(NodeType.IDENT)) {
			if ((table.contains(t.getValue()) && table.get(t.getValue()).getScale() <= scale)) {
				symbolType = table.get(t.getValue()).getType();
			} else {
				errMsg = "变量" + t.getValue() + "不存在!";
				System.out.println(errMsg);
			}
		}
		return symbolType;
	}

	static private boolean isInt = true;
	static private boolean isBool = false;
	static private boolean isCalEnd=false;
	// 计算算术表达式的值
	public TreeNode calAriExpr(TreeNode t) {
		if (isInt) {
			isInt = true;
		}
		else {
			if(isCalEnd) {
				isInt=true;
			}
		}
		isBool = false;
		isCalEnd=false;
		String result = calTerm(t.getNthChild(0)); // 第一个子节点一定是T
		if (t.getChildren().size() > 1) { // E --> T (+/- T)+ 有多个T
			String operand = calTerm(t.getNthChild(2));// 第二个T
			int termNum = t.getTypeChildNum(NodeType.TERM);
			if (isInt) {
				int o1, o2;
				for (int i = 0; i < termNum; i++) {
					if (i < (t.getChildren().size() - 1) / 2) {
						operand = calTerm(t.getNthChild(2 * i + 2));
						o1 = Integer.parseInt(result);
						o2 = Integer.parseInt(operand);
						if (t.getNthChild(2 * i + 1).getNodeType() == NodeType.PLUS) {
							result = String.valueOf(o1 + o2);
						} else if (t.getNthChild(2 * i + 1).getNodeType() == NodeType.MINUS) {
							result = String.valueOf(o1 - o2);
						}
					}
				}
			} else {
				double do1, do2;
				for (int i = 0; i < termNum; i++) {
					if (i < (t.getChildren().size() - 1) / 2) {
						operand = calTerm(t.getNthChild(2 * i + 2));
						do1 = Double.parseDouble(result);
						do2 = Double.parseDouble(operand);
						if (t.getNthChild(2 * i + 1).getNodeType() == NodeType.PLUS) {
							result = String.valueOf(do1 + do2);
						} else if (t.getNthChild(2 * i + 1).getNodeType() == NodeType.MINUS) {
							result = String.valueOf(do1 - do2);
						}
					}
				}
			}
		}
		NodeType nodeType;
		if (isInt) {
			nodeType = NodeType.INT_NUM;
		} else {
			nodeType = NodeType.DOUBLE_NUM;
		}

		if (isBool) {
			nodeType = NodeType.BOOL;
		}

		isCalEnd=true;
		return new TreeNode(nodeType, result, t.getParent());
	}

	public String calTerm(TreeNode t) {
		String result = calFactor(t.getNthChild(0));
		if (t.getChildren().size() > 1) {
			String operand = calFactor(t.getNthChild(2));
			int factorNum = t.getTypeChildNum(NodeType.FACTOR);
			if (isInt) {
				int o1, o2;
				for (int i = 0; i < factorNum; i++) {
					if (i < (t.getChildren().size() - 1) / 2) {
						operand = calFactor(t.getNthChild(2 * i + 2));

						o1 = Integer.parseInt(result);
						o2 = Integer.parseInt(operand);
						if (t.getNthChild(2 * i + 1).getNodeType() == NodeType.MULTI) {
							result = String.valueOf(o1 * o2);
						} else if (t.getNthChild(2 * i + 1).getNodeType() == NodeType.DIV) {
							result = String.valueOf(o1 / o2);
						} else if (t.getNthChild(2 * i + 1).getNodeType() == NodeType.MOD) {
							result = String.valueOf(o1 % o2);
						}
					}
				}
			} else {
				double do1, do2;
				for (int i = 0; i < factorNum; i++) {
					if (i < (t.getChildren().size() - 1) / 2) {
						operand = calFactor(t.getNthChild(2 * i + 2));
						do1 = Double.parseDouble(result);
						do2 = Double.parseDouble(operand);
						if (t.getNthChild(2 * i + 1).getNodeType() == NodeType.MULTI) {
							result = String.valueOf(do1 * do2);
						} else if (t.getNthChild(2 * i + 1).getNodeType() == NodeType.DIV) {
							result = String.valueOf(do1 / do2);
						} else if (t.getNthChild(2 * i + 1).getNodeType() == NodeType.MOD) {
							result = String.valueOf(do1 % do2);
						}
					}
				}
			}
		}
		return result;
	}

	public String calFactor(TreeNode t) {
		int childnum = t.getChildren().size();
		if (childnum == 1) { // F --> INT_NUM||DOUBLE_NUM||VALUE||FALSE||TRUE
			switch (t.getFirstChild().getNodeType()) {
				case TRUE:
				case FALSE:
					isBool = true;
					return t.getFirstChild().getValue();
				case INT_NUM:
					return t.getFirstChild().getValue();
				case DOUBLE_NUM:
					isInt = false;
					return t.getFirstChild().getValue();

				case VALUE:
					return calValue(t.getFirstChild());

			}
		} else if (childnum == 2) { // F --> -F
			String value = calFactor(t.getNthChild(1));
			if (isInt) {
				int e = Integer.parseInt(value);
				return String.valueOf(0 - e);
			} else {
				double d = Double.parseDouble(value);
				return String.valueOf(0 - d);
			}
		} else if (childnum == 3) { // F -> (E)
			return calAriExpr(t.getNthChild(1)).getValue();
		}
		return null;
	}

	private String calValue(TreeNode t) {
		int childnum = t.getChildren().size();
		String id = t.getFirstChild().getValue();
		if (childnum == 1) { // VALUE --> ID
			if (table.contains(id) && table.get(id).getScale() <= scale) {
				if(table.get(id).getType().equals("bool")) {
					isBool=true;
				}else if (table.get(id).getType().equals("double")) {
					isInt=false;
				}
				return table.get(id).getValue().get(0).toString();
			} else {
				errMsg = "变量"+id+"未声明";
				System.out.println(errMsg);
				return null;
			}
		} else { // VALUE --> ID[E]
			int arrnum = (int) (Double.parseDouble(calAriExpr(t.getNthChild(2)).getValue()));
			if (table.contains(id) && table.get(id).getScale() <= scale) {
				if(table.get(id).getType().equals("bool")) {
					isBool=true;
				}else if (table.get(id).getType().equals("double")) {
					isInt=false;
				}
				return table.get(id).getValue().get(arrnum ).toString();
			} else {
				errMsg = "数组未声明";
				System.out.println(errMsg);
				return null;
			}
		}
	}
}
