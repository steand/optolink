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
	private Optolink optolink;

	SocketHandler(Config config, DataStore dataStore, Optolink optolink) {
		this.config = config;
		this.dataStore = dataStore;
		this.optolink = optolink;
		
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
      	        log.error("Connection on Socket {} rejected", config.getPort());
    	        log.error("Diagnostic: {}", e.toString()); 
            
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
        
        out.println("Helo from optolink");
        
        while(!exit) {
            String [] inStr = in.readLine().trim().split(" +");
            switch (inStr[0].toLowerCase()) {
            
            case "sub" : subscribe(inStr) ; break;
            case "usub" : unsubscribe(inStr); break;
            case "list" : list(out); break;
            case "get" : get(inStr[1]); break;
            case "set" : set(); break;
            case "setint" : setInt(inStr[1]); break;
            case "test" : testIt(inStr); break;
            case "exit" : exit=true; break;
            default: log.error("Unknown Client Command:", inStr[0]); 
            } 
        } 
        
        out.println("By from optolink");
		
	}

	public void testIt(String[] inStr) {
		for (int i=0; i<6;i++) {
			
			optolink.psend(0x04);
			optolink.psend(0x16);
			optolink.psend(0x0);
			optolink.psend(0x0);
      		while (optolink.pread() != -1){} ;
			};
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

	private void get(String inStr) {
		Telegram t = config.getTelegram(inStr);
		optolink.startSession();
		byte [] buffer = new byte[32];
		if (t != null) {
			optolink.startSession();
			optolink.getData(buffer, t.getAddress(), t.getLength());
			optolink.stopSession();
		} else {
			log.error("Can't get Data for address: {} - address not exist", inStr);
		}

		
	}

	private void list(PrintStream out) {
		
    	for (int i=0; i<config.getTelegramListSize(); i++) {
    		out.println(config.viewTelegramDefinition(i));
    	}

	}

	private void subscribe(String[] inStr) {
		for (int i=1; i<inStr.length; i++)
    	if (config.existTelegram(inStr[i])) {
    		dataStore.subscribe(inStr[i]);
    	} else {
    	     log.error("Can't subscribe address: {} - address not exist", inStr[1]);
    	}		
	}
	
private void unsubscribe(String[] inStr) {	
		for (int i=1; i<inStr.length; i++) dataStore.unsubscribe(inStr[i]);	
	}
}
