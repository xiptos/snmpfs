package pt.ipb.snmpfs.prefs;

public class SnmpPrefs {
	public enum Version {
		V1, V2C, V3
	}

	public enum AuthProto {
		NONE, MD5, SHA
	}

	public enum PrivProto {
		NONE, SHA, DES, AES, AES128, AES192, AES256, TRIPDES, DESEDE
	}

	String userName;

	String authPassphrase = null;

	String privPassphrase = null;

	String host = "127.0.0.1";

	int port = 161;

	AuthProto authProtocol = AuthProto.NONE;

	PrivProto privProtocol = PrivProto.NONE;

	Version version = Version.V2C;

	String community;

	String context;

	public SnmpPrefs() {
	}

	public SnmpPrefs(String userName, AuthProto authProto, String authPass,
			PrivProto privProto, String privPass) {
		setVersion(Version.V3);
		setUserName(userName);
		setAuthPassphrase(authPass);
		setPrivPassphrase(privPass);
		setAuthProtocol(authProto);
		setPrivProtocol(privProto);
	}

	public SnmpPrefs(String userName, String authPass, String privPass) {
		this(userName, AuthProto.MD5, authPass, PrivProto.DES, privPass);
	}

	public SnmpPrefs(String userName, String authPass) {
		this(userName, AuthProto.MD5, authPass, PrivProto.NONE, null);
	}

	public SnmpPrefs(String userName) {
		this(userName, AuthProto.NONE, null, PrivProto.NONE, null);
	}

	public SnmpPrefs(Version version, String community) {
		setVersion(version);
		setCommunity(community);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAuthPassphrase() {
		return authPassphrase;
	}

	public void setAuthPassphrase(String authPassphrase) {
		this.authPassphrase = authPassphrase;
	}

	public String getPrivPassphrase() {
		return privPassphrase;
	}

	public void setPrivPassphrase(String privPassphrase) {
		this.privPassphrase = privPassphrase;
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

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getContext() {
		return context;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("\tVersion: " + getVersion() + "\n");
		str.append("\tAuthPassphrase: " + getAuthPassphrase() + "\n");
		str.append("\tCommunity: " + getCommunity() + "\n");
		str.append("\tHost: " + getHost() + "\n");
		str.append("\tPort: " + getPort() + "\n");
		str.append("\tPrivPassphrase: " + getPrivPassphrase() + "\n");
		str.append("\tUserName: " + getUserName() + "\n");
		str.append("\tAuthProtocol: " + getAuthProtocol() + "\n");
		str.append("\tPrivProtocol: " + getPrivProtocol() + "\n");
		str.append("\tContext: " + getContext() + "\n");

		return str.toString();
	}
}
