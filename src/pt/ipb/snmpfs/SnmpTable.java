package pt.ipb.snmpfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import pt.ipb.marser.type.OID;
import pt.ipb.marser.type.VarBind;

public class SnmpTable {
	List<OID> cols = new ArrayList<OID>();
	Set<OID> indexes = new TreeSet<OID>();
	Map<OID, VarBind> rows = new HashMap<OID, VarBind>();

	public SnmpTable() {
	}

	public void addCol(OID col) {
		cols.add(col);
	}

	public void addIndex(OID index) {
		indexes.add(index);
	}

	public Set<OID> getIndexes() {
		return indexes;
	}

	public List<OID> getCols() {
		return cols;
	}

	public void addValue(VarBind vb) {
		OID key = new OID(vb.getOID());
		rows.put(key, vb);

		for (OID col : cols) {
			if (key.startsWith(col)) {
				// This value belongs to this column
				// Lets store the index
				OID index = key.subOID(col.length());
				indexes.add(index);
			}
		}
	}

	public int getColumnCount() {
		return cols.size();
	}

	public int getRowCount() {
		return indexes.size();
	}

	public VarBind getValue(OID col, OID index) {
		OID key = new OID(col);
		key.append(index);
		if (rows.containsKey(key)) {
			return rows.get(key);
		} else {
			return new VarBind(col.toString(), index);
		}
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (OID col : getCols()) {
			str.append(col.toString() + ";");
		}
		str.append("\n");
		for (OID index : getIndexes()) {
			for (OID col : getCols()) {
				VarBind vb = getValue(col, index);
				str.append(vb.getValue().toString() + ";");
			}
			str.append("\n");
		}
		return str.toString();
	}
}
