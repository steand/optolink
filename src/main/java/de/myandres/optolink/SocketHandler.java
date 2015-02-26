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
import java.io.IOException;
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

	SocketHandler(Config config, DataStore dataStore, ViessmannHandler viessmannHandler) throws Exception {
		this.config = config;
		this.dataStore = dataStore;
		this.viessmannHandler = viessmannHandler;

		server = new ServerSocket(config.getPort());

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
              }

              catch (Exception e) {
      	        log.error("Connection on Socket {} rejected", config.getPort(), e);
            
              } 
          } 
         }
	
	
	private void open(Socket socket) throws Exception {
		
		BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintStream out = new PrintStream(socket.getOutputStream());

        boolean exit=false;
        
        out.println("Helo from viessmann");
        
        while(!exit) {
            String [] inStr = in.readLine().trim().split(" +");
            String command=inStr[0];
            log.trace("Command): |{}|", command);
            for (int i=1; i<inStr.length; i++) inStr[i-1]=inStr[i];
            inStr[inStr.length-1]=null;
            for (int i=0; i<inStr.length; i++)
            log.trace("param[{}]: |{}|", i, inStr[i]);
            switch (command.toLowerCase()) {
            
            case "sub" : subscribe(inStr) ; break;
            case "usub" : unsubscribe(inStr); break;
            case "list" : list(out); break;
            case "getall" : out.println(getall()); break;
            case "get" : 
            	out.println(getData(inStr)); 
//            	getData(inStr[1]);
            	break;
            case "set" : set(); break;
            case "testme" : testMe(); break;
            case "setint" : setInt(inStr[0]); break;
            case "exit" : exit=true; break;
            default: log.error("Unknown Client Command:", inStr[0]); 
            } 
        } 
        
        out.println("By from viessmann");
		
	}


	private void testMe() {
		// Listen wenn session closed
		
	}

	private String getall() {
		String[] s = new String[30];
    	for (int i=0; i<config.getTelegramListSize(); i++) {
    		log.trace("getall: adr = {}", String.format("%04X",config.getTelegramByIndex(i).getAddress()));
    		s[i] = String.format("%04X",config.getTelegramByIndex(i).getAddress());
    	}
       return getData(s);
		
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
		log.info("set() not implemented jet");
		
	}

	private String getData(String[] address ) {
		String returnStr="";
		log.trace("Try to get Data for Addresses");
		for (int i=0; ((i<address.length) && (address[i] != null)); i++) {
		try {
			  returnStr+= address[i] + ":" + viessmannHandler.readTelegramValue(config.getTelegram(address[i])) + "\n";		 
			}
			catch (Exception e) {
				log.error("Error in get command",e);
			}
		}
		
		return returnStr;
	}
	
	

	private void list(PrintStream out) {
		
    	for (int i=0; i<config.getTelegramListSize(); i++) {
    		out.println(config.viewTelegramDefinition(i));
    	}

	}

	private void subscribe(String[] inStr) {
		for (int i=0; i<inStr.length-1; i++)
    	if (config.existTelegram(inStr[i])) {
    		dataStore.subscribe(inStr[i]);
    	} else {
    	     log.error("Can't subscribe address: {} - address not exist", inStr[i]);
    	}		
	}
	
private void unsubscribe(String[] inStr) {	
		for (int i=0; i<inStr.length-1; i++) dataStore.unsubscribe(inStr[i]);	
	}
}
