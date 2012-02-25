package pt.ipb.snmpfs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.snmp4j.smi.VariableBinding;

public class SnmpTable {
	List<String> cols = new ArrayList<String>();
	LinkedHashMap<String, List<VariableBinding>> rows = new LinkedHashMap<String, List<VariableBinding>>();

	public SnmpTable() {
	}

	public void addCol(String col, Vector<? extends VariableBinding> variableBindings) {
		if (!cols.contains(col)) {
			cols.add(col);
		}
		List<VariableBinding> row = null;
		if (rows.containsKey(col)) {
			row = rows.get(col);
		} else {
			row = new ArrayList<VariableBinding>();
			rows.put(col, row);
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

	public VariableBinding getValue(String col, int row) {
		return rows.get(col).get(row);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String col : cols) {
			str.append(col.toString() + ";");
		}
		str.append("\n");
		int nRows = getRowCount();
		for (int i = 0; i < nRows; i++) {
			for (String col : cols) {
				str.append(getValue(col, i).getVariable().toString() + ";");
			}
			str.append("\n");
		}
		return str.toString();
	}
}
