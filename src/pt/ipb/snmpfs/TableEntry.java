package pt.ipb.snmpfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import pt.ipb.snmpfs.prefs.Entry;
import pt.ipb.snmpfs.prefs.Table;

public class TableEntry extends AbstractEntry {
	private SnmpBackend backend;
	String content = null;
	Table table;
	List<String> cols = new ArrayList<String>();
	private MibBackend mibBackend; 

	public TableEntry(Table table, SnmpBackend backend, MibBackend mibBackend) {
		this.table = table;
		String oid = table.getOid();
		String label = table.getLabel();
		if(oid==null) {
			if(label==null) {
				throw new IllegalArgumentException("OID or Label cannot be null");
			}
			oid = mibBackend.getOid(label);
		}

		String name = table.getFile();
		if(name==null) {
			name = label;
			if(name==null) {
				name = mibBackend.getCloserName(oid);
			}
		}
		setName(name+".csv");
		
		for(Entry col : table.getCols()) {
			oid = col.getOid();
			if(oid==null) {
				if(label==null) {
					throw new IllegalArgumentException("OID or Label cannot be null");
				}
				oid = mibBackend.getOid(label);
			}
			cols.add(oid);
		}
		if(cols.isEmpty()) {
			cols.addAll(mibBackend.getColumns(oid));
		}
		this.backend = backend;
		this.mibBackend = mibBackend;
	}

	@Override
	public long size() {
		if(content==null) {
			refreshContent();
			return 0;
		} else {
			return content.length();
		}
	}
	
	private void refreshContent() {
		try {
			SnmpTable snmpTable = backend.getTable(cols);
			StringBuilder str = new StringBuilder();
			for (String col : cols) {
				str.append(mibBackend.getCloserName(col) + ";");
			}
			str.append("\n");
			int nRows = snmpTable.getRowCount();
			for (int i = 0; i < nRows; i++) {
				for (String col : cols) {
					str.append(snmpTable.getValue(col, i).getVariable().toString() + ";");
				}
				str.append("\n");
			}
			content = str.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int read(String path, ByteBuffer buffer, long size, long offset) {
		if(content==null) {
			refreshContent();
		}
		final String s = content.substring((int) offset,
				(int) Math.max(offset, Math.min(content.length() - offset, offset + size)));
		buffer.put(s.getBytes());
		return s.getBytes().length;
	}
	
	@Override
	public FsEntryType getType() {
		return FsEntryType.FILE;
	}
}
