package de.myandres.optolink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OptolinkHandler implements Runnable {
	
	static Logger log = LoggerFactory.getLogger(OptolinkHandler.class);
	
	Config config;
	
	OptolinkHandler(Config config){
	  this.config=config;	
	}
	
	@Override
    public void run() {
	  
	 
		
      while ( true  ) {
    	  try {
			Thread.sleep(5000);
			log.info("config.port: {}", config.getPort());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	  
    	  
    	  

      }
    }
	
	

}
