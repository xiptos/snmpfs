package pt.ipb.snmpfs.prefs;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import pt.ipb.marser.type.OID;

public class XmlHandler extends DefaultHandler {

	protected Device device;
	private Table table;

	public XmlHandler() {
		this.device = new Device();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ("device".equals(qName)) {
			device = new Device();
			String name = attributes.getValue("name");
			device.setName(name);

		} else if ("mount".equals(qName)) {
			String dir = attributes.getValue("dir");
			device.setMountDir(dir);

		} else if ("mibs".equals(qName)) {
			String dir = attributes.getValue("dir");
			device.setMibDir(dir);

		} else if ("snmp".equals(qName)) {
			String address = attributes.getValue("address");
			String port = attributes.getValue("port");
			String user = attributes.getValue("user");
			String community = attributes.getValue("community");
			String authProtocol = attributes.getValue("authProtocol");
			String version = attributes.getValue("version");
			String privProtocol = attributes.getValue("privProtocol");
			String authPassphrase = attributes.getValue("authPassphrase");
			String privPassphrase = attributes.getValue("privPassphrase");
			String context = attributes.getValue("context");

			SnmpPrefs prefs = new SnmpPrefs(user);
			prefs.setHost(address);
			prefs.setPort(Integer.parseInt(port));
			if (authPassphrase != null) {
				prefs.setAuthPassphrase(authPassphrase);
			}
			if (authProtocol != null) {
				prefs.setAuthProtocol(SnmpPrefs.AuthProto.valueOf(authProtocol.toUpperCase()));
			}
			if (privProtocol != null) {
				prefs.setPrivProtocol(SnmpPrefs.PrivProto.valueOf(privProtocol.toUpperCase()));
			}
			if (community != null) {
				prefs.setCommunity(community);
			}
			if (privPassphrase != null) {
				prefs.setPrivPassphrase(privPassphrase);
			}
			if (version != null) {
				prefs.setVersion(SnmpPrefs.Version.valueOf(version.toUpperCase()));
			}
			if (context != null) {
				prefs.setContext(context);
			}
			device.setSnmpPrefs(prefs);

		} else if ("mib".equals(qName)) {
			String mib = attributes.getValue("file");
			device.addMib(mib);

		} else if ("entries".equals(qName)) {

		} else if ("scalar".equals(qName)) {
			String label = attributes.getValue("label");
			String oid = attributes.getValue("oid");
			String file = attributes.getValue("file");
			Entry entry = new Entry(label, OID.parseOID(oid), file);

			device.addEntry(entry);

		} else if ("table".equals(qName)) {
			String label = attributes.getValue("label");
			String oid = attributes.getValue("oid");
			String file = attributes.getValue("file");
			table = new Table(label, OID.parseOID(oid), file);

		} else if ("col".equals(qName)) {
			String label = attributes.getValue("label");
			String oid = attributes.getValue("oid");
			Entry entry = new Entry(label, OID.parseOID(oid));

			table.addCol(entry);
		}
	}

	public Device getDevice() {
		return device;
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ("table".equals(qName)) {
			device.addEntry(table);
			table = null;
		}
	}

	public static Device parse(InputStream is) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		XmlHandler handler = new XmlHandler();

		saxParser.parse(is, handler);

		return handler.getDevice();
	}

}
