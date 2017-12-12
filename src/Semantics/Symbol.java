package Semantics;
import java.util.ArrayList;
import java.util.List;

public class Symbol {
	private String type;
	private String name;
	//×÷ÓÃÓò
	private int scale;
	private List<Object> value=new ArrayList<Object>();
	private int arrayIndex;
	private  String funcName;
	private String idOrArray;
	
	public Symbol() {
		;
	}
	
	public Symbol(String type, String name, int scale, List<Object> value, int arrayIndex,String funcName,String idOrArray) {
		super();
		this.type = type;
		this.name = name;
		this.scale = scale;
		this.value = value;
		this.arrayIndex = arrayIndex;
		this.funcName=funcName;
		this.idOrArray=idOrArray;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFuncName() {
		return funcName;
	}
	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}
	public String getIdOrArray() {
		return idOrArray;
	}
	public void setIdOrArray(String idOrArray) {
		this.idOrArray = idOrArray;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getScale() {
		return scale;
	}
	public void setScale(int scale) {
		this.scale = scale;
	}
	public List<Object> getValue() {
		return value;
	}
	public void setValue(List<Object> value) {
		this.value = value;
	}

	public int getArrayIndex() {
		return arrayIndex;
	}

	public void setArrayIndex(int arrayIndex) {
		this.arrayIndex = arrayIndex;
	}

	@Override
	public String toString() {
		return "Symbol [type=" + type + ", name=" + name + ", scale=" + scale + ", value=" + value + ", arrayIndex="
				+ arrayIndex + "]";
	}
	
	
}
