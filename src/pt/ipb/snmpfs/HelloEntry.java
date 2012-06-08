package pt.ipb.snmpfs;

import java.nio.ByteBuffer;

public class HelloEntry extends AbstractEntry {

	String content = "Hello world!";

	public HelloEntry() {
		super("hello.txt");
	}


	@Override
	public int read(String path, ByteBuffer buffer, long size, long offset) {
		final String s = content.substring((int) offset,
				(int) Math.max(offset, Math.min(content.length() - offset, offset + size)));
		buffer.put(s.getBytes());
		getAttrs().setSize(content.length());
		getAttrs().setTime(System.currentTimeMillis());
		return s.getBytes().length;
	}

	@Override
	public FsEntryType getType() {
		return FsEntryType.FILE;
	}

}
