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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViessmannKW implements ViessmannProtocol {
	
	
	static Logger log = LoggerFactory.getLogger(ViessmannKW.class);

	private OptolinkInterface optolinkInterface;


	ViessmannKW(OptolinkInterface optolinkInterface) {
		log.trace("Start Session for Protokoll 'KW' ....");
		this.optolinkInterface = optolinkInterface;
		log.trace("Start Session for Protokoll 'KW' started");
	}
	

	@Override
	public int getData(byte[] buffer, int address, int length) {
 
		int j=0;
		optolinkInterface.flush();
        while (optolinkInterface.read() != 0x05 ) { // Wait for 0x05
        	if (j++ > 10) {
        		log.error("Can't send Data to OptolinkInterface, missing 0x05");
                log.error("!!!!!!!!!!!!!!!! Trouble with communication to OptolinkInterface !!!!!!!!" );
                log.error("!!!!!!!!!!!!!!!! Pleace check hardware !!!!!!!!" );
        		return -1;
        	}
        
        }    
        optolinkInterface.write(0x01); // Answer to 0x05
        optolinkInterface.write(0xF7); // Read Data
        optolinkInterface.write((byte)(address >> 8));   // upper Byte of address
        optolinkInterface.write((byte)(address & 0xff)); // lower Byte of address
        optolinkInterface.write((byte)length);           // number of expected bytes
        
        for (int i=0; i<length; i++) {
        	buffer[i] = (byte) optolinkInterface.read();
        } 
        return length;
	}



	@Override
	public void close() {
		// nothing to do
		
	}


	@Override
	public int setData(byte[] buffer, int address, int length, int value) {
		
		int j=0;
		optolinkInterface.flush();
        while (optolinkInterface.read() != 0x05 ) { // Wait for 0x05
        	if (j++ > 10) {
        		log.error("Can't send Data to OptolinkInterface, missing 0x05");
                log.error("!!!!!!!!!!!!!!!! Trouble with communication to OptolinkInterface !!!!!!!!" );
                log.error("!!!!!!!!!!!!!!!! Pleace check hardware !!!!!!!!" );
        		return -1;
        	}
        
        }    
        optolinkInterface.write(0x01); // Answer to 0x05
        optolinkInterface.write(0xF4); // write Data
        optolinkInterface.write((byte)(address >> 8));   // upper Byte of address
        optolinkInterface.write((byte)(address & 0xff)); // lower Byte of address
        optolinkInterface.write((byte)length);           // number of expected bytes
        switch (length) {
        case 1: optolinkInterface.write(buffer[0]); 	 // write lower byte
        		break;
        case 8: optolinkInterface.write(buffer[0]); // Timer data has length 8
        		optolinkInterface.write(buffer[1]);
        		optolinkInterface.write(buffer[2]);
        		optolinkInterface.write(buffer[3]);
        		optolinkInterface.write(buffer[4]);
        		optolinkInterface.write(buffer[5]);
        		optolinkInterface.write(buffer[6]);
        		optolinkInterface.write(buffer[7]);
        		break;
        }
		return length;
	}

}
