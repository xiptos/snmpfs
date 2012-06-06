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
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import pt.ipb.marser.SnmpConstants;
import pt.ipb.marser.type.OID;
import pt.ipb.marser.type.Var;
import pt.ipb.marser.type.VarBind;
import pt.ipb.snmpfs.prefs.SnmpPrefs;

public class SnmpBackend {
	SnmpPrefs prefs;

	SnmpLL ll = new SnmpLL();

	public SnmpBackend(SnmpPrefs prefs) throws IOException {
		this.prefs = prefs;
	}

	public VarBind getNext(OID oid) throws IOException {
		initLL(PDU.GETNEXT);
		ll.setVb(new org.snmp4j.smi.OID(oid.toString()));
		ll.setOperation(SnmpLL.DEFAULT);

		PDU response = ll.send();
		return toVarBind(response.getVariableBindings().get(0));
	}

	private VarBind toVarBind(VariableBinding vb) {
		return new VarBind(vb.getOid().toString(), toVar(vb.getVariable()));
	}

	private Var toVar(Variable var) {
		byte type = SnmpConstants.INTEGER;
		switch (var.getSyntax()) {
		case SMIConstants.SYNTAX_INTEGER32:
			type = SnmpConstants.INTEGER32;
			break;
		// case SMIConstants.SYNTAX_BITS: type = SnmpConstants.BITS; break;
		case SMIConstants.SYNTAX_COUNTER32:
			type = SnmpConstants.COUNTER32;
			break;
		case SMIConstants.SYNTAX_COUNTER64:
			type = SnmpConstants.COUNTER64;
			break;
		case SMIConstants.SYNTAX_GAUGE32:
			type = SnmpConstants.GAUGE32;
			break;
		case SMIConstants.SYNTAX_IPADDRESS:
			type = SnmpConstants.IPADDRESS;
			break;
		case SMIConstants.SYNTAX_NULL:
			type = SnmpConstants.NULL;
			break;
		case SMIConstants.SYNTAX_OBJECT_IDENTIFIER:
			type = SnmpConstants.OID;
			break;
		case SMIConstants.SYNTAX_OCTET_STRING:
			type = SnmpConstants.OCTETSTRING;
			break;
		case SMIConstants.SYNTAX_OPAQUE:
			type = SnmpConstants.OPAQUE;
			break;
		case SMIConstants.SYNTAX_TIMETICKS:
			type = SnmpConstants.TIMETICKS;
			break;
		// case SMIConstants.SYNTAX_UNSIGNED_INTEGER32: type =
		// SnmpConstants.UNSIGNED32; break;

		}
		return Var.createVar(var.toString(), type);
	}

	private void initLL(int pduType) {
		ll.setAddress("udp", prefs.getHost(), prefs.getPort());
		if (prefs.getVersion().equals(SnmpPrefs.Version.V3)) {
			ll.setAuthPassphrase(prefs.getAuthPassphrase());
			ll.setAuthProtocol(prefs.getAuthProtocol().toString());
			ll.setCommunity(prefs.getCommunity());
			ll.setSecurityName(prefs.getUserName());
			ll.setVersion(org.snmp4j.mp.SnmpConstants.version3);
			ll.setPrivPassphrase(prefs.getPrivPassphrase());
			ll.setPrivProtocol(prefs.getPrivProtocol().toString());
			ll.setContextName(prefs.getContext());

		} else {
			ll.setCommunity(prefs.getCommunity());
			ll.setPduType(pduType);
			if (prefs.getVersion().equals(SnmpPrefs.Version.V2C)) {
				ll.setVersion(org.snmp4j.mp.SnmpConstants.version2c);
			} else {
				ll.setVersion(org.snmp4j.mp.SnmpConstants.version1);
			}
		}
	}

	public VarBind get(OID oid) throws IOException {
		initLL(PDU.GET);
		ll.setVb(new org.snmp4j.smi.OID(oid.toString()));
		ll.setOperation(SnmpLL.DEFAULT);

		PDU response = ll.send();
		return toVarBind(response.getVariableBindings().get(0));
	}

