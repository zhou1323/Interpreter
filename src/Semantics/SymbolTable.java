package Semantics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.xpath.internal.operations.And;

public class SymbolTable {
	public HashMap<String,List<Symbol>> map=new HashMap<String,List<Symbol>>();
	
	public Symbol get(String s) {
		List<Symbol> temp=map.get(s);
		return temp.get(temp.size()-1);
	}
	
	public void put(String s,Symbol symbol) {
		List<Symbol> temp;
		if(contains(s)) 
			temp=map.get(s);
		else
			temp=new ArrayList<Symbol>();
		if(contains(s)&&get(s).getScale()==symbol.getScale()) {
			;
		}
		else
			temp.add(symbol);
		map.put(s, temp);
	}
	
	public void remove(String s) {
		if(contains(s)) {
			List<Symbol> temp=map.get(s);
			temp.remove(temp.size()-1);
			map.put(s, temp);
		}
	}
	
	public void removeAtLevel(int i) {
		if(!map.isEmpty()) {
			Iterator<Map.Entry<String,List<Symbol>>> iter = map.entrySet().iterator();
			List<String> toRemoveKeys=new ArrayList<String>();
			while (iter.hasNext()) {
				Map.Entry<String,List<Symbol>> entry = (Map.Entry<String,List<Symbol>>) iter.next();
				
				String key = entry.getKey();
				List<Symbol> val = entry.getValue();
				for (Symbol symbol : val) {
					if(symbol.getScale()==i) {
						val.remove(symbol);

						map.put(key, val);
						if(val.isEmpty()) {
							toRemoveKeys.add(key);
						}
						
						break;
					}
				}
			}
			for (String key : toRemoveKeys) {
				map.remove(key);
			}
		}
	}
	public boolean contains(String s) {
		if(map.containsKey(s) && !map.get(s).isEmpty()) {
			return true;
		}
		return false;
	}
	public void printTable () {
		Iterator<Map.Entry<String,List<Symbol>>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,List<Symbol>> entry = (Map.Entry<String,List<Symbol>>) iter.next();
			String key = entry.getKey();
			List<Symbol> val = entry.getValue();
			System.out.println("The key is: "+key+" , the value is: "+val.toString());
		}
	}
	
	public void printDebugTable() {
		Iterator<Map.Entry<String,List<Symbol>>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,List<Symbol>> entry = (Map.Entry<String,List<Symbol>>) iter.next();
			String key = entry.getKey();
			List<Symbol> val = entry.getValue();
			Symbol newSymbol=val.get(val.size()-1);
			System.out.println("The key is: "+key+" , the value is: "+newSymbol.toString());
		}
	}
}
