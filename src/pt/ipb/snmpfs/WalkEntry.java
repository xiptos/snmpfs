package pt.ipb.snmpfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import net.fusejna.ErrorCodes;

import pt.ipb.marser.type.OID;

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
	public int update() {
		try {
			List<OID> oids = backend.walk(new OID(".1"));
			StringBuilder str = new StringBuilder();
			for(OID oid : oids) {
				String label = mibBackend.getCloserName(oid.toString());
				str.append(label+";"+oid.toString()+"\n");
			}
			this.content = str.toString();
			getAttrs().setSize(this.content.length());
			getAttrs().setTime(System.currentTimeMillis());
		} catch (IOException e) {
			e.printStackTrace();
			return ErrorCodes.ENOENT;
		}
		return 0;
	}

	@Override
	public int read(String path, ByteBuffer buffer, long size, long offset) {
		if(content==null) {
			update();
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