	public SnmpTable getTable(OID root, List<OID> cols)
			throws IOException {
		SnmpTable table = new SnmpTable();
		if (cols == null || cols.isEmpty()) {
			return table;
		}
		for (OID col : cols) {
			table.addCol(col);
		}

		initLL(PDU.GETBULK);
		ll.setOperation(SnmpLL.TABLE);
		for (OID col : table.getCols()) {
			ll.addVb(new org.snmp4j.smi.OID(col.toString()));
		}

		org.snmp4j.smi.OID rootOid = new org.snmp4j.smi.OID(root.toString());
		boolean comeOut = false;
		while (!comeOut) {
			PDU response = ll.send();
			if (response.getErrorStatus() != org.snmp4j.mp.SnmpConstants.SNMP_ERROR_SUCCESS) {
				comeOut = true;
				break;
			}

			ll.clearVbs();
			for (VariableBinding vb : response.getVariableBindings()) {
				if (!vb.getOid().startsWith(rootOid)) {
					comeOut = true;
					break;
				}
				ll.addVb(vb.getOid());
				table.addValue(toVarBind(vb));
			}
		}

		return table;
	}

	public List<OID> walk(OID oid) throws IOException {
		List<OID> walked = new ArrayList<OID>();

		initLL(PDU.GETBULK);
		ll.setVb(new org.snmp4j.smi.OID(oid.toString()));
		ll.setOperation(SnmpLL.WALK);
		try {
			ll.send();
			for (VariableBinding vb : ll.getSnapshot()) {
				walked.add(new OID(vb.getOid().toString()));
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

			// SnmpBackend snmp = new SnmpBackend(new SnmpPrefs("bart",
			// "bartsimpson"));
			// snmp.getPrefs().setHost("192.168.1.78");
			// VariableBinding vb = snmp.getNext(new OID("1.3.6.1.2.1.1.1"));
			// System.out.println(vb);

			List<OID> cols = new ArrayList<OID>();
			// prtMarkerSuppliesTable
			// cols.add(new OID("1.3.6.1.2.1.43.11.1.1.1"));
			// cols.add(new OID("1.3.6.1.2.1.43.11.1.1.2"));
			// cols.add(new OID("1.3.6.1.2.1.43.11.1.1.3"));
			// cols.add(new OID("1.3.6.1.2.1.43.11.1.1.4"));
			// cols.add(new OID("1.3.6.1.2.1.43.11.1.1.5"));
			// cols.add(new OID("1.3.6.1.2.1.43.11.1.1.6"));
			// cols.add(new OID("1.3.6.1.2.1.43.11.1.1.7"));
			// cols.add(new OID("1.3.6.1.2.1.43.11.1.1.8"));
			// cols.add(new OID("1.3.6.1.2.1.43.11.1.1.9"));
			//
			// SnmpBackend snmp = new SnmpBackend(new SnmpPrefs(
			// SnmpPrefs.Version.V1, "public"));
			// snmp.getPrefs().setHost("192.168.1.9");
			// SnmpTable table = snmp.getTable(cols);
			// System.out.println(table.toString());

			// hrProcessorTable
			SnmpBackend snmp = new SnmpBackend(new SnmpPrefs("bart",
					"bartsimpson"));
			snmp.getPrefs().setHost("192.168.1.78");
			cols.add(new OID(".1.3.6.1.2.1.25.3.3.1.1"));
			cols.add(new OID(".1.3.6.1.2.1.25.3.3.1.2"));
			SnmpTable table = snmp.getTable(new OID("1.3.6.1.2.1.25.3.3"), cols);

			// IF-MIB (?)
			// SnmpBackend snmp = new SnmpBackend(new SnmpPrefs("bart",
			// "bartsimpson"));
			// snmp.getPrefs().setHost("192.168.1.78");
			// SnmpTable table = snmp.getTable(new OID("1.3.6.1.2.1.2.2"),cols);
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.1"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.2"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.3"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.4"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.5"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.6"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.7"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.8"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.9"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.10"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.11"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.12"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.13"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.14"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.15"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.16"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.17"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.18"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.19"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.20"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.21"));
			// cols.add(new OID("1.3.6.1.2.1.2.2.1.22"));

			for (OID idx : table.getIndexes()) {
				System.out.println(idx);
			}
			System.out.println(table.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
