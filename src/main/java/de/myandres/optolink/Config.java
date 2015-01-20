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

public class Config {
	
    static Logger log = LoggerFactory.getLogger(Config.class);	

    private volatile String tty;
	private volatile int    port;
	private volatile String deviceType;
	private volatile String protocol;
	private volatile List<Telegram> telegramList;
	private volatile int telegramIt;
	
	Config(String fileName) throws Exception {
		telegramList = new ArrayList<Telegram>();
	      // create XMLReader 
	      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
	      
	      log.trace("Try to open File {} open",fileName);
	      // Pfad tho XML Datei
	      FileReader reader = new FileReader(fileName);
	      InputSource inputSource = new InputSource(reader);
	      
	      log.info("File {} open for parsing");

	      // set ContentHandler
	      xmlReader.setContentHandler(new xHandler());

	      // start parser
	      log.debug("Start parsing");
	      xmlReader.parse(inputSource);
	      log.info("{} Telegram's are parsed", telegramList.size());
	}
	
	public void addTelegram(Telegram t){
		t.setIndex(telegramList.size()); 
		telegramList.add(new Telegram(t));
	}
	
	public void updateTelegram(Telegram t){
		Telegram telegram = telegramList.get(t.getIndex());
		telegram =t;
		telegramList.set(t.getIndex(), telegram);
	}
	
	public Telegram getTelegramByName(String name){
		name=name.toLowerCase();
		for (int i=0; i<telegramList.size(); i++) {
			if (name.equals(telegramList.get(i).getName().toLowerCase())) return telegramList.get(i);
		}
	    return null;
	}
	
	public boolean existAddress(String address) {
		int adr= Integer.parseInt(address,16);
		for (int i=0; i<telegramList.size(); i++) {
			if (telegramList.get(i).getAddress() == adr ) return true;
		}
		return false;
	}
	
	public Telegram getTelegramByAddress(String address){
		int adr= Integer.parseInt(address,16);
		for (int i=0; i<telegramList.size(); i++) {
			if (telegramList.get(i).getAddress() == adr ) return telegramList.get(i);
		}
	    return null;
	}
	
	public int getTelegramSize() {
		return telegramList.size();
	}
	
	public String printTelegram(int index) {
		Telegram t=telegramList.get(index);
		String s = t.getName();
		return s;
	}
	
	public Telegram getFirstTelegram(){
		telegramIt=0;
		if (telegramList.size()>0) return telegramList.get(0);
		return null;
	}
	
	public Telegram getNextTelegram(){
		if (++telegramIt < telegramList.size()) return telegramList.get(telegramIt);
		return null;	
	}
	

	public String getTty() {
		return tty;
	}

	public void setPort(int port) {
		this.port=port;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getDevice() {
		return deviceType;
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	
	// Handler zum lesen der xml-Tags
	public class xHandler implements ContentHandler {
		
        private Telegram telegram = new Telegram();
		private String path;
		

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			String s = new String(ch, start, length);
			switch(path) {
			case "root.optolink.tty": tty=s; break;
			case "root.optolink.port": port=Integer.parseInt(s); break;	
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
			path=path.substring(0, path.lastIndexOf('.'));     			
		}

		@Override
		public void startDocument() throws SAXException {
			path="root";
			
		}

		@Override
		public void startElement(String uri, String localName, String pName,
				Attributes attr) throws SAXException {
			path=path+"."+localName;
			switch(path) {
			case "root.optolink"  : 
				deviceType=attr.getValue("device");
				protocol=attr.getValue("protocol");	
				break;
			case "root.optolink.telegram" : 
				telegram.setAddress(attr.getValue("address"));
				telegram.setName(attr.getValue("name"));
				telegram.setAccess(attr.getValue("access"));
				telegram.setType(attr.getValue("type"));
				telegram.setLength(attr.getValue("length"));
				telegram.setDiv(attr.getValue("div"));		
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
