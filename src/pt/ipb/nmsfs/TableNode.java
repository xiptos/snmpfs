package pt.ipb.nmsfs;

import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.List;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.TableEvent;

import pt.ipb.marser.MibNode;
import fuse.FuseException;
import fuse.FuseFS;
import fuse.FuseFtype;
import fuse.compat.FuseStat;

public class TableNode extends FileNode {
	private SnmpBackend backend;
	private byte[] content;
	MibNode mibNode;

	public TableNode(MibNode mibNode, OID oid, SnmpBackend backend) {
		super(oid);
		this.mibNode = mibNode;
		setName(mibNode.getLabel() + ".csv");
		this.backend = backend;
		content = new byte[0];
		try {
			refreshContent();
		} catch (FuseException e) {
			e.printStackTrace();
		}
	}

	protected FuseStat createStat() {
		FuseStat stat = new FuseStat();

		stat.mode = FuseFtype.TYPE_FILE | 0444;
		stat.uid = stat.gid = 0;
		stat.ctime = stat.mtime = stat.atime = (int) (System.currentTimeMillis() / 1000L);
		stat.size = 0;
		stat.blocks = 0;

		return stat;
	}

	public synchronized void read(ByteBuffer buff, long offset) throws FuseException {
		if (offset >= content.length)
			return;

		int length = buff.capacity();
		if (offset + length > content.length)
			length = content.length - (int) offset;

		buff.put(content, (int) offset, length);
	}

	public void write(ByteBuffer buff, long offset) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EROFS);
	}

	public void open(int flags) throws FuseException {
		if (flags == FuseFS.O_RDWR || flags == FuseFS.O_WRONLY)
			throw new FuseException("Read Only").initErrno(FuseException.EROFS);

		refreshContent();
	}

	@SuppressWarnings("unchecked")
	private void refreshContent() throws FuseException {
		try {
			MibNode entry = mibNode.getFirstChild();
			String[] cols = new String[entry.getChildCount()];
			StringBuilder str = new StringBuilder();
			int i = 0;
			for (Enumeration<MibNode> e = entry.children(); e.hasMoreElements();) {
				MibNode col = e.nextElement();
				cols[i++] = col.getOID().toString();
				str.append(col.getLabel());
				if (e.hasMoreElements()) {
					str.append(";");
				}
			}
			str.append("\n");
			List<TableEvent> l = backend.getTable(cols);
			for (TableEvent e : l) {
				i = 0;
				if (e.getColumns() != null) {
					for (VariableBinding v : e.getColumns()) {
						if (v != null && v.getVariable() != null) {
							str.append(v.getVariable().toString());
						} else {
							str.append("");
						}
						i++;
						if (i < e.getColumns().length) {
							str.append(";");
						}
					}
					str.append("\n");
				}
			}
			setContent(str.toString().getBytes());
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	public void release(int flags) throws FuseException {
		// noop
	}

	public void truncate(long size) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EROFS);
	}

	public void utime(int atime, int mtime) throws FuseException {
		// noop
	}

	//
	// file content access

	public synchronized byte[] getContent() {
		return content;
	}

	public synchronized void setContent(byte[] content) {
		// stat is by declaration read-only - we must create a copy before
		// modifying it's attributes
		FuseStat stat = (FuseStat) super.getStat().clone();

		if (this.content == null)
			stat.ctime = (int) (System.currentTimeMillis() / 1000L);

		this.content = content;

		stat.mtime = stat.atime = (int) (System.currentTimeMillis() / 1000L);
		stat.size = content.length;
		stat.blocks = (content.length + 511) / 512;

		super.setStat(stat);
	}
}
