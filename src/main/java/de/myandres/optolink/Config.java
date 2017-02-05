/*******************************************************************************
 * Copyright (c) 2015,  Stefan Andres.  All rights reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *******************************************************************************/
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

	private String adapterID="TEST"; 
	private String tty;
	private String ttyIP;
	private Integer ttyPort;
	private String ttyType;
	private int ttyTimeOut = 2000;      //default
	private int port = 31113;           // default: unassigned Port. See: http://www.iana.org
	private String deviceType;
	private String protocol;
	private List<Thing> thingList;



	Config(String fileName) throws Exception {
		thingList = new ArrayList<Thing>();
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
		log.info("{} Things are parsed", thingList.size());
	}
	
	
	public List<Thing> getThingList() {
		return thingList;
	}

	private void addThing(Thing thing) {
		log.trace("Add thing id: {}", thing.getId());
		thingList.add(new Thing(thing));
	}

	public Thing getThing(String id) {
		log.trace("get thing id: {}", id);
		for (int i = 0; i < thingList.size(); i++) {
			if (thingList.get(i).getId().equals(id))  return thingList.get(i);
		}
		log.error("Add thing id: {} not found", id);
		return null;
	}
	

	private void setAdapterID(String s) {
		adapterID = s;
		log.info("Set adapterID: {}", adapterID);
	}

	public String getAdapterID() {
		return adapterID;
	}
	
	private void setTTY(String s) {
		tty = s;
		log.info("Set tty: {}", tty);
	}

	public String getTTY() {
		return tty;
	}

	private void setTTYType(String s) {
		ttyType = s;
		log.info("Set ttyType: {}", ttyType);
	}

	public String getTTYType() {
		return ttyType;
	}

	private void setTTYIP (String s) {
		ttyIP = s;
		log.info("Set ttyIP: {}", ttyIP);
	}

	public String getTTYIP() {
		return ttyIP;
	}

	private void setTTYPort(String s) {
		try {
			ttyPort = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			log.error("Wrong Format for Port: {}", s);
		}
		log.info("Set TTY Port: {}", ttyPort);
	}

	public int getTTYPort() {
		return ttyPort;
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

	
	private void setTtyTimeOut(String s) {
		try {
			ttyTimeOut = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			log.error("Wrong Format for TTY Timeout: {}", s);
		}
		log.info("Set TTY Timeout: {} Milliseconds", ttyTimeOut);
	}

	public int getTtyTimeOut() {
		return ttyTimeOut;
	}


	public String getDeviceType() {
		return deviceType;
	}

	public String getProtocol() {
		return protocol;
	}

	
	// Handler for reading xml-Tags
	public class xHandler implements ContentHandler {

		private Thing thing = null;
		private Channel channel = null;
		private String path;
		private String[] urlPort;
	    final String IPADDRESS_PATTERN =
	    		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	    		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	    		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	    		"([01]?\\d\\d?|2[0-4]\\d|25[0-5]):([0-9]{1,5})$";

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String s = new String(ch, start, length);
			switch (path) {
			case "root.optolink.tty":
				if (s.matches(IPADDRESS_PATTERN)) { 	// device is at an URL
					setTTYType ("URL");
					setTTY(s);
					urlPort = s.split(":");
					setTTYIP (urlPort[0]);
					setTTYPort (urlPort[1]);
				} else {								// device is local
					setTTYType ("GPIO");
					setTTY(s);
				}
				break;
			case "root.optolink.ttytimeout":
				setTtyTimeOut(s);
				break;
			case "root.optolink.port":
				setPort(s);
				break;
			case "root.optolink.adapterID":
				setAdapterID(s);
				break;		
			case "root.optolink.thing.description":
				thing.setDescription(s); 
				break;	
			case "root.optolink.thing.channel.description":
				channel.setDescription(s); 
				break;	
			}

		} 

		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void endElement(String uri, String localName, String pName)
				throws SAXException {

			if (localName.equals("thing")) {
				addThing(thing);
			}
			if (localName.equals("channel")) {
				thing.addChannel(channel);;
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
			case "root.optolink.thing":
				thing = new Thing(attr.getValue("id"), attr.getValue("type")); 
				break;
			case "root.optolink.thing.channel":
				channel = new Channel (attr.getValue("id"));
				break;
			case "root.optolink.thing.channel.telegram":
				channel.setTelegram(new Telegram(attr.getValue("address"), 
						                         attr.getValue("type"), 
						                         attr.getValue("divider")));
				break;
				
			}

		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			// Not use Auto-generated method stub

		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length)
				throws SAXException {
			// Not use  Auto-generated method stub

		}

		@Override
		public void processingInstruction(String target, String data)
				throws SAXException {
			// Not use  Auto-generated method stub

		}

		@Override
		public void setDocumentLocator(Locator locator) {
			// Not use  Auto-generated method stub

		}

		@Override
		public void skippedEntity(String name) throws SAXException {
			// Not use  Auto-generated method stub

		}

		@Override
		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
			// Not use  Auto-generated method stub

		}

	} // Handler

}
