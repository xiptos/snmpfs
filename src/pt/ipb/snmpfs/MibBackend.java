package pt.ipb.snmpfs;

import java.io.FileNotFoundException;
import java.util.List;

import pt.ipb.marser.MibException;
import pt.ipb.marser.MibModule;
import pt.ipb.marser.MibNode;
import pt.ipb.marser.MibOps;

public class MibBackend {
	MibOps ops;
	
	public MibBackend(String mibDir, List<String> mibs) throws FileNotFoundException, MibException {
		this.ops = new MibOps(new String[] { mibDir });
		for(String mib : mibs) {
			this.ops.loadMib(mib);
		}
	}

	public String getCloserName(String oid) {
		MibNode node = this.ops.getCloserNode(oid);
		System.out.println(node+" - "+oid);
		if(node!=null) {
			return node.getLabel();
		}
		return oid;
	}
	
	public String getOid(String label) {
		for(MibModule module : ops.getMibModules()) {
			MibNode node = module.getNode(label);
			if(node!=null) {
				return node.getNumberedOIDString();
			}
		}
		return null;
	}
}
