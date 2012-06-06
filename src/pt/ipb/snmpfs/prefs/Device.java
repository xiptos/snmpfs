package pt.ipb.snmpfs.prefs;

import java.util.ArrayList;
import java.util.List;

public class Device {
	String name;
	
	String mountDir;
	
	String mibDir;
	List<String> mibs = new ArrayList<String>();

	SnmpPrefs snmpPrefs;
	
	List<Entry> entries = new ArrayList<Entry>();
	
	public Device() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMountDir() {
		return mountDir;
	}

	public void setMountDir(String mountDir) {
		this.mountDir = mountDir;
	}

	public String getMibDir() {
		return mibDir;
	}

	public void setMibDir(String mibDir) {
		this.mibDir = mibDir;
	}

	public List<String> getMibs() {
		return mibs;
	}

	public void addMib(String mib) {
		this.mibs.add(mib);
	}

	public SnmpPrefs getSnmpPrefs() {
		return snmpPrefs;
	}

	public void setSnmpPrefs(SnmpPrefs snmpPrefs) {
		this.snmpPrefs = snmpPrefs;
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public void addEntry(Entry entry) {
		this.entries.add(entry);
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("SNMP Device:");
		str.append("\n");
		str.append("\tName: "+getName()+"\n");
		str.append("\tPrefs: "+getSnmpPrefs()+"\n");
		return str.toString();
	}
}
