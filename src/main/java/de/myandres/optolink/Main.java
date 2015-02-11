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

public class Main {
	
    static Logger log = LoggerFactory.getLogger(Main.class);
    
    // Central Classes, singular only!!
    static Config config;
    static DataStore dataStore;
    static ViessmannHandler viessmannHandler;
    static OptolinkInterface optolinkInterface;
    
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	    
	    log.info("Programm gestartet");	    
	    
	    try {
	    	// TODO ExeptionHandling
            // Central Thread Data
//            config = new Config("src/main/resources/optolink.xml");
            config = new Config("optolink.xml");
            dataStore = new DataStore();
            dataStore.setInterval(config.getInterval());
            
            //Start TTY Handling for Optolink
            optolinkInterface = new OptolinkInterface(config.getTTY(),config.getTtyTimeOut());
  
            
            //Start ViessmannHandler
            
            viessmannHandler = new ViessmannHandler(config.getProtocol(),optolinkInterface);
            
            
            
            
        }  catch (Exception e) {     	
            log.error("SomeThing wrong not init", e);
             
          }
            
         try {   
            // Run SocketHandler
            SocketThread sockedThread = new SocketThread(config, dataStore, viessmannHandler);
            new Thread(sockedThread).start();
            
            
 //          new Thread(oh).start();
           
            
       
            
            log.debug("Programm normal ended");
        }  catch (Exception e) {     	
           log.error("Programm abnormal terminated.");
           log.error("Diagnostic: {}", e.toString());       
         }
	    
		

	}

}
