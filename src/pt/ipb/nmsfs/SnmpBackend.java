/*
 * $Id$
 *
 @Copyright@
 @License@
 */
package pt.ipb.nmsfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

public class SnmpBackend {
	public static final int noAuthNoPriv = 0;

	public static final int authNoPriv = 1;

	public static final int authPriv = 2;

	public static final int MD5 = 3;

	public static final int SHA = 4;

	public static final int DES = 5;

	public static final int AES = 6;

	public static final int NO = -1;

	String userName;

	String authPassphrase = null;

	String privPassphrase = null;

	String host = "127.0.0.1";

	int port = 161;

	int secLevel = authNoPriv;

	int authProtocol = NO;

	int privProtocol = NO;

	protected Snmp snmp;

	protected UserTarget target;

	public SnmpBackend(String userName, int secLevel, String authPassphrase, String privPassphrase, int authProtocol,
			int privProtocol) throws IOException {
		setUserName(userName);
		setSecLevel(secLevel);
		setAuthPassphrase(authPassphrase);
		setPrivPassphrase(privPassphrase);
		setAuthProtocol(authProtocol);
		setPrivProtocol(privProtocol);
	}

	public SnmpBackend(String userName, String authPassphrase, String privPassphrase) throws IOException {
		this(userName, authPriv, authPassphrase, privPassphrase, MD5, DES);
	}

	public SnmpBackend(String userName, String authPassphrase) throws IOException {
		this(userName, authNoPriv, authPassphrase, null, MD5, NO);
	}

	public SnmpBackend(String userName) throws IOException {
		this(userName, noAuthNoPriv, null, null, NO, NO);
	}

	public void open() throws IOException {
		TransportMapping transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);
		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		transport.listen();

		// add user to the USM
		OID authOID = null;
		switch (authProtocol) {
		case MD5:
			authOID = AuthMD5.ID;
			break;
		case SHA:
			authOID = AuthSHA.ID;
			break;
		default:
			authOID = null;
			break;
		}
		OID privOID = null;
		switch (privProtocol) {
		case DES:
			privOID = PrivDES.ID;
			break;
		case AES:
			privOID = PrivAES128.ID;
			break;
		default:
			privOID = null;
			break;
		}

		UsmUser user = null;
		switch (secLevel) {
		case noAuthNoPriv:
			user = new UsmUser(new OctetString(userName), null, null, null, null);
			break;
		case authNoPriv:
			user = new UsmUser(new OctetString(userName), authOID, new OctetString(getAuthPassphrase()), null, null);
			break;
		case authPriv:
			user = new UsmUser(new OctetString(userName), authOID, new OctetString(getAuthPassphrase()), privOID,
					new OctetString(getPrivPassphrase()));
			break;
		}
		snmp.getUSM().addUser(new OctetString(userName), user);

		Address targetAddress = GenericAddress.parse("udp:" + getHost() + "/" + getPort());

		// create the target
		target = new UserTarget();
		target.setAddress(targetAddress);
		target.setRetries(1);
		target.setTimeout(5000);
		target.setVersion(SnmpConstants.version3);
		int sl = noAuthNoPriv;
		switch (secLevel) {
		case noAuthNoPriv:
			sl = SecurityLevel.NOAUTH_NOPRIV;
			break;
		case authNoPriv:
			sl = SecurityLevel.AUTH_NOPRIV;
			break;
		case authPriv:
			sl = SecurityLevel.AUTH_PRIV;
			break;
		}
		target.setSecurityLevel(sl);
		target.setSecurityName(new OctetString(getUserName()));
	}

	@SuppressWarnings("unchecked")
	public List<TableEvent> getTable(String[] columns) {
		TableUtils t = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));
		OID[] oid = new OID[columns.length];
		int i=0;
		for(String c : columns) {
			oid[i++] = new OID(c);
		}
		List<TableEvent> l = t.getTable(target, oid, null, null);
		return l;
	}
	
	public VariableBinding getNext(String oid) throws IOException {
		// create the PDU
		PDU pdu = new ScopedPDU();
		pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(PDU.GETNEXT);

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if (responsePDU.getErrorStatus() != PDU.noError) {
			throw new IOException(responsePDU.getErrorStatusText());
		}
		return (VariableBinding)responsePDU.getVariableBindings().firstElement();
//		Address peerAddress = response.getPeerAddress();
//		for (Enumeration e = v.elements(); e.hasMoreElements();) {
//			VariableBinding varBind = (VariableBinding) e.nextElement();
//			System.out.println("Received " + varBind.getVariable() + " (" + varBind.getOid() + ") from " + peerAddress);
//		}
		// extract the address used by the agent to send the response:

	}

	public VariableBinding getNext(OID oid) throws IOException {
		PDU pdu = new ScopedPDU();
		pdu.add(new VariableBinding(oid));
		pdu.setType(PDU.GETNEXT);
System.out.println("retrieving "+oid);
		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if(responsePDU==null) {
			throw new IOException("Timed out: "+target.getAddress().toString());
		}
		if (responsePDU.getErrorStatus() != PDU.noError) {
			throw new IOException(responsePDU.getErrorStatusText());
		}
		System.out.println(responsePDU);
		return (VariableBinding)responsePDU.getVariableBindings().firstElement();
	}

	public VariableBinding get(OID oid) throws IOException {
		PDU pdu = new ScopedPDU();
		pdu.add(new VariableBinding(oid));
		pdu.setType(PDU.GET);

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if (responsePDU.getErrorStatus() != PDU.noError) {
			throw new IOException(responsePDU.getErrorStatusText());
		}
		return (VariableBinding)responsePDU.getVariableBindings().firstElement();
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

	public int getSecLevel() {
		return secLevel;
	}

	public void setSecLevel(int secLevel) {
		this.secLevel = secLevel;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getAuthProtocol() {
		return authProtocol;
	}

	public void setAuthProtocol(int authProtocol) {
		this.authProtocol = authProtocol;
	}

	public int getPrivProtocol() {
		return privProtocol;
	}

	public void setPrivProtocol(int privProtocol) {
		this.privProtocol = privProtocol;
	}

//	public static void main(String[] arg) {
//		try {
//			SnmpBackend snmp = new SnmpBackend("bart", "bartsimpson");
//			snmp.open();
//			VariableBinding b = snmp.getNext(".1.3.6.2");
//			System.out.println(b);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	public Snmp getSnmp() {
		return snmp;
	}

	public UserTarget getTarget() {
		return target;
	}

	public List<OID> getChildren(OID oid) throws IOException {
		int candidate = 0;
		List<OID> childList = new ArrayList<OID>();
		while(true) {
			OID candidateOid = new OID(oid);
			candidateOid.append(candidate+1);
			VariableBinding varBind = getNext(candidateOid);
			candidateOid = varBind.getOid();
			if(!varBind.isException() && candidateOid.startsWith(oid)) {
				candidateOid.trim(candidateOid.size()-oid.size()-1);
				childList.add(new OID(candidateOid));
				candidate = candidateOid.get(candidateOid.size()-1);
			} else {
				break;
			}
		
		}
		
		return childList;
	}

}

