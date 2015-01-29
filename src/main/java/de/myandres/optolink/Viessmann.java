package de.myandres.optolink;

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
	
	public synchronized String get(String address) {
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
	
	
	public synchronized String get(String[] address) {
		String s = "";
		viessmannProtocol.startSession();
		for (int i = 0; i < address.length; i++) {
			Telegram t = config.getTelegram(address[i]);
			if (t != null) {
				s+= address + ":" + readData(t) + ",";
			} else {
				log.error("Can't get Data for address: {} - address not exist", address[i]);
			}
		}
		viessmannProtocol.stopSession();
		return s.substring(0, s.length()-1);
	}
	
	private synchronized String readData(Telegram t)  {
		byte [] buffer = new byte[16];
		long l=0;
		viessmannProtocol.getData(buffer,t.getAddress(), t.getLength());
		switch (t.getType()) {
		case Telegram.BOOLEAN:
			if (buffer[0] == 0) return "OFF";
			return "ON";
		case Telegram.DATE:
			// ?????
			return String.format("%2d.%2d.%2d%2d %2d:%2d:%2d",
					buffer[0],buffer[1],buffer[2],buffer[3],buffer[4],buffer[5],buffer[6])	;
		case Telegram.BYTE:
             l = buffer[0];
             break;
		case Telegram.UBYTE:
			  l = 0xFF & buffer[0];
			break;		
		case Telegram.SHORT:
			 l = ((long)(buffer[1]))<<8  & (long)(buffer[0]);
			break;
		case Telegram.USHORT:
			l = ((long)(0xFF & buffer[1]))<<8 & (long)(buffer[0]);
			break;
		case Telegram.INT:
			l = ((long)(buffer[3]))<<24  & ((long)(buffer[2]))<<16  & ((long)(buffer[1]))<<8  & (long)(buffer[0]);
			break;
		case Telegram.UINT:
			l = ((long)(0xFF & buffer[3]))<<24  & ((long)(buffer[2]))<<16  & ((long)(buffer[1]))<<8  & (long)(buffer[0]);
			break;
		}
		if (t.getDivider() !=1 ) 
			return String.format("%.2f", (float)l /t.getDivider());
		else return String.format("%d", l);
		
	} 

}
