package pt.ipb.snmpfs;

import java.nio.ByteBuffer;

public abstract class AbstractEntry implements FsEntry {
	String name;
	
	public AbstractEntry() {
	}
	
	public AbstractEntry(String name) {
		this.name = name;
	}

	@Override
	public boolean isUr() {
		return true;
	}

	@Override
	public boolean isUw() {
		return false;
	}

	@Override
	public boolean isUx() {
		return false;
	}

	@Override
	public boolean isGr() {
		return false;
	}

	@Override
	public boolean isGw() {
		return false;
	}

	@Override
	public boolean isGx() {
		return false;
	}

	@Override
	public boolean isOr() {
		return false;
	}

	@Override
	public boolean isOw() {
		return false;
	}

	@Override
	public boolean isOx() {
		return false;
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
	
	@Override
	public long size() {
		return 0;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
