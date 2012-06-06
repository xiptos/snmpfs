package pt.ipb.snmpfs;

import java.io.IOException;
import java.nio.ByteBuffer;

import pt.ipb.marser.type.OID;
import pt.ipb.marser.type.VarBind;
import pt.ipb.snmpfs.prefs.Entry;

public class ScalarEntry extends AbstractEntry {
	private SnmpBackend backend;
	String content = null;
	OID oid;

	public ScalarEntry(Entry entry, SnmpBackend backend, MibBackend mibBackend) {
		this.oid = entry.getOid();
		String label = entry.getLabel();
		if(this.oid==null) {
			if(label==null) {
				throw new IllegalArgumentException("OID or Label cannot be null");
			}
			this.oid = new OID(mibBackend.getOid(label));
		}

		if(!this.oid.endsWith(new OID(".0"))) {
			this.oid.append(0);
		}
		
		String name = entry.getFile();
		if(name==null) {
			name = label;
			if(name==null) {
				name = mibBackend.getCloserName(oid.toString());
			}
		}
		setName(name);
		this.backend = backend;
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
			VarBind vb = backend.get(oid);
			this.content = vb.getValue().toString()+"\n";
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
