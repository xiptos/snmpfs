package pt.ipb.snmpfs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

public class SnmpTable {
	List<OID> cols = new ArrayList<OID>();
	LinkedHashMap<OID, List<VariableBinding>> rows = new LinkedHashMap<OID, List<VariableBinding>>();

	public SnmpTable() {
	}

	public void addCol(OID oidCol, Vector<? extends VariableBinding> variableBindings) {
		if (!cols.contains(oidCol)) {
			cols.add(oidCol);
		}
		List<VariableBinding> row = null;
		if (rows.containsKey(oidCol)) {
			row = rows.get(oidCol);
		} else {
			row = new ArrayList<VariableBinding>();
			rows.put(oidCol, row);
		}
		row.addAll(variableBindings);
	}

	public int getColumnCount() {
		return cols.size();
	}

	public int getRowCount() {
		int n = 0;
		for (List<VariableBinding> l : rows.values()) {
			n = Math.max(n, l.size());
		}
		return n;
	}

	public VariableBinding getValue(OID col, int row) {
		return rows.get(col).get(row);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (OID col : cols) {
			str.append(col.toString() + ";");
		}
		str.append("\n");
		int nRows = getRowCount();
		for (int i = 0; i < nRows; i++) {
			for (OID col : cols) {
				str.append(getValue(col, i).getVariable().toString() + ";");
			}
			str.append("\n");
		}
		return str.toString();
	}
}
