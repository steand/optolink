package de.myandres.optolink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OptolinkHandler implements Runnable {
	
	static Logger log = LoggerFactory.getLogger(OptolinkHandler.class);
	
	private Config config;
	private DataStore dataStore;
	
	OptolinkHandler(Config config, DataStore dataStore){
	  this.config=config;	
	  this.dataStore=dataStore;
	}
	
	@Override
    public void run() {
	  
	 
		
      while ( true  ) {
    	  try {
			log.info("Interval start");
			if (dataStore.getSize()>0) {
				for (int i=0; i<dataStore.getSize(); i++) {
				log.info("Addresse {} testen", String.format("%04X", dataStore.getAddress(i)));
				}
			}
			log.info("Interval {}", dataStore.getInterval());
			Thread.sleep( (long)dataStore.getInterval()*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	  
    	  
    	  

      }
    }
	
	

}
