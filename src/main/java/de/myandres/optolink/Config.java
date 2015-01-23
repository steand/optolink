package de.myandres.optolink;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Contains Data from xml-File 
 * This Data will be static only - dynamic data are stored in DataStore
 */

public class Config {

	static Logger log = LoggerFactory.getLogger(Config.class);

	private String tty;
	private int port = 12001; // default
	private int subscriberPort = 12002; // default
	private int interval = 5 * 60; // 5 Minutes
	private String deviceType;
	private String protocol;
	private List<Telegram> telegramList;

	Config(String fileName) throws Exception {
		telegramList = new ArrayList<Telegram>();
		// create XMLReader
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();

		log.debug("Try to open File {}", fileName);
		// Pfad tho XML Datei
		FileReader reader = new FileReader(fileName);
		InputSource inputSource = new InputSource(reader);

		log.info("File {} open for parsing", fileName);

		// set ContentHandler
		xmlReader.setContentHandler(new xHandler());

		// start parser
		log.debug("Start parsing");
		xmlReader.parse(inputSource);
		log.info("{} Telegram's are parsed", telegramList.size());
	}

	private void addTelegram(Telegram telegram) {
		Telegram t = new Telegram();
		t = telegram;
		t.setIndex(telegramList.size());
		telegramList.add(new Telegram(t));
	}

	public Telegram getTelegram(int address) {
		for (int i = 0; i < telegramList.size(); i++) {
			if (address == telegramList.get(i).getAddress())
				return telegramList.get(i);
		}
		return null;
	}

	public Telegram getTelegram(String address) {
		try {
			int addr = Integer.parseInt(address, 16);
			return getTelegram(addr);
		} catch (NumberFormatException e) {
			log.error("Invalid  Address format: {}", address);
			return null;
		}
	}

	public int getTelegramListSize() {
		return telegramList.size();
	}

	public Telegram getTelegramByIndex(int index) {
		try {
			return telegramList.get(index);
		} catch (IndexOutOfBoundsException e) {
			log.error("Invalid  Index ({}) in TelegramList", index);
			return null;
		}

	}

	public String viewTelegramDefinition(int index) {
		Telegram t = telegramList.get(index);
		String s = String.format("%04X", t.getAddress()) + ":";
		// OpenHAB Item Types
		switch (t.getType()) {
		case Telegram.INT:
			s += "Number";
			break;
		case Telegram.UINT:
			s += "Number";
			break;
		case Telegram.FLOAT:
			s += "Number";
			break;
		case Telegram.BOOLEAN:
			s += "Switch";
			break;
		case Telegram.DATE:
			s += "DateTime";
			break;
		default:
			s += "*unknown";
		}
		return s += "," + t.getAccessAsString() + "," + t.getName();
	}

	// Setter/Getter

	private void setTTY(String s) {
		tty = s;
		log.info("Set tty: {}", tty);
	}

	public String getTTY() {
		return tty;
	}

	private void setPort(String s) {
		try {
			port = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			log.error("Wrong Format for Port: {}", s);
		}
		log.info("Set Socket Port: {}", port);
	}

	public int getPort() {
		return port;
	}

	private void setInterval(String s) {
		try {
			interval = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			log.error("Wrong Format for Interval: {}", s);
		}
		log.info("Set polling interval: {} Seconds", interval);
	}

	public int getInterval() {
		return interval;
	}

	public String getDevice() {
		return deviceType;
	}

	public String getProtocol() {
		return protocol;
	}
	
	public int getSubscriberPort() {
		return subscriberPort;
	}

	private void setSubscriberPort(String subscriberPort) {
		try {
			this.subscriberPort = Integer.parseInt(subscriberPort);
		} catch (NumberFormatException e) {
			log.error("Wrong Format for subscriberPort: {}", subscriberPort);
		}
		log.info("Set Socket for subscriberPort: {}", this.subscriberPort);
	}

	
	
	public boolean existTelegram(int address) {
		for (int i=0; i<telegramList.size(); i++) { 
			log.trace("{}: {} = {}", i, address, telegramList.get(i).getAddress());
			if (address == telegramList.get(i).getAddress()) return true;
		}
		return false;
	} 
	
	public boolean existTelegram(String address) {
		try {
			int addr = Integer.parseInt(address, 16);
			log.trace("{} - > {}", address, addr);
			return existTelegram(addr);
		} catch (NumberFormatException e) {
			log.error("Invalid  Address format: {}", address);
			return false;
		}
	}
	
	// Handler for reading xml-Tags
	public class xHandler implements ContentHandler {

		private Telegram telegram = new Telegram();
		private String path;

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String s = new String(ch, start, length);
			switch (path) {
			case "root.optolink.tty":
				setTTY(s);
				break;
			case "root.optolink.port":
				setPort(s);
				break;
			case "root.optolink.interval":
				setInterval(s);
				
			case "root.optolink.subscriberPort":
				setSubscriberPort(s);
			}

		} 

		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void endElement(String uri, String localName, String pName)
				throws SAXException {

			if (localName.equals("telegram")) {
				addTelegram(telegram);
			}
			path = path.substring(0, path.lastIndexOf('.'));
		}

		@Override
		public void startDocument() throws SAXException {
			path = "root";

		}

		@Override
		public void startElement(String uri, String localName, String pName,
				Attributes attr) throws SAXException {
			path = path + "." + localName;
			switch (path) {
			case "root.optolink":
				deviceType = attr.getValue("device");
				protocol = attr.getValue("protocol");
				break;
			case "root.optolink.telegram":
				telegram.setAddress(attr.getValue("address"));
				telegram.setName(attr.getValue("name"));
				telegram.setAccess(attr.getValue("access"));
				telegram.setType(attr.getValue("type"));
				telegram.setLength(attr.getValue("length"));
				telegram.setDivider(attr.getValue("divider"));
				break;
			}

		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void processingInstruction(String target, String data)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void setDocumentLocator(Locator locator) {
			// TODO Auto-generated method stub

		}

		@Override
		public void skippedEntity(String name) throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
			// TODO Auto-generated method stub

		}

	} // Handler

}
