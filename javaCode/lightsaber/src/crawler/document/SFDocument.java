
package crawler.document;

import java.util.HashMap;

public class SFDocument {
	
	private HashMap<FieldNames, Object> map;
	
	public SFDocument() {
		map = new HashMap<FieldNames, Object>();
	}
	
	public void setField(FieldNames fn, Object o) {
		map.put(fn, o);
	}
	
	public Object getField(FieldNames fn) {
		return map.get(fn);
	}
}
