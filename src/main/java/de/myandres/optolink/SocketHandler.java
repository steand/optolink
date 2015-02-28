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

/*
 * Install a Socked Handler for ip communication 
 * 
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler  {

	static Logger log = LoggerFactory.getLogger(SocketHandler.class);

	private Config config;
	private DataStore dataStore;
	private ServerSocket server;
	private ViessmannHandler viessmannHandler;
	private SubscriberThread subscriberThread;
	private PrintStream out;
	
	private Thread subThread;

	SocketHandler(Config config, DataStore dataStore, ViessmannHandler viessmannHandler) throws Exception {
		
		
		this.config = config;
		this.dataStore = dataStore;
		this.viessmannHandler = viessmannHandler;

		server = new ServerSocket(config.getPort());
		subscriberThread = new SubscriberThread(config, dataStore, viessmannHandler);
	}

      public void start() {
    	  
    	  // Wait connection
  	  
    	  while (true) {
              Socket socket = null;
              try {
            	  log.info("Listen on port {} for connection", config.getPort());
                  socket = server.accept();
                  log.info("Connection on port {} accept. Remote host {}", config.getPort(), socket.getRemoteSocketAddress());
                  
                  open(socket);
                  subThread.interrupt();
              }

              catch (Exception e) {
      	        log.info("Connection on Socket {} rejected or closed by client", config.getPort());
      	        subThread.interrupt();          
              } 
          } 
         }
	
	
	private void open(Socket socket) throws Exception {
		
		BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintStream(socket.getOutputStream());

        boolean exit=false;
        
        out.println("#Helo from viessmann");
        
       subscriberThread.setOutputStream(out);
       subThread = new Thread(subscriberThread);
       subThread.setName("Subscriber");
 

        subThread.start();
        
        while(!exit) {
            String [] inStr = in.readLine().trim().split(" +");
            String command=inStr[0];
            for (int i=1; i<inStr.length; i++) inStr[i-1]=inStr[i];
            inStr[inStr.length-1]=null;
            if (log.isTraceEnabled()) {
                 log.trace("Command): |{}|", command);
                 for (int i=0; i<inStr.length; i++) log.trace("param[{}]: |{}|", i, inStr[i]);
            }
            switch (command.toLowerCase()) {
            
            case "sub" : subscribe(inStr) ; break;
            case "usub" : unsubscribe(inStr); break;
            case "list" : list(); break;
            case "getall" : getall(); break;
            case "get" : getData(inStr); break;
            case "set" : set(); break;
            case "testme" : testMe(); break;
            case "setint" : setInt(inStr[0]); break;
            case "exit" : exit=true; break;
            default: log.error("Unknown Client Command:", inStr[0]); 
            } 
        } 
        
        out.println("#Bye from viessmann");
		
	}


	private void testMe() {
		// Listen wenn session closed
		
	}

	private void getall() {
		log.trace("Try to get Data for all Addresses");
		Telegram telegram;
    	for (int i=0; i<config.getTelegramListSize(); i++) {
    		try {
    		 telegram = config.getTelegramByIndex(i);	
   			 out.println("@" + telegram.getAddressAsString() + 
   					     ":" + viessmannHandler.readTelegramValue(telegram));		 
   			}
   			catch (Exception e) {
   				log.error("Error in get command",e);
   			}
    	}
	}

	private void setInt(String interval) {
		try {
			dataStore.setInterval(Integer.parseInt(interval));
		} catch (NumberFormatException e) {
			log.error("Invalid interval format: {}", interval);
		}	
	}

	private void set() {
		// TODO Auto-generated method stub
		out.println("set() not implemented jet");
		log.info("set() not implemented jet");
		
	}

	private void getData(String[] address ) {
		log.trace("Try to get Data for Addresses");
		for (int i=0; ((i<address.length) && (address[i] != null)); i++) {
		try {
			if (config.existTelegram(address[i])) {
			 out.println("@" + address[i] + 
					     ":" + viessmannHandler.readTelegramValue(config.getTelegram(address[i])));	
			} else {
		   	     log.error("Can't get data for address: {} - address not exist", address[i]);
			}
			}
			catch (Exception e) {
				log.error("Error in getData command",e);
			}
		}
	}
	
	

	private void list() {
		
    	for (int i=0; i<config.getTelegramListSize(); i++) {
    		out.println("%"+config.viewTelegramDefinition(i));
    	}

	}

	private void subscribe(String[] inStr) {
		for (int i=0; i<inStr.length-1; i++)
    	if (config.existTelegram(inStr[i])) {
    		dataStore.subscribe(inStr[i]);
    	} else {
    	     log.error("Can't subscribe address: {} - address not exist", inStr[i]);
    	}		
		getData(inStr);
	}
	
private void unsubscribe(String[] inStr) {	
	    if (inStr[0].toLowerCase().contains("all")) {
	    	for (int i=0; i<dataStore.getSize(); i++) {
	    		dataStore.unsubscribe(dataStore.getAddress(i));
	    	}
	    	
	    } else {
		   for (int i=0; i<inStr.length-1; i++) dataStore.unsubscribe(inStr[i]);
	    }
	}
}
