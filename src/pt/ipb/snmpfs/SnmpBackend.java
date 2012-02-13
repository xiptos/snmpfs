/*
 * $Id$
 *
 @Copyright@
 @License@
 */
package pt.ipb.snmpfs;

import java.io.IOException;
import java.util.List;

import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.TlsAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;
import org.snmp4j.util.ThreadPool;

public class SnmpBackend {
	public static final int noAuthNoPriv = 0;

	public static final int authNoPriv = 1;

	public static final int authPriv = 2;

	public static final int MD5 = 3;

	public static final int SHA = 4;

	public static final int DES = 5;

	public static final int AES = 6;

	public static final int NO = -1;

	private static final int NUM_DISPATCHER_THREADS = 2;

	private static final OctetString LOCAL_ENGINE_ID = new OctetString(MPv3.createLocalEngineID());

	String userName;

	String authPassphrase = null;

	String privPassphrase = null;

	String host = "127.0.0.1";

	int port = 161;

	int secLevel = authNoPriv;

	int authProtocol = NO;

	int privProtocol = NO;

	protected Snmp snmp;

	protected Target target;

	int version = SnmpConstants.version3;

	private String community;

	private Address address;

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

	public SnmpBackend(int version, String community) throws IOException {
		this.community = community;
		this.version = version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getVersion() {
		return version;
	}

	private static Address getAddress(String transport, String host, int port) {
		if (transport.equalsIgnoreCase("udp")) {
			return new UdpAddress(host + "/" + port);
		} else if (transport.equalsIgnoreCase("tcp")) {
			return new TcpAddress(host + "/" + port);
		} else if (transport.equalsIgnoreCase("tls")) {
			return new TlsAddress(host + "/" + port);
		}
		throw new IllegalArgumentException("Unknown transport " + transport);
	}

	public void open() throws IOException {
		AbstractTransportMapping<?> transport;
		address = getAddress("udp", getHost(), getPort());

		if (address instanceof TcpAddress) {
			transport = new DefaultTcpTransportMapping();
		} else {
			transport = new DefaultUdpTransportMapping();
		}
		ThreadPool threadPool = ThreadPool.create("DispatcherPool", NUM_DISPATCHER_THREADS);
		MessageDispatcher mtDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

		// add message processing models
		mtDispatcher.addMessageProcessingModel(new MPv1());
		mtDispatcher.addMessageProcessingModel(new MPv2c());
		mtDispatcher.addMessageProcessingModel(new MPv3(LOCAL_ENGINE_ID.getValue()));

		// add all security protocols
		SecurityProtocols.getInstance().addDefaultProtocols();
		SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

		snmp = new Snmp(mtDispatcher, transport);
		if (version == SnmpConstants.version3) {
			USM usm = new USM(SecurityProtocols.getInstance(), LOCAL_ENGINE_ID, 0);
			SecurityModels.getInstance().addSecurityModel(usm);
			// Add the configured user to the USM
			addUsmUser(snmp);
		} else {
			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString(community));
			this.target = target;
		}

		// snmp.addCommandResponder(this);

		transport.listen();
		System.out.println("Listening on " + address);

		this.target = createTarget();
		// create the target
		target = new UserTarget();
		target.setAddress(address);
		target.setRetries(1);
		target.setTimeout(5000);
		target.setVersion(version);

		// try {
		// this.wait();
		// } catch (InterruptedException ex) {
		// Thread.currentThread().interrupt();
		// }

	}

	private Target createTarget() {
		if (version == SnmpConstants.version3) {
			UserTarget target = new UserTarget();
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

			return target;
		} else {
			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString(community));
			return target;
		}
	}

	private void addUsmUser(Snmp snmp) {
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
	}

	public List<TableEvent> getTable(String[] columns) {
		TableUtils t = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));
		OID[] oid = new OID[columns.length];
		int i = 0;
		for (String c : columns) {
			oid[i++] = new OID(c);
		}
		List<TableEvent> l = t.getTable(target, oid, null, null);
		return l;
	}

	public VariableBinding getNext(String oid) throws IOException {
		return getNext(new OID(oid));
	}

	public PDU createPDU(Target target) {
		PDU request;
		if (target.getVersion() == SnmpConstants.version3) {
			request = new ScopedPDU();
			ScopedPDU scopedPDU = (ScopedPDU) request;
			scopedPDU.setContextEngineID(LOCAL_ENGINE_ID);
		} else {
			request = new PDU();
		}
		return request;
	}

	public VariableBinding getNext(OID oid) throws IOException {
		PDU pdu = createPDU(target);
		pdu.add(new VariableBinding(oid));
		pdu.setType(PDU.GETNEXT);
		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if (responsePDU == null) {
			throw new IOException("Timed out: " + target.getAddress().toString());
		}
		if (responsePDU.getErrorStatus() != PDU.noError) {
			throw new IOException(responsePDU.getErrorStatusText());
		}
		System.out.println(responsePDU);
		return (VariableBinding) responsePDU.getVariableBindings().firstElement();
	}

	public VariableBinding get(OID oid) throws IOException {
		PDU pdu = createPDU(target);
		pdu.add(new VariableBinding(oid));
		pdu.setType(PDU.GET);

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if (responsePDU.getErrorStatus() != PDU.noError) {
			throw new IOException(responsePDU.getErrorStatusText());
		}
		return (VariableBinding) responsePDU.getVariableBindings().firstElement();
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

	public Snmp getSnmp() {
		return snmp;
	}

	public Target getTarget() {
		return target;
	}

	public static void main(String[] args) {
		try {
			SnmpBackend snmp = new SnmpBackend(SnmpConstants.version2c, "public");
			snmp.setHost("192.168.1.1");
			snmp.open();
			VariableBinding varBind = snmp.getNext(new OID(".1"));
			System.out.println(varBind);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
