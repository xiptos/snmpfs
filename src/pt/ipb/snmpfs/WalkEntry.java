package pt.ipb.snmpfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.snmp4j.smi.OID;

public class WalkEntry extends AbstractEntry {
	
	private SnmpBackend backend;
	String content = null;
	private MibBackend mibBackend;

	public WalkEntry(SnmpBackend backend, MibBackend mibBackend) {
		super("_walk.txt");
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
			List<OID> oids = backend.walk(new OID(".1"));
			System.out.println("Refreshing content. Walked: "+oids.size());
			StringBuilder str = new StringBuilder();
			for(OID oid : oids) {
				String label = mibBackend.getCloserName(oid.toString());
				str.append(label+";"+oid.toString()+"\n");
			}
			this.content = str.toString();
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
