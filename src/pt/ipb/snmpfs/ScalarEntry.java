package pt.ipb.snmpfs;

import java.io.IOException;
import java.nio.ByteBuffer;

import pt.ipb.marser.SnmpConstants;
import pt.ipb.marser.type.OID;
import pt.ipb.marser.type.OctetString;
import pt.ipb.marser.type.Str;
import pt.ipb.marser.type.Var;
import pt.ipb.marser.type.VarBind;
import pt.ipb.snmpfs.prefs.Entry;

public class ScalarEntry extends AbstractEntry {
	private SnmpBackend backend;
	String content = null;
	OID oid;

	public ScalarEntry(Entry entry, SnmpBackend backend, MibBackend mibBackend) {
		this.oid = entry.getOid();
		String label = entry.getLabel();
		if (this.oid == null) {
			if (label == null) {
				throw new IllegalArgumentException(
						"OID or Label cannot be null");
			}
			this.oid = new OID(mibBackend.getOid(label));
		}

		if (!this.oid.endsWith(new OID(".0"))) {
			this.oid.append(0);
		}

		String name = entry.getFile();
		if (name == null) {
			name = label;
			if (name == null) {
				name = mibBackend.getCloserName(oid.toString());
			}
		}
		setName(name);
		getInfo().setUseCache(false);
		this.backend = backend;
	}

	@Override
	public int update() {
		try {
			VarBind vb = backend.get(oid);
			Var v = vb.getValue();
			if (v.getType() == SnmpConstants.OCTETSTRING) {
				this.content = Str.toString((OctetString) v) + "\n";
			} else {
				this.content = vb.getValue().toString() + "\n";
			}
			getAttrs().setSize(this.content.length());
			getAttrs().setTime(System.currentTimeMillis());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.update();
	}
	
	@Override
	public int read(String path, ByteBuffer buffer, long size, long offset) {
		if(content==null) {
			update();
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
