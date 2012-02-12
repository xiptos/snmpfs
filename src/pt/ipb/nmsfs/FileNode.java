package pt.ipb.nmsfs;

import java.nio.ByteBuffer;

import org.snmp4j.smi.OID;

import fuse.FuseException;

public abstract class FileNode extends Node {
	public FileNode(OID oid) {
		super(oid);
	}

	public abstract void open(int flags) throws FuseException;

	public abstract void release(int flags) throws FuseException;

	public abstract void read(ByteBuffer buff, long offset) throws FuseException;

	public abstract void write(ByteBuffer buff, long offset) throws FuseException;

	public abstract void truncate(long size) throws FuseException;

	public abstract void utime(int atime, int mtime) throws FuseException;
}
