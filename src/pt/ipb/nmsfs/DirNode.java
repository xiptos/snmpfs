package pt.ipb.nmsfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.snmp4j.smi.OID;

import fuse.FuseFtype;
import fuse.compat.FuseStat;

public class DirNode extends Node {
	protected Map<String,Node> children = new HashMap<String,Node>();

	public DirNode(String name) {
		super(name);
	}
	
	public DirNode(OID oid) {
		super(oid);
	}

	protected FuseStat createStat() {
		FuseStat stat = new FuseStat();

		stat.mode = FuseFtype.TYPE_DIR | 0755;
		stat.uid = stat.gid = 0;
		stat.ctime = stat.mtime = stat.atime = (int) (System.currentTimeMillis() / 1000L);
		stat.size = 0;
		stat.blocks = 0;

		return stat;
	}

	public synchronized void addChild(Node node) {
		children.put(node.getName(), node);
		node.setParent(this);

		FuseStat stat = (FuseStat) getStat().clone();
		stat.mtime = stat.atime = (int) (System.currentTimeMillis() / 1000L);
		setStat(stat);
	}

	public synchronized void removeChild(Node node) {
		children.remove(node.getName());

		if (node != null) {
			FuseStat stat = (FuseStat) getStat().clone();
			stat.mtime = stat.atime = (int) (System.currentTimeMillis() / 1000L);
			setStat(stat);

			node.setParent(null);
		}

	}

	public synchronized boolean hasChild(String name) {
		return children.containsKey(name);
	}
	
	public synchronized Node getChild(String name) {
		return children.get(name);
	}

	public synchronized Collection<Node> children() {
		return children.values();
	}
}
