package pt.ipb.snmpfs;

public class FsEntryInfo {
	boolean useCache = true;
	
	public FsEntryInfo() {
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}
	
	public boolean isUseCache() {
		return useCache;
	}

}
