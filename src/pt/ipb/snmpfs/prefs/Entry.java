package pt.ipb.snmpfs.prefs;

public class Entry {
	String node;
	String oid;

	public Entry() {
	}
	
	public Entry(String node, String oid) {
		this.node = node;
		this.oid = oid;
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

}
