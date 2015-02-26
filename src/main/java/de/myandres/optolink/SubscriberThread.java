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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SubscriberThread implements Runnable {
	
	static Logger log = LoggerFactory.getLogger(SubscriberThread.class);
	
	private Config config;
	private DataStore dataStore;
	private ServerSocket server;
    private ViessmannHandler viessmannHandler;
    private Socket socket = null;		
    private BufferedReader in;
	private PrintStream out;
	
	
	SubscriberThread(Config config, DataStore dataStore, ViessmannHandler viessmannHandler) throws Exception {
		this.config = config;
		this.dataStore = dataStore;
		this.viessmannHandler = viessmannHandler;

		server = new ServerSocket(config.getSubscriberPort());
		

	}
	
    public void run() {
    	
    	Thread outThread = null;
	  
    	  while (true) {
              try {
            	  log.info("Listen on port {} for connection", config.getSubscriberPort());
                  socket = server.accept();
                  log.info("Connection on port {} accept. Remote host {}", config.getSubscriberPort(), socket.getRemoteSocketAddress());
      		      in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));        
    			  out = new PrintStream(socket.getOutputStream());
    			  
    			  SubscriberOutThread subscriberOut = new SubscriberOutThread();
    			  outThread = new Thread(subscriberOut);
    			  outThread.setName("OutThread");
    			  outThread.start();
    			  
    			  while (true) {//loop until close exception
    				 if (in.read() == -1) { // Socket closed ; 
    		      	        log.info("Connection on Socket {} closed by remote", config.getSubscriberPort());
    		      	        if (outThread !=null) outThread.interrupt();  
    		      	        break;
    				 }
   			      }
              } catch (Exception e) {
      	        log.error("Connection on Socket {} rejected", config.getSubscriberPort(), e);
      	        if (outThread !=null) outThread.interrupt();   
              }
          } 
      }
	
	public class SubscriberOutThread implements Runnable {

		@Override	
		public void run() {
		
		log.debug("Start SubscriberOutThread");
		
		Telegram telegram = new Telegram();
		int address;
		
		try {
          out.println("@Helo");
        
          while(true){
        	for (int i=0; i<dataStore.getSize(); i++) {
        		address=dataStore.getAddress(i);
        		telegram = config.getTelegram(address);
        		out.println(String.format("%04X:", address) + viessmannHandler.readTelegramValue(telegram));
        	}
        	try {
        	Thread.sleep(dataStore.getInterval()*1000);
        	} catch (InterruptedException e) {
        		// Caller send Interrupt  - Say good Bye 
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

