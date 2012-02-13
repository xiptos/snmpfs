package pt.ipb.snmpfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.CounterSupport;
import org.snmp4j.mp.DefaultCounterListener;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.TSM;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.TlsAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.tools.console.SnmpRequest;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TLSTM;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.PDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableListener;
import org.snmp4j.util.TableUtils;
import org.snmp4j.util.ThreadPool;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeListener;
import org.snmp4j.util.TreeUtils;

public class SnmpLL implements PDUFactory {

	public static final int DEFAULT = 0;
	public static final int WALK = 1;
	public static final int LISTEN = 2;
	public static final int TABLE = 3;
	public static final int CVS_TABLE = 4;
	public static final int TIME_BASED_CVS_TABLE = 5;

	Target target;
	Address address;
	OID authProtocol;
	OID privProtocol;
	OctetString privPassphrase;
	OctetString authPassphrase;
	OctetString community = new OctetString("public");
	OctetString authoritativeEngineID;
	OctetString contextEngineID;
	OctetString contextName = new OctetString();
	OctetString securityName = new OctetString();
	OctetString localEngineID = new OctetString(MPv3.createLocalEngineID());

	int version = SnmpConstants.version3;
	int engineBootCount = 0;
	int retries = 1;
	int timeout = 1000;
	int pduType = PDU.GETNEXT;
	int maxRepetitions = 10;
	int nonRepeaters = 0;
	int maxSizeResponsePDU = 65535;
	Vector<VariableBinding> vbs = new Vector<VariableBinding>();
	File snapshotFile;

	protected int operation = DEFAULT;

	int numDispatcherThreads = 2;

	boolean useDenseTableOperation = false;

	// table options
	OID lowerBoundIndex, upperBoundIndex;
	private List<VariableBinding> snapshot = new ArrayList<VariableBinding>();

	public SnmpLL() {
		// Set the default counter listener to return proper USM and MP error
		// counters.
		CounterSupport.getInstance().addCounterListener(new DefaultCounterListener());
	}

	public int getPduType() {
		return pduType;
	}

	public int getVersion() {
		return version;
	}

	public Vector<? extends VariableBinding> getVbs() {
		return vbs;
	}

	public boolean isUseDenseTableOperation() {
		return useDenseTableOperation;
	}

	public OID getUpperBoundIndex() {
		return upperBoundIndex;
	}

	public int getTimeout() {
		return timeout;
	}

	public Target getTarget() {
		return target;
	}

	public OctetString getSecurityName() {
		return securityName;
	}

	public int getRetries() {
		return retries;
	}

	public OID getPrivProtocol() {
		return privProtocol;
	}

	public OctetString getPrivPassphrase() {
		return privPassphrase;
	}

	public int getOperation() {
		return operation;
	}

	public int getNumDispatcherThreads() {
		return numDispatcherThreads;
	}

	public int getNonRepeaters() {
		return nonRepeaters;
	}

	public int getMaxRepetitions() {
		return maxRepetitions;
	}

	public OID getLowerBoundIndex() {
		return lowerBoundIndex;
	}

	public OctetString getContextName() {
		return contextName;
	}

	public OctetString getContextEngineID() {
		return contextEngineID;
	}

	public OctetString getCommunity() {
		return community;
	}

	public OctetString getAuthoritativeEngineID() {
		return authoritativeEngineID;
	}

	public OID getAuthProtocol() {
		return authProtocol;
	}

	public OctetString getAuthPassphrase() {
		return authPassphrase;
	}

	public Address getAddress() {
		return address;
	}

	private void checkOptions() {
		if ((operation == WALK) && ((pduType != PDU.GETBULK) && (pduType != PDU.GETNEXT))) {
			throw new IllegalArgumentException("Walk operation is not supported for PDU type: "
					+ PDU.getTypeString(pduType));
		} else if ((operation == WALK) && (vbs.size() != 1)) {
			throw new IllegalArgumentException("There must be exactly one OID supplied for walk operations");
		}
		if ((pduType == PDU.V1TRAP) && (version != SnmpConstants.version1)) {
			throw new IllegalArgumentException("V1TRAP PDU type is only available for SNMP version 1");
		}
	}

