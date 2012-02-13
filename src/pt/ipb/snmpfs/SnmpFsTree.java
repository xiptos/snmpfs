/**
 * $Id$
@copyright@
@license@
 */

package pt.ipb.snmpfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import pt.ipb.snmpfs.FsEntry.FsEntryType;

public class SnmpFsTree extends SnmpBackend implements FsTree {
	
	public SnmpFsTree(String userName, int secLevel, String authPassphrase, String privPassphrase, int authProtocol,
			int privProtocol) throws IOException {
		super(userName, secLevel, authPassphrase, privPassphrase, authProtocol, privProtocol);
	}

	public SnmpFsTree(String userName, String authPassphrase, String privPassphrase) throws IOException {
		super(userName, authPassphrase, privPassphrase);
	}

	public SnmpFsTree(String userName, String authPassphrase) throws IOException {
		super(userName, authPassphrase);
	}

	public SnmpFsTree(String userName) throws IOException {
		super(userName);
	}

	@Override
	public FsEntry getChild(FsEntry parent, int i) {
		// TODO May be optimized
		try {
			return getChildren((OID)parent.getUserObject()).get(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getChildCount(FsEntry parent) {
		// TODO May be optimized
		try {
			return getChildren((OID)parent.getUserObject()).size();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public FsEntry getRoot() {
		return new FsEntry("1", getDirType(), new OID("1"));
	}

	protected FsEntryType getDirType() {
		return FsEntryType.FILE;
	}


	@Override
	public boolean isLeaf(FsEntry n) {
		return false;
	}

	public List<FsEntry> getChildren(OID oid) throws IOException {
		int candidate = 0;
		List<FsEntry> childList = new ArrayList<FsEntry>();
		while(true) {
			OID candidateOid = new OID(oid);
			candidateOid.append(candidate+1);
			VariableBinding varBind = getNext(candidateOid);
			candidateOid = varBind.getOid();
			if(!varBind.isException() && candidateOid.startsWith(oid)) {
				candidateOid.trim(candidateOid.size()-oid.size()-1);
				childList.add(new FsEntry(candidateOid.toString(), getDirType(), new OID(candidateOid)));
				candidate = candidateOid.get(candidateOid.size()-1);
			} else {
				break;
			}
		}
		
		return childList;
	}

	@Override
	public FsEntry getChild(FsEntry parent, String name) {
		for(int i=0; i<getChildCount(parent); i++) {
			FsEntry c = getChild(parent, i);
			if(name.equals(c.getName())) {
				return c;
			}
		}
		return null;
	}


}
