package pt.ipb.snmpfs.prefs;

public class Entry {
	String node;
	String oid;
	String name;

	public Entry() {
	}
	
	public Entry(String node, String oid, String name) {
		this.node = node;
		this.oid = oid;
		this.name = name;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
