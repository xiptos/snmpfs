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
		this.prefs = prefs;
	}

	public VariableBinding getNext(OID oid) throws IOException {
		initLL(PDU.GETNEXT);
		ll.setVb(oid);
		ll.setOperation(SnmpLL.DEFAULT);

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
		ll.setVb(oid);
		ll.setOperation(SnmpLL.DEFAULT);

		PDU response = ll.send();
		return response.getVariableBindings().get(0);
	}

	public SnmpTable getTable(List<String> cols) throws IOException {
		SnmpTable table = new SnmpTable();
		initLL(PDU.GETBULK);
		ll.setOperation(SnmpLL.TABLE);
		for (String col : cols) {
			ll.setVb(new OID(col));
			PDU response = ll.send();
			table.addCol(col, response.getVariableBindings());
		}
		return table;
	}

	public List<OID> walk(OID oid) throws IOException {
		List<OID> walked = new ArrayList<OID>();

		initLL(PDU.GETBULK);
		ll.setVb(oid);
		ll.setOperation(SnmpLL.WALK);
		try {
			ll.send();
			for (VariableBinding vb : ll.getSnapshot()) {
				walked.add(vb.getOid());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return walked;
	}

	public SnmpPrefs getPrefs() {
		return prefs;
	}

	public static void main(String[] args) {
		try {
			SnmpBackend snmp = new SnmpBackend(new SnmpPrefs(SnmpPrefs.Version.V2C, "public"));
			snmp.getPrefs().setHost("192.168.1.1");
			List<String> cols = new ArrayList<String>();
			cols.add("1.3.6.1.2.1.2.2.1.1");
			cols.add("1.3.6.1.2.1.2.2.1.2");
			cols.add("1.3.6.1.2.1.2.2.1.3");
			cols.add("1.3.6.1.2.1.2.2.1.4");
			cols.add("1.3.6.1.2.1.2.2.1.5");
			cols.add("1.3.6.1.2.1.2.2.1.6");
			cols.add("1.3.6.1.2.1.2.2.1.7");
			cols.add("1.3.6.1.2.1.2.2.1.8");
			cols.add("1.3.6.1.2.1.2.2.1.9");
			cols.add("1.3.6.1.2.1.2.2.1.10");
			cols.add("1.3.6.1.2.1.2.2.1.11");
			cols.add("1.3.6.1.2.1.2.2.1.12");
			cols.add("1.3.6.1.2.1.2.2.1.13");
			cols.add("1.3.6.1.2.1.2.2.1.14");
			cols.add("1.3.6.1.2.1.2.2.1.15");
			cols.add("1.3.6.1.2.1.2.2.1.16");
			cols.add("1.3.6.1.2.1.2.2.1.17");
			cols.add("1.3.6.1.2.1.2.2.1.18");
			cols.add("1.3.6.1.2.1.2.2.1.19");
			cols.add("1.3.6.1.2.1.2.2.1.20");
			cols.add("1.3.6.1.2.1.2.2.1.21");
			cols.add("1.3.6.1.2.1.2.2.1.22");

			SnmpTable table = snmp.getTable(cols);
			System.out.println(table.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
