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
	static Logger log = LoggerFactory.getLogger(OptolinkInterface.class);
	private ViessmannProtocol viessmannProtocol;
	@SuppressWarnings("unused")
	private OptolinkInterface optolinkInterface;

	ViessmannHandler(String interfaceProtocol, OptolinkInterface optolinkInterface) throws Exception {
		log.debug("Try to Init Viessmann Handler for Protokoll: {}", interfaceProtocol);
		switch (interfaceProtocol) {
		case "300":
			viessmannProtocol = new Viessmann300(optolinkInterface);
			break;
		case "KW":
			viessmannProtocol = new ViessmannKW(optolinkInterface);
			break;
		default:
			log.error("Unknown Protokol for Optolink: {}", interfaceProtocol);
			throw new RuntimeException();
		}
		log.trace("Viessmann Protokoll Handler for Class {} initalisiert:", viessmannProtocol.getClass().getName());

		log.info("Viessmann Protokoll Handler for Protocol {} initalisiert:", interfaceProtocol);
	}
	
	
	
	public synchronized String setTelegramValue(Telegram t, String value) {

			log.info("Set not implemented jet");
			return null;

	}
	
	
	
	public synchronized String readTelegramValue(Telegram t)  {
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
