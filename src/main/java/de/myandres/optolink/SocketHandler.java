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
	Config config;
	private ServerSocket server;
	private DataStore dataStore;

	SocketHandler(Config config) {
		this.config = config;
		dataStore = new DataStore();
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
        String s;
        String sl[] = new String[8];
        int i;
        
        System.out.println("open");
        
        out.println("Helo from optolink");
        
        while(true) {
            s = in.readLine().toUpperCase();
            if (s.startsWith("SUB")) {
            	sl=s.split(" ");
            	if (config.existAddress(sl[1])) {
            		dataStore.subscribeData(sl[1]);
            	} else {
            	     log.error("Can't subscribe address: {} - address not exist", sl[1]);
            	}
            }
            if (s.startsWith("UNSUB")) {
            	sl=s.split(" ");
            	dataStore.unsubscribeData(sl[1]);
            }
            if (s.equals("LIST")) {
            	for (int i1=0; i1<config.getTelegramSize(); i1++) out.println(config.printTelegram(i1));
            };
            if (s.equals("EXIT")) break;
            System.out.println(s);
        } 
        
        out.println("By from optolink");
		
	}
}
