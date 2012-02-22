/*
 * $Id$
 *
 @Copyright@
 @License@
 */
package pt.ipb.snmpfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import pt.ipb.snmpfs.prefs.SnmpPrefs;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SnmpBackend {
	SnmpPrefs prefs;

	SnmpLL ll = new SnmpLL();

	public SnmpBackend(SnmpPrefs prefs) throws IOException {
	}

	public VariableBinding getNext(OID oid) throws IOException {
		initLL(PDU.GETNEXT);
		ll.addVb(oid);

		PDU response = ll.send();
		return response.getVariableBindings().get(0);
	}

	private void initLL(int pduType) {
		ll.setAddress("udp", prefs.getHost(), prefs.getPort());
		if (prefs.getVersion().equals(SnmpPrefs.Version.V3)) {
			throw new NotImplementedException();
		} else {
			ll.setCommunity(prefs.getCommunity());
			ll.setPduType(pduType);
			if (prefs.getVersion().equals(SnmpPrefs.Version.V2C)) {
				ll.setVersion(SnmpConstants.version2c);
			} else {
				ll.setVersion(SnmpConstants.version1);
			}
		}
	}

	public VariableBinding get(OID oid) throws IOException {
		initLL(PDU.GET);
		ll.addVb(oid);

		PDU response = ll.send();
		return response.getVariableBindings().get(0);
	}

	public SnmpTable getTable(List<OID> oidCols) throws IOException {
		SnmpTable table = new SnmpTable();
		initLL(PDU.GETBULK);
		for (OID oidCol : oidCols) {
			ll.setVb(oidCol);
			PDU response = ll.send();
			table.addCol(oidCol, response.getVariableBindings());
		}
		return table;
	}

	public SnmpPrefs getPrefs() {
		return prefs;
	}
	
	public static void main(String[] args) {
		try {
			SnmpBackend snmp = new SnmpBackend(new SnmpPrefs(SnmpPrefs.Version.V2C, "public"));
			snmp.getPrefs().setHost("192.168.1.1");
			List<OID> cols = new ArrayList<OID>();
			cols.add(new OID("1.3.6.1.2.1.2.2.1.1"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.2"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.3"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.4"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.5"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.6"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.7"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.8"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.9"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.10"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.11"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.12"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.13"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.14"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.15"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.16"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.17"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.18"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.19"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.20"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.21"));
			cols.add(new OID("1.3.6.1.2.1.2.2.1.22"));

			SnmpTable table = snmp.getTable(cols);
			System.out.println(table.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
