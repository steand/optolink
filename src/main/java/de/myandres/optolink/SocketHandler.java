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

public class SocketHandler implements Runnable {

	static Logger log = LoggerFactory.getLogger(SocketHandler.class);

	private Config config;
	private DataStore dataStore;
	private ServerSocket server;
	private Viessmann viessmann;

	SocketHandler(Config config, DataStore dataStore, Viessmann viessmann) {
		this.config = config;
		this.dataStore = dataStore;
		this.viessmann = viessmann;
		
		try {
			server = new ServerSocket(config.getPort());

		} catch (IOException e) {
			log.error("Can't init Socket {}", config.getPort());
			log.error("Diagnostic: {}", e.toString());
		}
	}

	@Override
      public void run() {
    	  
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
            
              } finally {
                  if (socket != null)
                      try {
                          socket.close();
                          log.info("Connection (port: {}) closed", config.getPort());
                      } catch (IOException e) {
               	        log.error("Closing Port {}", config.getPort());
            	        log.error("Diagnostic: {}", e.toString()); 
         
                      }
              }
          } 
  	
         }
	
	
	private void open(Socket socket) throws Exception {
		
		BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintStream out = new PrintStream(socket.getOutputStream());

        boolean exit=false;
        
        System.out.println("open");
        
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
            case "setint" : setInt(inStr[0]); break;
            case "exit" : exit=true; break;
            default: log.error("Unknown Client Command:", inStr[0]); 
            } 
        } 
        
        out.println("By from viessmann");
		
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
		log.trace("Try to get Data for Addresses");
		try {
			  return viessmann.readData(address);		 
			}
			catch (Exception e) {
				log.error("Error in get command",e);
			}
		
		return "nix";
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
    	     log.error("Can't subscribe address: {} - address not exist", inStr[1]);
    	}		
	}
	
private void unsubscribe(String[] inStr) {	
		for (int i=0; i<inStr.length-1; i++) dataStore.unsubscribe(inStr[i]);	
	}
}
