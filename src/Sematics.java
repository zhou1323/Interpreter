import java.io.FileNotFoundException;
import java.io.IOException;
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

	public void travel(TreeNode treeNode) throws Exception {
		if (!treeNode.getChildren().isEmpty()) {
			for (TreeNode t : treeNode.getChildren()) {
				if (t.getValue().equals("{") || t.getValue().equals("}")) {
					continue;
				}

				TreeNode temp = t.getChildren().get(0);
				scale++;
				if (temp.getNodeType().equals(NodeType.VAR_DECL)) {
					decl_stmt(temp);
				} else if (temp.getNodeType().equals(NodeType.ASSIGN_STMT)) {
					assign_stmt(temp);
				} else if (temp.getNodeType().equals(NodeType.IF_STMT)) {
					if_stmt(t);
				} else if (temp.getNodeType().equals(NodeType.FOR_STMT)) {
					for_stmt(t);
				} else if (temp.getNodeType().equals(NodeType.WHILE_STMT)) {
					while_stmt(t);
				} else if (t.getNodeType().equals(NodeType.STMT_BLOCK)) {
					for (TreeNode child : t.getChildren()) {
						travel(child);
					}
				} else if (temp.getNodeType().equals(NodeType.READ_STMT)) {
					read_stmt(temp);
				}
				scale--;
			}
		}
	}

	public void block_stmt(TreeNode t) throws Exception {
		if (!t.getChildren().isEmpty()) {
			TreeNode temp = t.getFirstChild();
			travel(temp);
		}
	}

	public boolean judgeCondition(TreeNode t) throws Exception {
		TreeNode eChild = t.getFirstChild(); // 判断条件左边
		TreeNode leftOperand = calAriExpr(eChild);
		NodeType type = leftOperand.getNodeType();
		String value = leftOperand.getValue();

		// 条件是变量
		if (t.getChildren().size() == 1) {

			if (((type.equals(NodeType.INT_NUM)) || (type.equals(NodeType.DOUBLE_NUM))) // if(a),a不为布尔变量
					&& (t.getChildren().size() == 1)) {
				System.err.println("This kind of identifier cannot be used in the if condition!");
			} else if (type.equals(NodeType.BOOL)) {
				if (value.equals("true"))
					return true; // if(a),a为true
				else
					return false;

			}
		}
			// 条件是逻辑表达式
			else if (t.getChildren().size() == 3) {
				TreeNode e1Child = t.getChildren().get(2);
				TreeNode rightOperand = calAriExpr(e1Child);

				String e1 = leftOperand.getValue();
				if (e1 == null) {
					throw new Exception("The identifier has not been assigned a value!");
				}
				double v1 = Double.parseDouble(e1);

				String e2 = rightOperand.getValue();
				if (e2 == null) {
					throw new Exception("The identifier has not been assigned a value!");
				}
				double v2 = Double.parseDouble(e2);

				TreeNode logOp = t.getChildren().get(1);
				switch (logOp.getChildren().get(0).getNodeType()) {
				case EQUAL:
					if (v1 == v2)
						return true;
					else
						return false;
				case NOT_EQUAL:
					if (v1 != v2)
						return true;
					else
						return false;
				case LESS:
					if (v1 < v2)
						return true;
					else
						return false;
				case GREATER:
					if (v1 > v2)
						return true;
					else
						return false;
				case LE_EQ:
					if (v1 <= v2)
						return true;
					else
						return false;
				case GR_EQ:
					if (v1 >= v2)
						return true;
					else
						return false;
				default:
					System.err.println("Here should be a logical operator!");
				}
			
		}
		return false;
	}

	public void for_stmt(TreeNode t) throws Exception {
		if (!t.getChildren().isEmpty()) {
			TreeNode temp = t.getFirstChild();
			TreeNode assignNode = temp.getChildren().get(2);

			// TreeNode declOrAssignNode=temp.getChildren().get(1);
			TreeNode conditionNode = temp.getChildren().get(3);
			TreeNode addNode = temp.getChildren().get(5);
			TreeNode forStmtNode = temp.getChildren().get(7);
			if (assignNode.getChildren().size() == 4) {
				assign_stmt(assignNode);
			} else if (assignNode.getChildren().size() == 3) {
				decl_stmt(assignNode);
			}
			boolean c = judgeCondition(conditionNode);
			while (c) {
				block_stmt(forStmtNode);
				assign_stmt(addNode.getChildren().get(0));
			}
		}
		System.out.println("ddd");
	}

	public void if_stmt(TreeNode t) throws Exception {
		if (!t.getChildren().isEmpty()) {
			TreeNode temp = t.getFirstChild();
			int childNumber = temp.getChildren().size();
			TreeNode conditionNode = temp.getChildren().get(2);

			TreeNode ifStmtNode = temp.getChildren().get(4);
			// if(conditionNode)
			boolean c = judgeCondition(conditionNode);
			if (c) {
				block_stmt(ifStmtNode);
			} else if (!judgeCondition(conditionNode.getChildren().get(0)) && (childNumber == 7)) {
				TreeNode elseStmtNode = temp.getChildren().get(6);
				block_stmt(elseStmtNode);
			} else {
				return;
			}
		}
		System.out.println("aaa");
	}

	public void while_stmt(TreeNode t) throws Exception {
		if (!t.getChildren().isEmpty()) {
			TreeNode temp = t.getFirstChild();
			TreeNode conditionNode = temp.getChildren().get(2);
			TreeNode whileStmtNode = temp.getChildren().get(4);
			TreeNode stmtBlockNode = whileStmtNode.getFirstChild();
			boolean c = judgeCondition(conditionNode);
			while (c) {
				block_stmt(whileStmtNode);
			}
		}
		System.out.println("hhh");
	}

	public void read_stmt(TreeNode t) throws Exception {
		if (!t.getChildren().isEmpty()) {
			TreeNode temp = t.getFirstChild();
			TreeNode variable = temp.getChildren().get(2);
			if (variable.getNodeType() == NodeType.IDENT) {
				if (table.contains(variable.getValue())) {

				} else {
					System.err.println("The identifier has not be declared!");
				}
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
					iniValue.add(0);
					// 数组情况
					if (arrayNode.getChildren().size() > 1) {
						TreeNode exprNode = arrayNode.getChildren().get(1);
						TreeNode factor = calAriExpr(exprNode);
						arrayIndex = Integer.parseInt(factor.getValue());
						for (int i = 0; i < arrayIndex-1; i++) {
							iniValue.add(0);
						}
						symbol.setArrayIndex(arrayIndex);
					}
					symbol.setValue(iniValue);
				} else if (temp.getNodeType().equals(NodeType.VARLIST)) {
					for (int i = 0; i < temp.getChildren().size(); i++) {
						TreeNode node = temp.getChildren().get(i);
						if (node.getNodeType().equals(NodeType.IDENT)) {
							String name = node.getValue();

							//符号表中存在该符号 并且 当前函数未超出该符号的作用域
							if (table.contains(name) && table.get(name).getScale()<=scale) {
								errMsg = "变量" + name + "已存在!";
								System.out.println(errMsg);
								return;
							}
							
							else if (!table.contains(name) || (table.contains(name)&&table.get(name).getScale()>scale)) {
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
								} else {
									errMsg = "!!变量类型错误!";
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

					if (!table.contains(variable) || ( table.contains(variable) && table.get(variable).getScale()>scale)) {
						errMsg = "变量未声明!";
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

					if (symbol.getType().equals(symbolType)) {
						if (symbol.getArrayIndex() == 0) {
							symbol.setValue(matchType(factor));

						} else {
							Object value = matchType(factor).get(0);
							List<Object> temp = symbol.getValue();
							if (sub >= 0 && sub < temp.size()) {
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
			if ((table.contains(t.getValue())&&table.get(t.getValue()).getScale()<=scale)) {
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

	// 计算算术表达式的值
	public TreeNode calAriExpr(TreeNode t) {
		if (isInt) {
			isInt = true;
		}
		isBool = false;

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
			if (isInt) {
				int e = Integer.parseInt(calFactor(t.getNthChild(1)));
				return String.valueOf(0 - e);
			} else {
				double d = Double.parseDouble(calFactor(t.getNthChild(1)));
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
			if (table.contains(id) && table.get(id).getScale()<=scale) {
				return table.get(id).getValue().get(0).toString();
			} else {
				errMsg = "变量未声明";
				System.out.println(errMsg);
				return null;
			}
		} else { // VALUE --> ID[E]
			int arrnum = (int) (Double.parseDouble(calAriExpr(t.getNthChild(2)).getValue()));
			if (table.contains(id)&&table.get(id).getScale()<=scale) {
				return table.get(id).getValue().get(arrnum - 1).toString();
			} else {
				errMsg = "数组未声明";
				System.out.println(errMsg);
				return null;
			}
		}
	}
}
