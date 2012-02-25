package pt.ipb.snmpfs;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import pt.ipb.marser.MibException;
import pt.ipb.marser.MibModule;
import pt.ipb.marser.MibNode;
import pt.ipb.marser.MibOps;
import pt.ipb.marser.type.OID;

public class MibBackend {
	MibOps ops;
	
	public MibBackend(String mibDir, List<String> mibs) throws FileNotFoundException, MibException {
		this.ops = new MibOps(new String[] { mibDir });
		for(String mib : mibs) {
			this.ops.loadMib(mib);
		}
	}

	public boolean isTable(String oid) {
		MibNode tableNode = this.ops.getMibNode(oid);
		return tableNode.isTable() || tableNode.isTableEntry();
	}
	
	public List<String> getColumns(String oid) {
		List<String> cols = new ArrayList<String>();
		
		MibNode tableNode = this.ops.getMibNode(new OID(oid));
		if(tableNode.isTable() || tableNode.isTableEntry()) {
			for(Enumeration<MibNode> en = tableNode.breadthFirstEnumeration(); en.hasMoreElements(); ) {
				MibNode node = en.nextElement();
				if(node.isTableColumn()) {
					cols.add(node.getNumberedOIDString());
				}
			}
		}
		return cols;
	}
	
	public String getCloserName(String oid) {
		MibNode node = this.ops.getCloserNode(oid);
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
