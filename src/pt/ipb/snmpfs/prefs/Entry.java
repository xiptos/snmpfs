package pt.ipb.snmpfs.prefs;

public class Entry {
	String label;
	String oid;
	String file;

	public Entry() {
	}
	
	public Entry(String label, String oid, String file) {
		this.label = label;
		this.oid = oid;
		this.file = file;
	}

	public Entry(String label, String oid) {
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

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

}
