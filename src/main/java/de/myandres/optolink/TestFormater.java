package de.myandres.optolink;

import java.util.List;

public class TestFormater {
	
	Config config;
	Thing t;
    List<Thing> tl;
    Channel c;
    List<Channel> cl;
    
    Telegram a;
	
	TestFormater(Config config) {
		this.config = config;
	}

	
	public void print() {
		
		
        Thing t;

        Telegram a;
        
        
        tl=config.getThingList();
        for (int i=0; i<tl.size(); i++) {
        	t=tl.get(i);
        	System.out.println("###########################################");
        	System.out.println(t.getId());
        	System.out.println(t.getType());
        	System.out.println(t.getDescribtion());
        	cl = t.getChannelList();
        	for (int j=0; j<cl.size(); j++) {
            	System.out.println("---------------------------CCCCCCCC");
        		c=cl.get(j);
               	System.out.println(c.getId());
            	System.out.println(c.getType());
            	System.out.println(c.getDescribtion());
            	System.out.println(c.getTelegram().getAddress());
            	System.out.println(c.getTelegram().getType());
            	System.out.println(c.getTelegram().getLength());
            	System.out.println(c.getTelegram().getDivider());
        	}
        }
		
	}
}
