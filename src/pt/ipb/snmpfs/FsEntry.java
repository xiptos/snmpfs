package pt.ipb.snmpfs;

public class FsEntry {
	public enum FsEntryType {
		FILE, DIRECTORY, SYMBOLIC_LINK, SOCKET, FIFO, BLOCK_DEVICE
	};

	private FsEntryType type;
	boolean ur = true;
	boolean uw = true;
	boolean ux = false;
	boolean gr = false;
	boolean gw = false;
	boolean gx = false;
	boolean or = false;
	boolean ow = false;
	boolean ox = false;
	String name;
	Object userObject;

	public FsEntry(String name, FsEntryType type, Object userObject) {
		this.type = type;
		this.name = name;
		this.userObject = userObject;
	}

	public boolean isUr() {
		return ur;
	}

	public void setUr(boolean ur) {
		this.ur = ur;
	}

	public boolean isUw() {
		return uw;
	}

	public void setUw(boolean uw) {
		this.uw = uw;
	}

	public boolean isUx() {
		return ux;
	}

	public void setUx(boolean ux) {
		this.ux = ux;
	}

	public boolean isGr() {
		return gr;
	}

	public void setGr(boolean gr) {
		this.gr = gr;
	}

	public boolean isGw() {
		return gw;
	}

	public void setGw(boolean gw) {
		this.gw = gw;
	}

	public boolean isGx() {
		return gx;
	}

	public void setGx(boolean gx) {
		this.gx = gx;
	}

	public boolean isOr() {
		return or;
	}

	public void setOr(boolean or) {
		this.or = or;
	}

	public boolean isOw() {
		return ow;
	}

	public void setOw(boolean ow) {
		this.ow = ow;
	}

	public boolean isOx() {
		return ox;
	}

	public void setOx(boolean ox) {
		this.ox = ox;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	public FsEntryType getType() {
		return type;
	}

	public void setType(FsEntryType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
