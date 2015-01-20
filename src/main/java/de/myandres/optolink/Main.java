package de.myandres.optolink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
    static Logger log = LoggerFactory.getLogger(Main.class);
    
    
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	    
	    log.info("Programm gestartet");	    
	    
	    try {

            final Config config = new Config("src/main/resources/optolink.xml");
            
            OptolinkHandler oh = new OptolinkHandler(config);
            SocketHandler sh = new SocketHandler(config);
            
           //  new Thread(oh).start();
            new Thread(sh).start();
            
            
            
            log.debug("Programm normal ended");
        }  catch (Exception e) {     	
           log.error("Programm abnormal terminated.");
           log.error("Diagnostic: {}", e.toString());       
         }
	    
		

	}

}
