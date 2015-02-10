package de.myandres.optolink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
    static Logger log = LoggerFactory.getLogger(Main.class);
    static Config config;
    static DataStore dataStore;
    static Viessmann viessmann;
    
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	    
	    log.info("Programm gestartet");	    
	    
	    try {
            // Central Thread Data
//            config = new Config("src/main/resources/optolink.xml");
            config = new Config("optolink.xml");
            dataStore = new DataStore();
            dataStore.setInterval(config.getInterval());
            
            //Start TTY Handling for Optolink
            viessmann = new Viessmann(config);
        }  catch (Exception e) {     	
            log.error("TTY not init", e);
             
          }
            
         try {   
            
            OptolinkHandler oh = new OptolinkHandler(config, dataStore);
            SocketHandler sh = new SocketHandler(config, dataStore, viessmann);
            
            
           new Thread(oh).start();
           new Thread(sh).start();
            
       
            
            log.debug("Programm normal ended");
        }  catch (Exception e) {     	
           log.error("Programm abnormal terminated.");
           log.error("Diagnostic: {}", e.toString());       
         }
	    
		

	}

}
