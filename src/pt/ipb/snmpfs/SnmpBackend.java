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

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SnmpBackend {
	enum Version {
		V1, V2C, V3
	}

	enum AuthProto {
		NONE, MD5, SHA
	}

	enum PrivProto {
		NONE, SHA, DES, AES
	}

	String userName;

	String authPassphrase = null;

	String privPassphrase = null;

	String host = "127.0.0.1";

	int port = 161;

	AuthProto authProtocol = AuthProto.NONE;

	PrivProto privProtocol = PrivProto.NONE;

	Version version = Version.V2C;

	private String community;

	SnmpLL ll = new SnmpLL();

	public SnmpBackend(String userName, AuthProto authProto, String authPass, PrivProto privProto, String privPass)
			throws IOException {
		setVersion(Version.V3);
		setUserName(userName);
		setAuthPassphrase(authPass);
		setPrivPassphrase(privPass);
		setAuthProtocol(authProto);
		setPrivProtocol(privProto);
	}

	public SnmpBackend(String userName, String authPass, String privPass) throws IOException {
		this(userName, AuthProto.MD5, authPass, PrivProto.DES, privPass);
	}

	public SnmpBackend(String userName, String authPass) throws IOException {
		this(userName, AuthProto.MD5, authPass, PrivProto.NONE, null);
	}

	public SnmpBackend(String userName) throws IOException {
		this(userName, AuthProto.NONE, null, PrivProto.NONE, null);
	}

	public SnmpBackend(Version version, String community) throws IOException {
		setVersion(version);
		setCommunity(community);
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public Version getVersion() {
		return version;
	}

	public VariableBinding getNext(OID oid) throws IOException {
		initLL(PDU.GETNEXT);
		ll.addVb(oid);

		PDU response = ll.send();
		return response.getVariableBindings().get(0);
	}

	private void initLL(int pduType) {
		ll.setAddress("udp", getHost(), getPort());
		if (getVersion().equals(Version.V3)) {
			throw new NotImplementedException();
		} else {
			ll.setCommunity(getCommunity());
			ll.setPduType(pduType);
			if (getVersion().equals(Version.V2C)) {
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
		for(OID oidCol : oidCols) {
			ll.setVb(oidCol);
			PDU response = ll.send();
			table.addCol(oidCol, response.getVariableBindings());
		}
		return table;
	}
	
	public String getAuthPassphrase() {
		return authPassphrase;
	}

	public void setAuthPassphrase(String authPassphrase) {
		this.authPassphrase = authPassphrase;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPrivPassphrase() {
		return privPassphrase;
	}

	public void setPrivPassphrase(String privPassphrase) {
		this.privPassphrase = privPassphrase;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public AuthProto getAuthProtocol() {
		return authProtocol;
	}

	public void setAuthProtocol(AuthProto authProtocol) {
		this.authProtocol = authProtocol;
	}

	public PrivProto getPrivProtocol() {
		return privProtocol;
	}

	public void setPrivProtocol(PrivProto privProtocol) {
		this.privProtocol = privProtocol;
	}

	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public static void main(String[] args) {
		try {
			SnmpBackend snmp = new SnmpBackend(Version.V2C, "public");
			snmp.setHost("192.168.1.1");
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
