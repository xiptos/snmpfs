package pt.ipb.nmsfs;

import org.snmp4j.smi.OID;

import fuse.compat.FuseStat;

public abstract class Node {
	private FuseStat stat;
	private DirNode parent;
	String name;
	OID oid;

	public Node(String name) {
		stat = createStat();
		this.name = name;
		this.oid = null;
	}
	
	public Node(OID oid) {
		stat = createStat();
		this.name = Integer.toString(oid.last());
		this.oid = oid;
	}

	protected abstract FuseStat createStat();

	public synchronized FuseStat getStat() {
		return stat;
	}

	public synchronized void setStat(FuseStat stat) {
		this.stat = stat;
	}

	public synchronized DirNode getParent() {
		return parent;
	}

	synchronized void setParent(DirNode parent) {
		this.parent = parent;
	}

	public OID getOid() {
		return oid;
	}
	
	public void setOid(OID oid) {
		this.oid = oid;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