	public synchronized void listen() throws IOException {
		AbstractTransportMapping<? extends Address> transport;
		if (address instanceof TcpAddress) {
			transport = new DefaultTcpTransportMapping((TcpAddress) address);
		} else {
			transport = new DefaultUdpTransportMapping((UdpAddress) address);
		}
		ThreadPool threadPool = ThreadPool.create("DispatcherPool", numDispatcherThreads);
		MessageDispatcher mtDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

		// add message processing models
		mtDispatcher.addMessageProcessingModel(new MPv1());
		mtDispatcher.addMessageProcessingModel(new MPv2c());
		mtDispatcher.addMessageProcessingModel(new MPv3(localEngineID.getValue()));

		// add all security protocols
		SecurityProtocols.getInstance().addDefaultProtocols();
		SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

		Snmp snmp = new Snmp(mtDispatcher, transport);
		if (version == SnmpConstants.version3) {
			USM usm = new USM(SecurityProtocols.getInstance(), localEngineID, 0);
			SecurityModels.getInstance().addSecurityModel(usm);
			// Add the configured user to the USM
			addUsmUser(snmp);
		} else {
			CommunityTarget target = new CommunityTarget();
			target.setCommunity(community);
			this.target = target;
		}

		transport.listen();
		System.out.println("Listening on " + address);

		try {
			this.wait();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	private void addUsmUser(Snmp snmp) {
		snmp.getUSM().addUser(securityName,
				new UsmUser(securityName, authProtocol, authPassphrase, privProtocol, privPassphrase));
	}

	private Snmp createSnmpSession() throws IOException {
		AbstractTransportMapping<? extends Address> transport;
		if (address instanceof TlsAddress) {
			transport = new TLSTM();
		} else if (address instanceof TcpAddress) {
			transport = new DefaultTcpTransportMapping();
		} else {
			transport = new DefaultUdpTransportMapping();
		}
		// Could save some CPU cycles:
		// transport.setAsyncMsgProcessingSupported(false);
		Snmp snmp = new Snmp(transport);
		((MPv3) snmp.getMessageProcessingModel(MPv3.ID)).setLocalEngineID(localEngineID.getValue());

		if (version == SnmpConstants.version3) {
			USM usm = new USM(SecurityProtocols.getInstance(), localEngineID, engineBootCount);
			SecurityModels.getInstance().addSecurityModel(usm);
			addUsmUser(snmp);
			SecurityModels.getInstance().addSecurityModel(new TSM(localEngineID, false));
		}
		return snmp;
	}

	private Target createTarget() {
		if (version == SnmpConstants.version3) {
			UserTarget target = new UserTarget();
			if (authPassphrase != null) {
				if (privPassphrase != null) {
					target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
				} else {
					target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
				}
			} else {
				target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
			}
			target.setSecurityName(securityName);
			if (authoritativeEngineID != null) {
				target.setAuthoritativeEngineID(authoritativeEngineID.getValue());
			}
			if (address instanceof TlsAddress) {
				target.setSecurityModel(TSM.SECURITY_MODEL_TSM);
			}
			return target;
		} else {
			CommunityTarget target = new CommunityTarget();
			target.setCommunity(community);
			return target;
		}
	}

	public PDU send() throws IOException {
		checkOptions();
		Snmp snmp = createSnmpSession();
		this.target = createTarget();
		target.setVersion(version);
		target.setAddress(address);
		target.setRetries(retries);
		target.setTimeout(timeout);
		target.setMaxSizeRequestPDU(maxSizeResponsePDU);
		snmp.listen();

		PDU request = createPDU(target);
		if (request.getType() == PDU.GETBULK) {
			request.setMaxRepetitions(maxRepetitions);
			request.setNonRepeaters(nonRepeaters);
		}
		for (VariableBinding vb : vbs) {
			request.add(vb);
		}

		PDU response = null;
		if ((operation == WALK)) {
			walk(snmp, request, target);
			return null;
		} else {
			ResponseEvent responseEvent;
			long startTime = System.nanoTime();
			responseEvent = snmp.send(request, target);
			if (responseEvent != null) {
				response = responseEvent.getResponse();
				System.out.println("Received response after " + (System.nanoTime() - startTime) / 1000000
						+ " milliseconds");
			}
		}
		snmp.close();
		return response;
	}

	private void walk(Snmp snmp, PDU request, Target target) throws IOException {
		snapshot.clear();
		request.setNonRepeaters(0);
		OID rootOID = request.get(0).getOid();
		final WalkCounts counts = new WalkCounts();
		final long startTime = System.nanoTime();
		TreeUtils treeUtils = new TreeUtils(snmp, this);
		TreeListener treeListener = new TreeListener() {

			private boolean finished;

			public boolean next(TreeEvent e) {
				counts.requests++;
				if (e.getVariableBindings() != null) {
					VariableBinding[] vbs = e.getVariableBindings();
					counts.objects += vbs.length;
					for (VariableBinding vb : vbs) {
						snapshot.add(vb);
					}
				}
				return true;
			}

			public void finished(TreeEvent e) {
				if ((e.getVariableBindings() != null) && (e.getVariableBindings().length > 0)) {
					next(e);
				}
				System.out.println();
				System.out.println("Total requests sent:    " + counts.requests);
				System.out.println("Total objects received: " + counts.objects);
				System.out.println("Total walk time:        " + (System.nanoTime() - startTime) / 1000000
						+ " milliseconds");
				if (e.isError()) {
					System.err.println("The following error occurred during walk:");
					System.err.println(e.getErrorMessage());
				}
				finished = true;
				synchronized (this) {
					this.notify();
				}
			}

			public boolean isFinished() {
				return finished;
			}

		};
		synchronized (treeListener) {
			treeUtils.getSubtree(target, rootOID, null, treeListener);
			try {
				treeListener.wait();
			} catch (InterruptedException ex) {
				System.err.println("Tree retrieval interrupted: " + ex.getMessage());
				Thread.currentThread().interrupt();
			}
		}
	}

	public List<VariableBinding> getSnapshot() {
		return snapshot;
	}

	public void setAddress(String transport, String host, int port) {
		if (transport.equalsIgnoreCase("udp")) {
			setAddress(new UdpAddress(host + "/" + port));
		} else if (transport.equalsIgnoreCase("tcp")) {
			setAddress(new TcpAddress(host + "/" + port));
		} else if (transport.equalsIgnoreCase("tls")) {
			setAddress(new TlsAddress(host + "/" + port));
		} else {
			throw new IllegalArgumentException("Unknown transport " + transport);
		}
	}

	public void setAddress(String transportAddress) {
		String transport = "udp";
		int colon = transportAddress.indexOf(':');
		if (colon > 0) {
			transport = transportAddress.substring(0, colon);
			transportAddress = transportAddress.substring(colon + 1);
		}
		// set default port
		if (transportAddress.indexOf('/') < 0) {
			transportAddress += "/161";
		}
		if (transport.equalsIgnoreCase("udp")) {
			setAddress(new UdpAddress(transportAddress));
		} else if (transport.equalsIgnoreCase("tcp")) {
			setAddress(new TcpAddress(transportAddress));
		} else if (transport.equalsIgnoreCase("tls")) {
			setAddress(new TlsAddress(transportAddress));
		}
		throw new IllegalArgumentException("Unknown transport " + transport);
	}

	private static OctetString createOctetString(String s) {
		OctetString octetString;
		if (s.startsWith("0x")) {
			octetString = OctetString.fromHexString(s.substring(2), ':');
		} else {
			octetString = new OctetString(s);
		}
		return octetString;
	}

	protected static void printVariableBindings(PDU response) {
		for (int i = 0; i < response.size(); i++) {
			VariableBinding vb = response.get(i);
			System.out.println(vb.toString());
		}
	}

	protected static void printReport(PDU response) {
		if (response.size() < 1) {
			System.out.println("REPORT PDU does not contain a variable binding.");
			return;
		}

		VariableBinding vb = response.get(0);
		OID oid = vb.getOid();
		if (SnmpConstants.usmStatsUnsupportedSecLevels.equals(oid)) {
			System.out.print("REPORT: Unsupported Security Level.");
		} else if (SnmpConstants.usmStatsNotInTimeWindows.equals(oid)) {
			System.out.print("REPORT: Message not within time window.");
		} else if (SnmpConstants.usmStatsUnknownUserNames.equals(oid)) {
			System.out.print("REPORT: Unknown user name.");
		} else if (SnmpConstants.usmStatsUnknownEngineIDs.equals(oid)) {
			System.out.print("REPORT: Unknown engine id.");
		} else if (SnmpConstants.usmStatsWrongDigests.equals(oid)) {
			System.out.print("REPORT: Wrong digest.");
		} else if (SnmpConstants.usmStatsDecryptionErrors.equals(oid)) {
			System.out.print("REPORT: Decryption error.");
		} else if (SnmpConstants.snmpUnknownSecurityModels.equals(oid)) {
			System.out.print("REPORT: Unknown security model.");
		} else if (SnmpConstants.snmpInvalidMsgs.equals(oid)) {
			System.out.print("REPORT: Invalid message.");
		} else if (SnmpConstants.snmpUnknownPDUHandlers.equals(oid)) {
			System.out.print("REPORT: Unknown PDU handler.");
		} else if (SnmpConstants.snmpUnavailableContexts.equals(oid)) {
			System.out.print("REPORT: Unavailable context.");
		} else if (SnmpConstants.snmpUnknownContexts.equals(oid)) {
			System.out.print("REPORT: Unknown context.");
		} else {
			System.out.print("REPORT contains unknown OID (" + oid.toString() + ").");
		}
		System.out.println(" Current counter value is " + vb.getVariable().toString() + ".");
	}

	public synchronized void processPdu(CommandResponderEvent e) {
		PDU command = e.getPDU();
		if (command != null) {
			System.out.println(command.toString());
			if ((command.getType() != PDU.TRAP) && (command.getType() != PDU.V1TRAP)
					&& (command.getType() != PDU.REPORT) && (command.getType() != PDU.RESPONSE)) {
				command.setErrorIndex(0);
				command.setErrorStatus(0);
				command.setType(PDU.RESPONSE);
				StatusInformation statusInformation = new StatusInformation();
				StateReference ref = e.getStateReference();
				try {
					e.getMessageDispatcher().returnResponsePdu(e.getMessageProcessingModel(), e.getSecurityModel(),
							e.getSecurityName(), e.getSecurityLevel(), command, e.getMaxSizeResponsePDU(), ref,
							statusInformation);
				} catch (MessageException ex) {
					System.err.println("Error while sending response: " + ex.getMessage());
					LogFactory.getLogger(SnmpRequest.class).error(ex);
				}
			}
		}
	}

	public PDU createPDU(Target target) {
		PDU request;
		if (target.getVersion() == SnmpConstants.version3) {
			request = new ScopedPDU();
			ScopedPDU scopedPDU = (ScopedPDU) request;
			if (contextEngineID != null) {
				scopedPDU.setContextEngineID(contextEngineID);
			}
			if (contextName != null) {
				scopedPDU.setContextName(contextName);
			}
		} else {
			request = new PDU();
		}
		request.setType(pduType);
		return request;
	}

	public void table() throws IOException {
		Snmp snmp = createSnmpSession();
		this.target = createTarget();
		target.setVersion(version);
		target.setAddress(address);
		target.setRetries(retries);
		target.setTimeout(timeout);
		snmp.listen();

		TableUtils tableUtils = new TableUtils(snmp, this);
		tableUtils.setMaxNumRowsPerPDU(maxRepetitions);
		Counter32 counter = new Counter32();

		OID[] columns = new OID[vbs.size()];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = (vbs.get(i)).getOid();
		}
		long startTime = System.nanoTime();
		synchronized (counter) {

			TableListener listener;
			if (operation == TABLE) {
				listener = new TextTableListener();
			} else {
				listener = new CVSTableListener(System.nanoTime());
			}
			if (useDenseTableOperation) {
				tableUtils.getDenseTable(target, columns, listener, counter, lowerBoundIndex, upperBoundIndex);
			} else {
				tableUtils.getTable(target, columns, listener, counter, lowerBoundIndex, upperBoundIndex);
			}
			try {
				counter.wait(timeout);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
		System.out.println("Table received in " + (System.nanoTime() - startTime) / 1000000 + " milliseconds.");
		snmp.close();
	}

	class MyTableListener implements TableListener {
		boolean finished = false;

		@Override
		public void finished(TableEvent event) {
			finished = true;
			synchronized (event.getUserObject()) {
				event.getUserObject().notify();
			}
		}

		@Override
		public boolean isFinished() {
			return finished;
		}

		@Override
		public boolean next(TableEvent event) {
			if (event.isError()) {
				throw new RuntimeException(event.getErrorMessage());
			}
			for (VariableBinding vb : event.getColumns()) {
				System.out.print(vb.toString());
			}
			System.out.println();
			return true;
		}

	}

	class CVSTableListener implements TableListener {

		private long requestTime;
		private boolean finished;

		public CVSTableListener(long time) {
			this.requestTime = time;
		}

		public boolean next(TableEvent event) {
			if (operation == TIME_BASED_CVS_TABLE) {
				System.out.print(requestTime);
				System.out.print(",");
			}
			System.out.print("\"" + event.getIndex() + "\",");
			for (int i = 0; i < event.getColumns().length; i++) {
				Variable v = event.getColumns()[i].getVariable();
				String value = v.toString();
				switch (v.getSyntax()) {
				case SMIConstants.SYNTAX_OCTET_STRING: {
					StringBuffer escapedString = new StringBuffer(value.length());
					StringTokenizer st = new StringTokenizer(value, "\"", true);
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						escapedString.append(token);
						if (token.equals("\"")) {
							escapedString.append("\"");
						}
					}
				}
				case SMIConstants.SYNTAX_IPADDRESS:
				case SMIConstants.SYNTAX_OBJECT_IDENTIFIER:
				case SMIConstants.SYNTAX_OPAQUE: {
					System.out.print("\"");
					System.out.print(value);
					System.out.print("\"");
					break;
				}
				default: {
					System.out.print(value);
				}
				}
				if (i + 1 < event.getColumns().length) {
					System.out.print(",");
				}
			}
			System.out.println();
			return true;
		}

		public void finished(TableEvent event) {
			finished = true;
			synchronized (event.getUserObject()) {
				event.getUserObject().notify();
			}
		}

		public boolean isFinished() {
			return finished;
		}

	}

	class TextTableListener implements TableListener {

		private boolean finished;

		public void finished(TableEvent event) {
			System.out.println();
			System.out.println("Table walk completed with status " + event.getStatus() + ". Received "
					+ event.getUserObject() + " rows.");
			finished = true;
			synchronized (event.getUserObject()) {
				event.getUserObject().notify();
			}
		}

		public boolean next(TableEvent event) {
			System.out.println("Index = " + event.getIndex() + ":");
			for (int i = 0; i < event.getColumns().length; i++) {
				System.out.println(event.getColumns()[i]);
			}
			System.out.println();
			((Counter32) event.getUserObject()).increment();
			return true;
		}

		public boolean isFinished() {
			return finished;
		}

	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void clearVbs() {
		this.vbs.clear();
	}

	public void addVb(String oid) {
		this.vbs.add(new VariableBinding(new OID(oid)));
	}

	public void setVb(String oid) {
		this.vbs.clear();
		addVb(oid);
	}

	public void addVb(VariableBinding vb) {
		this.vbs.add(vb);
	}

	public void setVbs(Vector<VariableBinding> vbs) {
		this.vbs = vbs;
	}

	public void setUseDenseTableOperation(boolean useDenseTableOperation) {
		this.useDenseTableOperation = useDenseTableOperation;
	}

	public void setUpperBoundIndex(OID upperBoundIndex) {
		this.upperBoundIndex = upperBoundIndex;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setTarget(Target target) {
		this.target = target;
	}

	public void setSecurityName(String securityName) {
		setSecurityName(createOctetString(securityName));
	}

	public void setSecurityName(OctetString securityName) {
		this.securityName = securityName;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public void setPrivProtocol(OID privProtocol) {
		this.privProtocol = privProtocol;
	}

	public void setPrivPassphrase(String privPassphrase) {
		setPrivPassphrase(createOctetString(privPassphrase));
	}

	public void setPrivPassphrase(OctetString privPassphrase) {
		this.privPassphrase = privPassphrase;
	}

	public void setPduType(int pduType) {
		this.pduType = pduType;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}

	public void setNumDispatcherThreads(int numDispatcherThreads) {
		this.numDispatcherThreads = numDispatcherThreads;
	}

	public void setNonRepeaters(int nonRepeaters) {
		this.nonRepeaters = nonRepeaters;
	}

	public void setMaxRepetitions(int maxRepetitions) {
		this.maxRepetitions = maxRepetitions;
	}

	public void setLowerBoundIndex(OID lowerBoundIndex) {
		this.lowerBoundIndex = lowerBoundIndex;
	}

	public void setContextName(String contextName) {
		setContextName(createOctetString(contextName));
	}

	public void setContextName(OctetString contextName) {
		this.contextName = contextName;
	}

	public void setContextEngineID(OctetString contextEngineID) {
		this.contextEngineID = contextEngineID;
	}

	public void setCommunity(String community) {
		setCommunity(createOctetString(community));
	}

	public void setCommunity(OctetString community) {
		this.community = community;
	}

	public void setAuthoritativeEngineID(String authoritativeEngineID) {
		setAuthoritativeEngineID(createOctetString(authoritativeEngineID));
	}

	public void setAuthoritativeEngineID(OctetString authoritativeEngineID) {
		this.authoritativeEngineID = authoritativeEngineID;
	}

	public void setAuthProtocol(OID authProtocol) {
		this.authProtocol = authProtocol;
	}

	public void setAuthPassphrase(String authPassphrase) {
		setAuthPassphrase(createOctetString(authPassphrase));
	}

	public void setAuthPassphrase(OctetString authPassphrase) {
		this.authPassphrase = authPassphrase;
	}

	class WalkCounts {
		public int requests;
		public int objects;
	}

	public static void main(String[] args) {
		SnmpLL ll = new SnmpLL();
		ll.setAddress("udp", "192.168.1.1", 161);
		ll.setCommunity("public");
		ll.addVb(".1.3.6");
		ll.setPduType(PDU.GETNEXT);
		ll.setVersion(SnmpConstants.version2c);

		try {
			PDU response = ll.send();
			printReport(response);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ll.setOperation(WALK);
		// ll.setPduType(PDU.GETBULK);
		// try {
		// ll.send();
		// for(VariableBinding vb : ll.getSnapshot()) {
		// System.out.println(vb.toString());
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		//ll.setVb("1.3.6.1.2.1.2.2");
		ll.setVb("1.3.6.1.2.1.4.21");
		ll.setOperation(TABLE);
		ll.setPduType(PDU.GETBULK);
		try {
			ll.table();
			for (VariableBinding vb : ll.getSnapshot()) {
				System.out.println(vb.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * try { SnmpRequest snmpRequest = new SnmpRequest(args); if
		 * (snmpRequest.operation == SNAPSHOT_DUMP) {
		 * snmpRequest.dumpSnapshot(); } else { try { if (snmpRequest.operation
		 * == LISTEN) { snmpRequest.listen(); } else if ((snmpRequest.operation
		 * == TABLE) || (snmpRequest.operation == CVS_TABLE) ||
		 * (snmpRequest.operation == TIME_BASED_CVS_TABLE)) {
		 * snmpRequest.table(); } else { PDU response = snmpRequest.send(); if
		 * ((snmpRequest.getPduType() == PDU.TRAP) || (snmpRequest.getPduType()
		 * == PDU.REPORT) || (snmpRequest.getPduType() == PDU.V1TRAP) ||
		 * (snmpRequest.getPduType() == PDU.RESPONSE)) {
		 * System.out.println(PDU.getTypeString(snmpRequest.getPduType()) +
		 * " sent successfully"); } else if (response == null) { if
		 * (snmpRequest.operation != WALK) {
		 * System.out.println("Request timed out."); } } else if
		 * (response.getType() == PDU.REPORT) { printReport(response); } else if
		 * (snmpRequest.operation == DEFAULT) {
		 * System.out.println("Response received with requestID=" +
		 * response.getRequestID() + ", errorIndex=" + response.getErrorIndex()
		 * + ", " + "errorStatus=" + response.getErrorStatusText() + "(" +
		 * response.getErrorStatus() + ")"); printVariableBindings(response); }
		 * else { System.out.println("Received something strange: requestID=" +
		 * response.getRequestID() + ", errorIndex=" + response.getErrorIndex()
		 * + ", " + "errorStatus=" + response.getErrorStatusText() + "(" +
		 * response.getErrorStatus() + ")"); printVariableBindings(response); }
		 * } } catch (IOException ex) {
		 * System.err.println("Error while trying to send request: " +
		 * ex.getMessage()); ex.printStackTrace(); } } } catch
		 * (IllegalArgumentException iaex) { System.err.print("Error: " +
		 * iaex.getMessage()); iaex.printStackTrace(); }
		 */
	}

}
