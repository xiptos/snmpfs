package pt.ipb.snmpfs;

public class ScalarEntry extends AbstractEntry {
	String name;
	
	public ScalarEntry(String name) {
		this.name = name;
	}

	@Override
	public FsEntryType getType() {
		return FsEntryType.FILE;
	}

	@Override
	public String getName() {
		return name;
	}

}
