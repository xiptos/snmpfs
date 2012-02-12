package pt.ipb.nmsfs;

import org.snmp4j.smi.OID;

import pt.ipb.marser.MibNode;

import fuse.FuseFtype;
import fuse.compat.FuseStat;

public class SymlinkNode extends Node {
	MibNode mibNode;

	public SymlinkNode(MibNode mibNode, OID oid) {
		super(oid);
		this.mibNode = mibNode;
		setName(mibNode.getLabel());
	}

	//
	// create initial FuseStat structure (called from Node's constructor)

	protected FuseStat createStat() {
		FuseStat stat = new FuseStat();

		stat.mode = FuseFtype.TYPE_SYMLINK | 0777;
		stat.uid = stat.gid = 0;
		stat.ctime = stat.mtime = stat.atime = (int) (System.currentTimeMillis() / 1000L);
		stat.size = 0;
		stat.blocks = 0;

		return stat;
	}

	public MibNode getMibNode() {
		return mibNode;
	}

	public void setMibNode(MibNode mibNode) {
		this.mibNode = mibNode;
	}

	public OID getTarget() {
		return oid;
	}
}
