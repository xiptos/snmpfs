package pt.ipb.snmpfs;

import java.nio.ByteBuffer;

public interface FsEntry {
	public enum FsEntryType {
		FILE, DIRECTORY, SYMBOLIC_LINK, SOCKET, FIFO, BLOCK_DEVICE
	};

	public boolean isUr();

	public boolean isUw();
	
	public boolean isUx();
	
	public boolean isGr();
	
	public boolean isGw();
	
	public boolean isGx();

	public boolean isOr();
	
	public boolean isOw();

	public boolean isOx();

	public FsEntryType getType();

	public String getName();

	int read(String path, ByteBuffer buffer, long size, long offset);

	long size();
}
