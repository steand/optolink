package de.myandres.optolink;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Stores all Dynamic Data 
 */

public class DataStore {

	static Logger log = LoggerFactory.getLogger(DataStore.class);
	
	private List<Data> dataList;
	
	private int interval;
	

	private class Data {
				
		public int address;
		public Object value;
		
		Data(int address, Object value) {
			this.address = address;
			this.value = value;
			}
		}

	
	
	DataStore() {
	 dataList = new ArrayList<Data>();
	}
	
	public synchronized void set(int address, Object value) {
		for (int i=0; i<dataList.size(); i++) {
			if (address == dataList.get(i).address) { 
				dataList.set(i, new Data(address, value));
				return;
			}
		}
		log.error("Address: {} not subscribed", String.format("%04X", address));
		
	}
	
	public boolean isChange(int address, Object value) {
		// check if value is change and set new value, if changed
		for (int i=0; i<dataList.size(); i++) {
			if ((address == dataList.get(i).address)) { 
				// found 
			    if (value.equals(dataList.get(i).value)) return false ; // nothing to do
			    else {
			    	this.set(address, value);
			    	return true;	
			    }
		    }
		}
		log.error("Address: {} not subscribed", String.format("%04X", address) );
		return false;	
	}

	public void subscribe(String address) {
		try {
			int addr = Integer.parseInt(address,16);
			subscribe(addr);
		} catch (NumberFormatException e) {
			log.error("Invalide Format of address: {}", address);
		}
	}
	
	public void subscribe(int address) {
		for (int i=0; i<dataList.size(); i++) {
			if (address == dataList.get(i).address) {
				log.info("Address: {} already subscribed", String.format("%04X", address));
				return;
			};
		}
		dataList.add(new Data(address, null));
		log.debug("Address: {} subscribed", String.format("%04X", address));
	}
	
	public String getValue(int  address) {
		for (int i=0; i<dataList.size(); i++) {
			if (address == dataList.get(i).address) {
                  return  String.valueOf(dataList.get(i).value);
			    }
		}
		log.error("Address: {} not subscribed -> Value: null returned", String.format("%04X", address) );
		return null;
	}
	
	public void unsubscribe(String address) {
		try {
			int addr = Integer.parseInt(address);
			unsubscribe(addr);
		} catch (NumberFormatException e) {
			log.error("Invalide Format of address: {}", address);
		}
	}
	
	public void unsubscribe(int address) {
		for (int i=0; i<dataList.size(); i++) {
			if (address == dataList.get(i).address) {
				dataList.remove(i);
				log.info("Address: {}  unsubscribed", String.format("%04X", String.format("%04X", address)));
				return;
			};
		}
		log.info("Can't unsubscribe Address: {}, address not subscribed", String.format("%04X", address));
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	public int getSize() {
		return dataList.size();
	}
	
	public int getAddress(int index) {
		return dataList.get(index).address;
	}
	
}
