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


public class SubscriberThread implements Runnable {
	
	static Logger log = LoggerFactory.getLogger(SubscriberThread.class);
	
	private Config config;
	private DataStore dataStore;
	
	SubscriberThread(Config config, DataStore dataStore){
	  this.config=config;	
	  this.dataStore=dataStore;
	}
	
	@Override
    public void run() {
	  
	 
		
      while ( true  ) {
    	  try {
			log.info("Interval start");
			if (dataStore.getSize()>0) {
				for (int i=0; i<dataStore.getSize(); i++) {
				log.info("Addresse {} testen", String.format("%04X", dataStore.getAddress(i)));
				}
			}
			log.info("Interval {}", dataStore.getInterval());
			Thread.sleep( (long)dataStore.getInterval()*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	  
    	  
    	  

      }
    }
	
	

}
