package pt.ipb.snmpfs;

import java.nio.ByteBuffer;

public interface FsEntry {
	public enum FsEntryType {
		FILE, DIRECTORY, SYMBOLIC_LINK, SOCKET, FIFO, BLOCK_DEVICE
	};

	public FsEntryType getType();

	public String getName();

	int read(String path, ByteBuffer buffer, long size, long offset);

	FsEntryInfo getInfo();
	
	FsEntryAttrs getAttrs();

	public int update();
}
