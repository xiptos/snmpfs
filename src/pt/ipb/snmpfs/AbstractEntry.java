package pt.ipb.snmpfs;

import java.nio.ByteBuffer;

public abstract class AbstractEntry implements FsEntry {
	String name;
	FsEntryAttrs attrs = null;
	FsEntryInfo info = new FsEntryInfo();
	
	public AbstractEntry() {
	}
	
	public AbstractEntry(String name) {
		this.name = name;
	}

	@Override
	public abstract FsEntryType getType();

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int read(String path, ByteBuffer buffer, long size, long offset) {
		return 0;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public FsEntryAttrs getAttrs() {
		if(attrs == null) {
			attrs = new FsEntryAttrs();
			update();
		}
		return attrs;
	}
	
	@Override
	public FsEntryInfo getInfo() {
		return info;
	}
	
	@Override
	public int update() {
		return 0;
	}
}
