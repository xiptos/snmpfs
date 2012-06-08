package pt.ipb.snmpfs;

public class FsEntryAttrs {

	boolean ur = true;
	boolean uw = false;
	boolean ux = false;

	boolean gr = false;
	boolean gw = false;
	boolean gx = false;

	boolean or = false;
	boolean ow = false;
	boolean ox = false;

	long size;
	
	long time;

	public FsEntryAttrs() {
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

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	

}
