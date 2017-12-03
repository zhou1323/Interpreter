import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.org.apache.xpath.internal.operations.And;

public class SymbolTable {
	private HashMap<String,Symbol> map=new HashMap<String,Symbol>();
	
	public Symbol get(String s) {
		return map.get(s);
	}
	
	public void put(String s,Symbol symbol) {
		map.put(s, symbol);
	}
	
	public boolean contains(String s) {
		return map.containsKey(s);
	}
	public void printTable () {
		Iterator<Map.Entry<String,Symbol>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,Symbol> entry = (Map.Entry<String,Symbol>) iter.next();
			String key = entry.getKey();
			Symbol val = entry.getValue();
			System.out.println("The key is: "+key+" , the value is: "+val.toString());
		}
	}
}
