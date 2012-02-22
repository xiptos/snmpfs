package pt.ipb.snmpfs.prefs;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler {

	protected Device device;
	private Table table;

	public XmlHandler() {
		this.device = new Device();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ("device".equals(qName)) {
			device = new Device();
			String address = attributes.getValue("address");
			String name = attributes.getValue("name");
			device.setAddress(address);
			device.setName(name);

		} else if ("mount".equals(qName)) {
			String dir = attributes.getValue("dir");
			device.setMountDir(dir);

		} else if ("mibs".equals(qName)) {
			String dir = attributes.getValue("dir");
			device.setMibDir(dir);

		} else if ("mib".equals(qName)) {
			String mib = attributes.getValue("file");
			device.addMib(mib);

		} else if ("entries".equals(qName)) {

		} else if ("scalar".equals(qName)) {
			String node = attributes.getValue("node");
			String oid = attributes.getValue("oid");
			Entry entry = new Entry(node, oid);

			device.addEntry(entry);

		} else if ("table".equals(qName)) {
			String node = attributes.getValue("node");
			String oid = attributes.getValue("oid");
			table = new Table(node, oid);

		} else if ("col".equals(qName)) {
			String node = attributes.getValue("node");
			String oid = attributes.getValue("oid");
			Entry entry = new Entry(node, oid);

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
