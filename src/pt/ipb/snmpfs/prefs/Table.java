package pt.ipb.snmpfs.prefs;

import java.util.ArrayList;
import java.util.List;

import pt.ipb.marser.type.OID;

public class Table extends Entry {
	List<Entry> cols = new ArrayList<Entry>();

	public Table(OID oid) {
		setOid(oid);
	}
	
	public Table(String node, OID oid, String name) {
		super(node, oid, name);
	}

	public List<Entry> getCols() {
		return cols;
	}
	
	public void addCol(Entry entry) {
		cols.add(entry);
	}
	
	public void clear() {
		cols.clear();
	}
}
