/**
 * $Id$
@copyright@
@license@
 */

package pt.ipb.nmsfs;

import java.io.IOException;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import pt.ipb.marser.MibNode;
import pt.ipb.marser.MibOps;

public class AgentTree {
	DirNode root;
	MibOps mibOps;
	private SnmpBackend backend;

	public AgentTree() {
		this(null, null);
	}

	public AgentTree(SnmpBackend backend, MibOps mibOps) {
		this.root = new DirNode(AgentFS.ROOTDIR);
		this.backend = backend;
		this.mibOps = mibOps;
	}

	public Node getNode(String path) {
		if (AgentFS.ROOTDIR.equals(path)) {
			return root;
		}
		String[] pathComponents = path.split(AgentFS.ROOTDIR);
		Node node = root;
		for (String p : pathComponents) {
			if ("".equals(p)) {
				node = root;
				continue;
			}
			if (node instanceof DirNode) {
				Node child = ((DirNode) node).getChild(p);
				if (child == null) {
					return null;
				}
				node = child;
			}
		}
		return node;
	}

	public Node getParent(String path) {
		return getNode(path).getParent();
	}

	protected String oidToPath(OID oid) {
		StringBuilder str = new StringBuilder();
		for (int i : oid.getValue()) {
			str.append(AgentFS.ROOTDIR);
			str.append(i);
		}
		return str.toString();
	}

	public void mkdirs(VariableBinding varBind) {
		OID oid = varBind.getOid();
		OID nc = (OID) oid.clone();
		Node parentNode = null;
		while (parentNode == null && nc.size() > 0) {
			nc.trim(1);
			parentNode = getNode(oidToPath(nc));
		}
		if (parentNode == null)
			parentNode = getRoot();
		if (parentNode instanceof DirNode) {
			DirNode parent = (DirNode) parentNode;
			OID parentOID = parent.getOid() == null ? new OID(".") : parent.getOid();
			for (int i = oid.size() - parentOID.size() - 1; i >= 0; i--) {
				if (i == 0 && varBind.getVariable() != null) {
					ScalarNode n = new ScalarNode(varBind.getOid(), backend);
					parent.addChild(n);
				} else {
					OID o = (OID) oid.clone();
					o.trim(i);
					DirNode n = new DirNode(o);
					parent.addChild(n);
					if (mibOps != null) {
						MibNode mibNode = mibOps.getMibNode(new pt.ipb.marser.type.OID(o.toString()));
						if (mibNode != null) {
							SymlinkNode ln = new SymlinkNode(mibNode, o);
							parent.addChild(ln);
							if(mibNode.isTable()) {
								parent.addChild(new TableNode(mibNode, o, backend));
							}
						}
					}
					parent = n;
				}
			}
		}
	}

	public Node getRoot() {
		return root;
	}

	public static void main(String[] args) throws IOException {
		SnmpBackend b = new SnmpBackend("bart", "bartsimpson");
		b.open();
		AgentTree tree = new AgentTree(b, new MibOps());
		OID oid = new OID(".1");
		while (true) {
			VariableBinding varBind = b.getNext(oid);
			oid = varBind.getOid();
			if (varBind.isException()) {
				break;
			}
			tree.mkdirs(varBind);
		}

		DirNode dn = (DirNode) tree.getNode("/1");
		for (Node node : dn.children()) {
			System.out.println(node.getOid());
		}
	}

}
