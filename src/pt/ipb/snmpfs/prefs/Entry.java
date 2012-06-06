package pt.ipb.snmpfs.prefs;

import pt.ipb.marser.type.OID;

public class Entry {
	String label;
	OID oid;
	String file;

	public Entry() {
	}
	
	public Entry(String label, OID oid, String file) {
		this.label = label;
		this.oid = oid;
		this.file = file;
	}

	public Entry(String label, OID oid) {
		this(label, oid, null);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public OID getOid() {
		return oid;
	}

	public void setOid(OID oid) {
		this.oid = oid;
	}

}
