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

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViessmannHandler {
	static Logger log = LoggerFactory.getLogger(ViessmannHandler.class);
	private ViessmannProtocol viessmannProtocol;
	private OptolinkInterface optolinkInterface;
	private Config config;
	private String interfaceProtocol;

	ViessmannHandler(Config config, OptolinkInterface optolinkInterface) throws Exception {
		
		log.debug("Init Handler for Protokoll {} ...", interfaceProtocol);
		this.config = config;
		this.optolinkInterface = optolinkInterface;
		interfaceProtocol = config.getProtocol();
		switch (interfaceProtocol) {
		case "300":
			viessmannProtocol = new Viessmann300(optolinkInterface);
			break;
		case "KW":
			viessmannProtocol = new ViessmannKW(optolinkInterface);
			break;
		default:
			log.error("Unknown Protokol: {}", interfaceProtocol);
			throw new RuntimeException();
		}
		log.trace("Handler for Class {} initalisiert", viessmannProtocol.getClass().getName());

		log.info("Handler for Protocol {} initalisiert", interfaceProtocol);
	}
	
	public synchronized void close() {
		viessmannProtocol.close();
	}
	
	
	public synchronized String setValue(Telegram telegram, String value) {
		byte [] buffer = new byte[16];
		int locValue=0;
		
		switch (telegram.getType()) {
		case Telegram.BOOLEAN:
			if (value.equals("ON")) buffer[0]=1; else  buffer[0]=0;
			break;
			
		case Telegram.DATE:
			log.error("Update of Date not implemented");
			return null	;
		case Telegram.TIMER: // Receiving a string of format: "On:--:--Off:--:--On:--:--Off:--:--On:--:--Off:--:--On:--:-- Off:--:--"
			String[] switchTimes = new String[8];
			String[] timeParts = new String[2];
			int hr;
			int min;
			int switchTimesLength = 0;
			Pattern pattern = Pattern.compile("(\\d{1,2}:\\d{2})");
			Matcher matcher = pattern.matcher(value);

			while (matcher.find()) {
				switchTimes[switchTimesLength++] = matcher.group(1);
			}

			if ((switchTimesLength % 2) == 0) {
				for (int i = 0; i<switchTimesLength; i++) {
					timeParts = switchTimes[i].split(":");
					hr = Integer.parseInt(timeParts[0]);
					min = Integer.parseInt(timeParts[1]);
					if (hr > 23 | hr < 0 | min > 59 | min < 0) {
						log.error("Invalid time. Hour %d has to between 0 and 23 and Minute %d between 0 and 59", hr,min);
						return null;
					}
					hr = hr << 3;
					min = (int) min/10;
					buffer[i] = (byte) (hr | min);
				}
				for (int i = switchTimesLength; i < 8; i++) {
					buffer[i] = (byte) 0xff;
				}
				for (int i = 0; i < 8; i+=2) {
					if ((buffer[i] & 0xff) > (buffer[i+1] & 0xff)) {
						log.error("Invalid time pair. On time %02x if bigger than Off time %02x", buffer[i],buffer[i+1]);
						return null;
					}
				}
			} else {
				log.error("Error! SwitchTime has to be in on/off pairs");
				return null;
			}
			locValue = 9;
			break;
		default : float fl = (new Float(value)) * telegram.getDivider(); // all other writable channels are byte or ubyte
			buffer[0] = (byte) fl;
			break;
		}
		if (this.config.getTTYType().matches("URL")) {
			try {
				optolinkInterface.open();
			} catch (Exception e) {
				log.error("Opening TTY type URL failed");
				optolinkInterface.close();
				return null;
			}
		}
		int resultLength = viessmannProtocol.setData(buffer, telegram.getAddress() , telegram.getLength(), locValue);
		if (this.config.getTTYType().matches("URL")) {
			optolinkInterface.close();
		}

		if (resultLength == 0) return null;
        else return formatValue(buffer, telegram.getType(), telegram.getDivider());

	}
	
	
	
	public synchronized String getValue(Telegram telegram)  {
		byte [] buffer = new byte[16];
		
		if (this.config.getTTYType().matches("URL")) {
			try {
				optolinkInterface.open();
			} catch (Exception e) {
				log.error("Opening TTY type URL failed");
				optolinkInterface.close();
				return null;
			}
		}
		int resultLength=viessmannProtocol.getData(buffer,telegram.getAddress(), telegram.getLength());
		if (log.isTraceEnabled()) {
	    	log.trace("Number of Bytes: {}", resultLength);
	    	for (int i=0; i<resultLength; i++) log.trace("[{}] {} ",i,buffer[i]);
		}
		if (this.config.getTTYType().matches("URL")) {
			optolinkInterface.close();
		}
		return formatValue(buffer, telegram.getType(), telegram.getDivider());
		
	} 
	
	private String formatValue(byte[] buffer, byte type, short divider) {
		
		log.trace("Formating....");
		long result = 0;
		String timer = "";
		switch (type) {
		case Telegram.BOOLEAN:
			if (buffer[0] == 0) return "OFF";
			return "ON";
		case Telegram.TIMER:
			for (int i=0; i<8; i+=2) {
				if (buffer[i] == -1) { // -1 equals 0xFF
					timer += "On:--:--Off:--:--";
				} else {
					timer += String.format("On:%02d:%02dOff:%02d:%02d", 
							(buffer[i] & 0xF8)>>3,(buffer[i] & 7)*10,	(buffer[i+1] & 0xF8)>>3,(buffer[i+1] & 7)*10);
				}
			}
			return timer;
		case Telegram.DATE:
			return String.format("%02x%02x-%02x-%02xT%02x:%02x:%02x",
					buffer[0],buffer[1],buffer[2],buffer[3],buffer[5],buffer[6],buffer[7])	;
		case Telegram.BYTE:
             result = buffer[0];
             break;
		case Telegram.UBYTE:
			  result = 0xFF & buffer[0];
			break;		
		case Telegram.SHORT:
			 result = ((long)(buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
			break;
		case Telegram.USHORT:
			result = ((long)(0xFF & buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
			break;
		case Telegram.INT:
			result = ((long)(buffer[3]))*0x1000000  + ((long)(0xFF & buffer[2]))*0x10000  + ((long)(0xFF & buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
			break;
		case Telegram.UINT:
			result = ((long)(0xFF & buffer[3]))*0x1000000  + ((long)(0xFF & buffer[2]))*0x10000  + ((long)(0xFF & buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
			break;
		}
		if (divider !=1 ) 
			return String.format(Locale.US,"%.2f", (float)result / divider);
		else return String.format("%d", result);
		
		
	}
	

}
