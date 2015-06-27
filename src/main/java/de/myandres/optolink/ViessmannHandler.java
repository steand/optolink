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

public class ViessmannHandler {
	static Logger log = LoggerFactory.getLogger(ViessmannHandler.class);
	private ViessmannProtocol viessmannProtocol;
	@SuppressWarnings("unused")
	private OptolinkInterface optolinkInterface;

	ViessmannHandler(String interfaceProtocol, OptolinkInterface optolinkInterface) throws Exception {
		
		log.debug("Init Handler for Protokoll {} ...", interfaceProtocol);
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
		int locValue;
		
		switch (telegram.getType()) {
		case Telegram.BOOLEAN:
			if (value.equals("ON")) locValue=1; else  locValue=0;
			break;
			
		case Telegram.DATE:
			log.error("Updat of Date not implemented");
			return null	;
		default : float fl = (new Float(value)) * telegram.getDivider();
			locValue = (int) fl;
			break;
		}
		int resultLength = viessmannProtocol.setData(buffer, telegram.getAddress() , telegram.getLength(), locValue);

		if (resultLength == 0) return null;
        else return formatValue(buffer, telegram.getType(), telegram.getDivider());

	}
	
	
	
	public synchronized String getValue(Telegram telegram)  {
		byte [] buffer = new byte[16];
		long result=0;
		
		int resultLength=viessmannProtocol.getData(buffer,telegram.getAddress(), telegram.getLength());
		if (log.isTraceEnabled()) {
	    	log.trace("Number of Bytes: {}", resultLength);
	    	for (int i=0; i<resultLength; i++) log.trace("[{}] {} ",i,buffer[i]);
		}
		return formatValue(buffer, telegram.getType(), telegram.getDivider());
		
	} 
	
	private String formatValue(byte[] buffer, byte type, short divider) {
		
		log.trace("Formating....");
		long result = 0;
		switch (type) {
		case Telegram.BOOLEAN:
			if (buffer[0] == 0) return "OFF";
			return "ON";
		case Telegram.DATE:
			//TODO check it
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
