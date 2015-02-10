package de.myandres.optolink;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Viessmann {
	static Logger log = LoggerFactory.getLogger(ComPort.class);
	private Config config;
	private ViessmannProtocol viessmannProtocol;

	Viessmann(Config config) throws Exception {
		log.debug("Try to init Viessmann Handler");
		this.config = config;
		switch (config.getProtocol()) {
		case "300":
			viessmannProtocol = new Viessmann300(config.getTTY(),
					config.getTtyTimeOut());
			break;
		case "KW":
			viessmannProtocol = new ViessmannKW(config.getTTY(),
					config.getTtyTimeOut());
			break;
		default:
			log.error("Unknown Protokol for Optolink: {}", config.getProtocol());
			throw new Exception();
		}
		log.info("Viessmann Handler for Protocol {} initalisiert");
	}
	
	public  String get(String address) {
		log.trace("Try to get Data for address: {}", address);
		Telegram t = config.getTelegram(address);
		String s;
		if (t != null) {
			viessmannProtocol.startSession();
			s=address + ":" + readData(t);
			viessmannProtocol.stopSession();
			return s;
		} else {
			log.error("Can't get Data for address: {} - address not exist", address);
		}
		return null;
	}
	
	public synchronized String set(String address, String value) {

			log.info("Set not implemented jet");
			return null;

	}
	
	
	public synchronized String readData(String[] address) {
		String s = "";
		viessmannProtocol.startSession();
		int i=0;
		while ((i < address.length) && (address[i] != null)) {
			Telegram t = config.getTelegram(address[i]);
			if (t != null) {
				s+= address[i] + ":" + readData(t) + "\n";
			} else {
				log.error("Can't get Data for address: {} - address not exist", address[i]);
			}
			i++;
		}
		viessmannProtocol.stopSession();
		return s.substring(0, s.length()-1);
	}
	
	private synchronized String readData(Telegram t)  {
		byte [] buffer = new byte[16];
		long l=0;
		int len=viessmannProtocol.getData(buffer,t.getAddress(), t.getLength());
		if (log.isTraceEnabled()) {
	    	log.trace("Number of Bytes: {}", len);
	    	for (int i=0; i<len; i++) log.trace("[{}] {} ",i,buffer[i]);
		}
		switch (t.getType()) {
		case Telegram.BOOLEAN:
			if (buffer[0] == 0) return "OFF";
			return "ON";
		case Telegram.DATE:
			//TODO check it
			return String.format("value=\"%02x.%02x.%02x%02x %02x:%02x:%02x\"",
					buffer[3],buffer[2],buffer[0],buffer[1],buffer[5],buffer[6],buffer[7])	;
		case Telegram.BYTE:
             l = buffer[0];
             break;
		case Telegram.UBYTE:
			  l = 0xFF & buffer[0];
			break;		
		case Telegram.SHORT:
			 l = ((long)(buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
			break;
		case Telegram.USHORT:
			l = ((long)(0xFF & buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
			break;
		case Telegram.INT:
			l = ((long)(buffer[3]))*0x1000000  + ((long)(0xFF & buffer[2]))*0x10000  + ((long)(0xFF & buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
			break;
		case Telegram.UINT:
			l = ((long)(0xFF & buffer[3]))*0x1000000  + ((long)(0xFF & buffer[2]))*0x10000  + ((long)(0xFF & buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
			break;
		}
		if (t.getDivider() !=1 ) 
			return String.format(Locale.US,"value=%.2f", (float)l / t.getDivider());
		else return String.format("value=%d", l);
		
	} 

}
