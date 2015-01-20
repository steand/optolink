package de.myandres.optolink;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataStore {

	static Logger log = LoggerFactory.getLogger(DataStore.class);
	
	private List<Data> dataList;

	private class Data {
	
	    Data() {}; 
		
		Data(String address, String value) {
			this.address=address;
			this.value=value;
		}
		
		public String address;
		public String value;

	}
	
	DataStore() {
	 dataList = new ArrayList<Data>();
	}
	
	public void set(String address, String value) {
		for (int i=0; i<dataList.size(); i++) {
			if (address.equals(dataList.get(i).address)) { 
				dataList.set(i, new Data(address, value));
				return;
			}
		}
		log.error("Address: {} not subscribed", String.format("%04X", address) );
		
	}
	
	public boolean isChange(String address, String value) {
		// check if value is change and set new value, if changed
		Data d = new Data();
		for (int i=0; i<dataList.size(); i++) {
			d=dataList.get(i);
			if ((address.equals(d.address))) { 
				// found 
			    if (d.value.equals(value)) return false ; // nothing to do
			    else {
			    	this.set(address, value);
			    	return true;	
			    }
		    }
		}
		log.error("Address: {} not subscribed", String.format("%04X", address) );
		return false;	
	}

	public void subscribeData(String address) {
		for (int i=0; i<dataList.size(); i++) {
			if (address.equals(dataList.get(i).address)) {
				log.info("Address: {} already subscribed", address);
				return;
			};
		}
		dataList.add(new Data(address, null));
		log.debug("Address: {} subscribed", address);
	}
	
	public String getData(String address) {
		for (int i=0; i<dataList.size(); i++) {
			if (address.equals(dataList.get(i).address)) return dataList.get(i).value;
		}
		log.error("Address: {} not subscribed -> Value: null returned", address );
		return null;
	}
	
	public void unsubscribeData(String address) {
		for (int i=0; i<dataList.size(); i++) {
			if (address.equals(dataList.get(i).address)) {
				dataList.remove(i);
				log.info("Address: {}  unsubscribed", String.format("%04X", address));
				return;
			};
		}
		log.info("Can't unsubscribe Address: {}, address not subscribed", address);
	}
	
}
