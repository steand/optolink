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

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SubscriberThread implements Runnable {
	
	static Logger log = LoggerFactory.getLogger(SubscriberThread.class);
	
	private Config config;
	private DataStore dataStore;
    private ViessmannHandler viessmannHandler;
	private PrintStream out;
	
	
	SubscriberThread(Config config, DataStore dataStore, ViessmannHandler viessmannHandler) throws Exception {
		this.config = config;
		this.dataStore = dataStore;
		this.viessmannHandler = viessmannHandler;

	}
	
	public void setOutputStream(PrintStream out) {
		this.out = out;

	}
	@Override
    public void run() {
    	
		log.debug("Start SubscriberOutThread");
		
		Telegram telegram = new Telegram();
		int address;
		
	  
    	  while (true) {
         		try {
                    out.println("#Helo from Subscriber");
                  
                    while(true){
                  	for (int i=0; i<dataStore.getSize(); i++) {
                  		address=dataStore.getAddress(i);
                  		telegram = config.getTelegram(address);
                  		out.println(String.format("@%04X:", address) + viessmannHandler.readTelegramValue(telegram));
                  	}
                  	try {
                  	Thread.sleep(dataStore.getInterval()*1000);
                  	} catch (InterruptedException e) {
                  		// Caller send Interrupt  - Say good Bye 
                  		out.println("#Bye from Subscriber");
                  		log.debug("Caller has send Interrupt -> Good Bye);");
                  		return; //Bye Bye
                  	}      	
                  }
          		
          		} catch (Exception e1) {
          			// TODO Auto-generated catch block
          			log.error("Something wrong", e1);
          		}
            	  
          } 
      }
	
}


