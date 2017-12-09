import java.util.ArrayList;
import java.util.List;

/**
 * Created by superman on 2017/10/24.
 */
public class TreeNode {
    private NodeType nodeType; //�������
    private TreeNode parent;   //���ڵ�
    private List<TreeNode> children;    //�ӽڵ��б�
    private String value;       //���ֵ
    private int lineNum;
    private int position;
    //���캯������ʼ��������ͣ�ֵ���͸��ڵ�
    public TreeNode(NodeType nodeType,String value,TreeNode parent,int lineNum,int position) {
        this.nodeType = nodeType;
        this.children = new ArrayList<TreeNode>();
        this.value = value;
        this.parent = parent;
        this.lineNum=lineNum;
        this.position=position;
    }

    public NodeType getNodeType() {

        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public TreeNode getFirstChild() {
    	return children.get(0);
    }
    
    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLineNum(){return lineNum;}

    public void setLineNum(){this.lineNum=lineNum;}

    public int getPosition(){return position;}

    public void setPosition(){this.position=position;}

    public TreeNode getNthChild(int index){
        if(index<getChildren().size()){
            return getChildren().get(index);
        }
        return null;
    }
    public int getTypeChildNum(NodeType nodeType){
        int num = 0;
        for(TreeNode t:getChildren()){
            if(t.getNodeType()==nodeType)
                num++;
        }
        return num;
    }

}
