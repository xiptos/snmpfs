package pt.ipb.snmpfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import pt.ipb.marser.type.OID;
import pt.ipb.snmpfs.prefs.Entry;
import pt.ipb.snmpfs.prefs.Table;

public class TableEntry extends AbstractEntry {
	private SnmpBackend backend;
	String content = null;
	Table table;
	OID root;
	List<OID> cols = new ArrayList<OID>();
	private MibBackend mibBackend;

	public TableEntry(Table table, SnmpBackend backend, MibBackend mibBackend) {
		this.table = table;
		OID oid = table.getOid();
		String label = table.getLabel();
		if (oid == null) {
			if (label == null) {
				throw new IllegalArgumentException(
						"OID or Label cannot be null");
			}
			oid = new OID(mibBackend.getOid(label));
			this.root = oid;
		}

		String name = table.getFile();
		if (name == null) {
			name = label;
			if (name == null) {
				name = mibBackend.getCloserName(oid.toString());
			}
		}
		setName(name + ".csv");

		for (Entry col : table.getCols()) {
			oid = col.getOid();
			if (oid == null) {
				if (label == null) {
					throw new IllegalArgumentException(
							"OID or Label cannot be null");
				}
				oid = new OID(mibBackend.getOid(label));
			}
			cols.add(oid);
		}
		if (cols.isEmpty()) {
			for (String o : mibBackend.getColumns(oid.toString())) {
				cols.add(new OID(o));
			}
		}
		this.backend = backend;
		this.mibBackend = mibBackend;
	}

	@Override
	public long size() {
		if (content == null) {
			refreshContent();
			return 0;
		} else {
			return content.length();
		}
	}

	private void refreshContent() {
		try {
			SnmpTable snmpTable = backend.getTable(root, cols);
			StringBuilder str = new StringBuilder();
			for (OID col : snmpTable.getCols()) {
				str.append(mibBackend.getCloserName(col.toString()) + ";");
			}
			str.append("\n");
			for (OID index : snmpTable.getIndexes()) {
				for (OID col : snmpTable.getCols()) {
					str.append(snmpTable.getValue(col, index).getValue()
							.toString()
							+ ";");
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
		if (content == null) {
			refreshContent();
		}
		final String s = content.substring(
				(int) offset,
				(int) Math.max(offset,
						Math.min(content.length() - offset, offset + size)));
		buffer.put(s.getBytes());
		return s.getBytes().length;
	}

	@Override
	public FsEntryType getType() {
		return FsEntryType.FILE;
	}
}
